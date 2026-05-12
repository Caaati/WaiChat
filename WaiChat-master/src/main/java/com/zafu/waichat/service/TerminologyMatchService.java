package com.zafu.waichat.service;

import com.zafu.waichat.pojo.dto.TerminologyCacheDTO;
import com.zafu.waichat.pojo.dto.TerminologyTranslationPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TerminologyMatchService {

    /** 拼进提示词的术语表：最多行数（多词条命中时尽量都带上） */
    private static final int TOP_K = 24;
    /** 翻译 API terms 条数上限（可多条：多词条、同条多别名命中） */
    private static final int MAX_TRANSLATION_PAIRS = 48;
    /** terms 字段中单条 target 最大长度，避免超出模型侧限制 */
    private static final int MAX_TARGET_LEN = 512;

    @Autowired
    private TerminologyCacheService terminologyCacheService;

    /**
     * 命中「我的术语」转为 DashScope translation_options.terms（source=正文中的标准名或别名，target=推荐用语）。
     * <p>别名表可按 {@code translateTargetLang} 筛选：优先使用目标语言匹配的行，其次使用未限定目标语言的行。
     */
    public List<TerminologyTranslationPair> buildTranslationTermPairs(Integer userId, String text, String translateTargetLang) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String haystack = text.toLowerCase(Locale.ROOT);
        String targetLangNorm = translateTargetLang == null ? "" : translateTargetLang.trim();
        List<MatchCand> cands = sortedCandidates(userId, true);
        Set<String> seenTermIdAndSource = new LinkedHashSet<>();
        List<TerminologyTranslationPair> out = new ArrayList<>();
        for (MatchCand c : cands) {
            if (out.size() >= MAX_TRANSLATION_PAIRS) {
                break;
            }
            String ph = c.phrase != null ? c.phrase.trim() : "";
            if (ph.isEmpty()) {
                continue;
            }
            String pLow = ph.toLowerCase(Locale.ROOT);
            if (!haystack.contains(pLow)) {
                continue;
            }
            String dedupeKey = c.termId + "\t" + pLow;
            if (!seenTermIdAndSource.add(dedupeKey)) {
                continue;
            }
            List<String> aliasOnly = aliasesOnly(c.phrases(), c.displayTerm);
            String target = resolveTranslationTarget(ph, c.displayTerm, c.aliasRows, aliasOnly, targetLangNorm);
            if (target.isEmpty() || ph.equalsIgnoreCase(target)) {
                continue;
            }
            out.add(new TerminologyTranslationPair(ph, target));
        }
        return out;
    }

    /**
     * @param personalOnly 为 true 时仅「我的术语」（owner_user_id 非空），不包含系统默认库。
     */
    private List<MatchCand> sortedCandidates(Integer userId, boolean personalOnly) {
        List<MatchCand> cands = new ArrayList<>();
        List<TerminologyCacheDTO> userRows = terminologyCacheService.loadUserSnapshot(userId);
        for (TerminologyCacheDTO dto : userRows) {
            addCands(dto, cands, 0);
        }
        if (!personalOnly) {
            List<TerminologyCacheDTO> systemRows = terminologyCacheService.loadSystemSnapshot();
            for (TerminologyCacheDTO dto : systemRows) {
                addCands(dto, cands, 1);
            }
        }
        cands.sort(Comparator
                .comparingInt((MatchCand c) -> -c.phrase.length())
                .thenComparingInt(MatchCand::systemRank)
                .thenComparingInt((MatchCand c) -> -c.sortWeight));
        return cands;
    }

    /** 与 terminology_alias 对应：标准术语以外的别名列表（来自缓存 phrases，已含顺序）。 */
    private static List<String> aliasesOnly(List<String> phrases, String displayTerm) {
        String t = displayTerm == null ? "" : displayTerm.trim();
        if (phrases == null || phrases.isEmpty()) {
            return List.of();
        }
        return phrases.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.equalsIgnoreCase(t))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * target 来自别名行（按目标语言优先）；若无结构化行则退回「与 source 不同的第一条别名」逻辑。
     */
    private static String resolveTranslationTarget(String sourcePhrase, String displayTerm,
                                                   List<TerminologyCacheDTO.AliasCacheRow> aliasRows,
                                                   List<String> aliasOnlyFallback,
                                                   String requestTargetLang) {
        String src = sourcePhrase == null ? "" : sourcePhrase.trim();
        if (aliasRows != null && !aliasRows.isEmpty()) {
            List<TerminologyCacheDTO.AliasCacheRow> ordered = orderAliasRowsForTranslate(aliasRows, requestTargetLang);
            for (TerminologyCacheDTO.AliasCacheRow row : ordered) {
                if (row == null || row.getAlias() == null || row.getAlias().isBlank()) {
                    continue;
                }
                String at = row.getAlias().trim();
                if (!at.equalsIgnoreCase(src)) {
                    return normalizeAliasText(at);
                }
            }
        }
        if (aliasOnlyFallback == null || aliasOnlyFallback.isEmpty()) {
            return fallbackDisplayTerm(displayTerm, src);
        }
        for (String a : aliasOnlyFallback) {
            if (!a.equalsIgnoreCase(src)) {
                return normalizeAliasText(a);
            }
        }
        return fallbackDisplayTerm(displayTerm, src);
    }

    private static String fallbackDisplayTerm(String displayTerm, String src) {
        String dt = displayTerm == null ? "" : displayTerm.trim();
        if (!dt.isEmpty() && !dt.equalsIgnoreCase(src)) {
            return normalizeAliasText(dt);
        }
        return "";
    }

    /**
     * 顺序：先当前请求目标语言精确匹配的行（库表 id 序），再「未限定目标语言」的行，最后其它语言行（兼容旧数据）。
     */
    private static List<TerminologyCacheDTO.AliasCacheRow> orderAliasRowsForTranslate(
            List<TerminologyCacheDTO.AliasCacheRow> all, String requestLang) {
        if (all == null || all.isEmpty()) {
            return List.of();
        }
        String lang = requestLang == null ? "" : requestLang.trim();
        List<TerminologyCacheDTO.AliasCacheRow> exact = new ArrayList<>();
        List<TerminologyCacheDTO.AliasCacheRow> wild = new ArrayList<>();
        List<TerminologyCacheDTO.AliasCacheRow> other = new ArrayList<>();
        for (TerminologyCacheDTO.AliasCacheRow r : all) {
            if (r == null || r.getAlias() == null || r.getAlias().isBlank()) {
                continue;
            }
            String tl = r.getTargetLang() == null ? "" : r.getTargetLang().trim();
            if (tl.isEmpty()) {
                wild.add(r);
            } else if (!lang.isEmpty() && tl.equalsIgnoreCase(lang)) {
                exact.add(r);
            } else {
                other.add(r);
            }
        }
        List<TerminologyCacheDTO.AliasCacheRow> out = new ArrayList<>(exact);
        out.addAll(wild);
        if (!out.isEmpty()) {
            return out;
        }
        if (!other.isEmpty()) {
            return other;
        }
        return new ArrayList<>(all);
    }

    private static String normalizeAliasText(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String firstLine = raw.trim().split("\\R", 2)[0].trim();
        if (firstLine.length() > MAX_TARGET_LEN) {
            return firstLine.substring(0, MAX_TARGET_LEN).trim();
        }
        return firstLine;
    }

    /**
     * 根据正文做关键词子串匹配，返回格式化的术语表文本；仅「我的术语」，无命中返回 null。
     */
    public String buildGlossaryBlock(Integer userId, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String haystack = text.toLowerCase(Locale.ROOT);
        List<MatchCand> cands = sortedCandidates(userId, true);
        Set<Integer> used = new LinkedHashSet<>();
        List<String> lines = new ArrayList<>();
        for (MatchCand c : cands) {
            if (used.contains(c.termId)) {
                continue;
            }
            String p = c.phrase.toLowerCase(Locale.ROOT);
            if (p.isEmpty()) {
                continue;
            }
            if (haystack.contains(p)) {
                used.add(c.termId);
                lines.add(formatGlossaryLine(c.displayTerm, c.definition, c.phrases()));
                if (lines.size() >= TOP_K) {
                    break;
                }
            }
        }
        if (lines.isEmpty()) {
            return null;
        }
        return String.join("\n", lines);
    }

    private void addCands(TerminologyCacheDTO dto, List<MatchCand> out, int systemRank) {
        if (dto.getPhrases() == null) {
            return;
        }
        List<String> distinct = dto.getPhrases().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (dto.getId() == null) {
            return;
        }
        int termId = dto.getId();
        String displayTerm = dto.getTerm() != null ? dto.getTerm() : distinct.get(0);
        int sw = dto.getSortWeight() != null ? dto.getSortWeight().intValue() : 0;
        List<String> phraseSnapshot = new ArrayList<>(distinct);
        List<TerminologyCacheDTO.AliasCacheRow> aliasRows = dto.getAliasRows() != null
                ? new ArrayList<>(dto.getAliasRows())
                : List.of();
        for (String phrase : distinct) {
            out.add(new MatchCand(termId, phrase, displayTerm, dto.getDefinition(), sw, systemRank, phraseSnapshot, aliasRows));
        }
    }

    /**
     * 术语表一行：标准名、别名（若有）、释义，便于翻译时采用「外聊」等推荐称谓。
     */
    private static String formatGlossaryLine(String term, String definition, List<String> phrases) {
        String t = term != null ? term.trim() : "";
        String def = definition != null ? definition : "";
        if (t.isEmpty()) {
            return "- " + def;
        }
        if (phrases == null || phrases.isEmpty()) {
            return "- " + t + "：" + def;
        }
        List<String> aliases = phrases.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.equalsIgnoreCase(t))
                .distinct()
                .toList();
        if (aliases.isEmpty()) {
            return "- " + t + "：" + def + "（无别名：译文中凡与「" + t + "」同指的专名请保持与原文完全一致的字面形式，勿翻译、勿音译、勿改写。）";
        }
        return "- " + t + "；别名：" + String.join("、", aliases) + "；释义：" + def;
    }

    private record MatchCand(int termId, String phrase, String displayTerm, String definition, int sortWeight,
                             int systemRank, List<String> phrases,
                             List<TerminologyCacheDTO.AliasCacheRow> aliasRows) {
    }
}

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
     * 命中「我的术语」转为 DashScope translation_options.terms（source=正文中的标准名或别名，target=别名表中的推荐用语，不用 definition）。
     * <p>支持多条：多个词条、同一词条多个别名/标准名在正文中同时命中时各输出一对；去重键为词条 id + 原文片段，避免不同词条 source 相同时被误合并。
     */
    public List<TerminologyTranslationPair> buildTranslationTermPairs(Integer userId, String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String haystack = text.toLowerCase(Locale.ROOT);
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
            String target = resolveTranslationTargetFromAliases(ph, c.displayTerm, aliasOnly);
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
     * target 仅来自别名表：优先取与 source 不同的第一条别名；若无（例如原文已是唯一别名），则用标准术语 term（仍非 definition）。
     */
    private static String resolveTranslationTargetFromAliases(String sourcePhrase, String displayTerm, List<String> aliasOnly) {
        String src = sourcePhrase == null ? "" : sourcePhrase.trim();
        if (aliasOnly == null || aliasOnly.isEmpty()) {
            return "";
        }
        for (String a : aliasOnly) {
            if (!a.equalsIgnoreCase(src)) {
                return normalizeAliasText(a);
            }
        }
        String dt = displayTerm == null ? "" : displayTerm.trim();
        if (!dt.isEmpty() && !dt.equalsIgnoreCase(src)) {
            return normalizeAliasText(dt);
        }
        return "";
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
        for (String phrase : distinct) {
            out.add(new MatchCand(termId, phrase, displayTerm, dto.getDefinition(), sw, systemRank, phraseSnapshot));
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
                             int systemRank, List<String> phrases) {
    }
}

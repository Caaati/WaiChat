package com.zafu.waichat.service;

import com.zafu.waichat.pojo.dto.TerminologyCacheDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TerminologyMatchService {

    private static final int TOP_K = 12;

    @Autowired
    private TerminologyCacheService terminologyCacheService;

    /**
     * 根据正文做关键词子串匹配，返回格式化的术语表文本；无命中返回 null。
     */
    public String buildGlossaryBlock(Integer userId, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String haystack = text.toLowerCase(Locale.ROOT);
        List<TerminologyCacheDTO> userRows = terminologyCacheService.loadUserSnapshot(userId);
        List<TerminologyCacheDTO> systemRows = terminologyCacheService.loadSystemSnapshot();
        List<MatchCand> cands = new ArrayList<>();
        for (TerminologyCacheDTO dto : userRows) {
            addCands(dto, cands, 0);
        }
        for (TerminologyCacheDTO dto : systemRows) {
            addCands(dto, cands, 1);
        }
        cands.sort(Comparator
                .comparingInt((MatchCand c) -> -c.phrase.length())
                .thenComparingInt(MatchCand::systemRank)
                .thenComparingInt((MatchCand c) -> -c.sortWeight));
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

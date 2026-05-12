package com.zafu.waichat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zafu.waichat.mapper.TerminologyAliasMapper;
import com.zafu.waichat.mapper.TerminologyMapper;
import com.zafu.waichat.pojo.dto.TerminologyCacheDTO;
import com.zafu.waichat.pojo.entity.Terminology;
import com.zafu.waichat.pojo.entity.TerminologyAlias;
import com.zafu.waichat.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TerminologyCacheService {

    public static final String KEY_SYSTEM = "waichat:terminology:system";

    public static String userKey(Integer userId) {
        return "waichat:terminology:user:" + userId;
    }

    private static final long TTL_SECONDS = 1800;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TerminologyMapper terminologyMapper;
    @Autowired
    private TerminologyAliasMapper terminologyAliasMapper;

    public void evictSystem() {
        redisUtil.delete(KEY_SYSTEM);
    }

    public void evictUser(Integer userId) {
        if (userId != null) {
            redisUtil.delete(userKey(userId));
        }
    }

    /**
     * 用户词库变更后：删旧缓存并立即从库重建写入 Redis，避免键长时间缺失。
     */
    public void refreshUser(Integer userId) {
        evictUser(userId);
        loadUserSnapshot(userId);
    }

    public List<TerminologyCacheDTO> loadSystemSnapshot() {
        String json = redisUtil.get(KEY_SYSTEM);
        if (json != null && !json.isEmpty()) {
            try {
                return objectMapper.readValue(json, new TypeReference<>() {
                });
            } catch (Exception e) {
                log.warn("terminology system cache corrupt, rebuilding: {}", e.getMessage());
                redisUtil.delete(KEY_SYSTEM);
            }
        }
        List<TerminologyCacheDTO> built = buildSnapshotFromDb(true, null);
        try {
            redisUtil.setWithExpire(KEY_SYSTEM, objectMapper.writeValueAsString(built), TTL_SECONDS);
        } catch (Exception e) {
            log.error("write system terminology cache failed", e);
        }
        return built;
    }

    public List<TerminologyCacheDTO> loadUserSnapshot(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        String key = userKey(userId);
        String json = redisUtil.get(key);
        if (json != null && !json.isEmpty()) {
            try {
                return objectMapper.readValue(json, new TypeReference<>() {
                });
            } catch (Exception e) {
                log.warn("terminology user cache corrupt, rebuilding: {}", e.getMessage());
                redisUtil.delete(key);
            }
        }
        List<TerminologyCacheDTO> built = buildSnapshotFromDb(false, userId);
        try {
            redisUtil.setWithExpire(key, objectMapper.writeValueAsString(built), TTL_SECONDS);
        } catch (Exception e) {
            log.error("write user terminology cache failed", e);
        }
        return built;
    }

    private List<TerminologyCacheDTO> buildSnapshotFromDb(boolean system, Integer ownerUserId) {
        QueryWrapper<Terminology> qw = new QueryWrapper<>();
        qw.eq("enabled", 1);
        if (system) {
            qw.isNull("owner_user_id");
        } else {
            qw.eq("owner_user_id", ownerUserId);
        }
        List<Terminology> rows = terminologyMapper.selectList(qw);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ids = rows.stream().map(Terminology::getId).toList();
        QueryWrapper<TerminologyAlias> aw = new QueryWrapper<>();
        aw.in("terminology_id", ids).orderByAsc("id");
        List<TerminologyAlias> aliases = terminologyAliasMapper.selectList(aw);
        Map<Integer, List<TerminologyAlias>> aliasRowsByTerm = aliases.stream()
                .collect(Collectors.groupingBy(TerminologyAlias::getTerminologyId, LinkedHashMap::new, Collectors.toList()));
        List<TerminologyCacheDTO> out = new ArrayList<>();
        for (Terminology t : rows) {
            TerminologyCacheDTO dto = new TerminologyCacheDTO();
            dto.setId(t.getId());
            dto.setOwnerUserId(t.getOwnerUserId());
            dto.setTerm(t.getTerm());
            dto.setDefinition(t.getDefinition());
            dto.setSortWeight(t.getSortWeight() != null ? t.getSortWeight() : 0);
            List<TerminologyAlias> termAliases = aliasRowsByTerm.getOrDefault(t.getId(), Collections.emptyList());
            List<TerminologyCacheDTO.AliasCacheRow> aliasRows = new ArrayList<>();
            for (TerminologyAlias al : termAliases) {
                if (al.getAlias() == null || al.getAlias().isBlank()) {
                    continue;
                }
                String tl = al.getTargetLang();
                aliasRows.add(new TerminologyCacheDTO.AliasCacheRow(
                        al.getAlias().trim(),
                        tl != null && !tl.isBlank() ? tl.trim() : null));
            }
            dto.setAliasRows(aliasRows);
            LinkedHashSet<String> phrases = new LinkedHashSet<>();
            if (t.getTerm() != null && !t.getTerm().isBlank()) {
                phrases.add(t.getTerm().trim());
            }
            for (TerminologyCacheDTO.AliasCacheRow row : aliasRows) {
                phrases.add(row.getAlias());
            }
            dto.setPhrases(new ArrayList<>(phrases));
            out.add(dto);
        }
        return out;
    }
}

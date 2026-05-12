package com.zafu.waichat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zafu.waichat.mapper.TerminologyAliasMapper;
import com.zafu.waichat.mapper.TerminologyGroupMapper;
import com.zafu.waichat.mapper.TerminologyMapper;
import com.zafu.waichat.pojo.dto.TerminologyImportSystemDTO;
import com.zafu.waichat.pojo.dto.TerminologyAliasItemDTO;
import com.zafu.waichat.pojo.dto.TerminologySaveDTO;
import com.zafu.waichat.pojo.entity.Terminology;
import com.zafu.waichat.pojo.entity.TerminologyAlias;
import com.zafu.waichat.pojo.entity.TerminologyGroup;
import com.zafu.waichat.pojo.vo.TerminologyEntryVO;
import com.zafu.waichat.pojo.vo.TerminologyAliasVO;
import com.zafu.waichat.pojo.vo.TerminologyGroupSectionVO;
import com.zafu.waichat.pojo.vo.TerminologyMineGroupedVO;
import com.zafu.waichat.pojo.vo.TerminologySystemTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TerminologyService {

    private static final int MAX_IMPORT_PER_GROUP = 50;

    @Autowired
    private TerminologyMapper terminologyMapper;
    @Autowired
    private TerminologyAliasMapper terminologyAliasMapper;
    @Autowired
    private TerminologyGroupMapper terminologyGroupMapper;
    @Autowired
    private TerminologyCacheService terminologyCacheService;

    public List<TerminologyEntryVO> listSystemEntries() {
        QueryWrapper<Terminology> qw = new QueryWrapper<>();
        qw.isNull("owner_user_id").eq("enabled", 1).orderByDesc("sort_weight").orderByDesc("id");
        return toVoList(terminologyMapper.selectList(qw));
    }

    /**
     * 系统术语按组 + 无组扁平词条，供前端勾选导入。
     */
    public TerminologySystemTreeVO listSystemGroups() {
        TerminologySystemTreeVO tree = new TerminologySystemTreeVO();
        QueryWrapper<TerminologyGroup> gq = new QueryWrapper<>();
        gq.eq("enabled", 1).orderByDesc("sort_weight").orderByAsc("id");
        List<TerminologyGroup> groupRows = terminologyGroupMapper.selectList(gq);
        for (TerminologyGroup g : groupRows) {
            QueryWrapper<Terminology> tq = new QueryWrapper<>();
            tq.isNull("owner_user_id").eq("enabled", 1).eq("group_id", g.getId())
                    .orderByDesc("sort_weight").orderByAsc("id");
            List<Terminology> terms = terminologyMapper.selectList(tq);
            TerminologyGroupSectionVO sec = new TerminologyGroupSectionVO();
            sec.setGroupId(g.getId());
            sec.setGroupName(g.getName());
            sec.setEntries(toVoList(terms));
            tree.getGroups().add(sec);
        }
        QueryWrapper<Terminology> uq = new QueryWrapper<>();
        uq.isNull("owner_user_id").eq("enabled", 1).isNull("group_id")
                .orderByDesc("sort_weight").orderByAsc("id");
        List<Terminology> ung = terminologyMapper.selectList(uq);
        tree.setUngrouped(toVoList(ung));
        return tree;
    }

    public List<TerminologyEntryVO> listMine(Integer userId) {
        QueryWrapper<Terminology> qw = new QueryWrapper<>();
        qw.eq("owner_user_id", userId).orderByDesc("update_time").orderByDesc("id");
        return toVoList(terminologyMapper.selectList(qw));
    }

    public TerminologyMineGroupedVO listMineGrouped(Integer userId) {
        QueryWrapper<Terminology> qw = new QueryWrapper<>();
        qw.eq("owner_user_id", userId).orderByDesc("update_time").orderByAsc("id");
        List<Terminology> rows = terminologyMapper.selectList(qw);
        List<Terminology> ungroupedRows = new ArrayList<>();
        Map<Integer, List<Terminology>> byGid = new LinkedHashMap<>();
        for (Terminology t : rows) {
            if (t.getGroupId() == null) {
                ungroupedRows.add(t);
            } else {
                byGid.computeIfAbsent(t.getGroupId(), k -> new ArrayList<>()).add(t);
            }
        }
        TerminologyMineGroupedVO out = new TerminologyMineGroupedVO();
        out.setUngrouped(toVoList(ungroupedRows));
        Map<Integer, String> gmap = loadGroupNameMap(byGid.keySet());
        for (Map.Entry<Integer, List<Terminology>> e : byGid.entrySet()) {
            TerminologyGroupSectionVO sec = new TerminologyGroupSectionVO();
            sec.setGroupId(e.getKey());
            sec.setGroupName(gmap.getOrDefault(e.getKey(), "（未知组）"));
            sec.setEntries(toVoList(e.getValue()));
            out.getGroups().add(sec);
        }
        return out;
    }

    private Map<Integer, String> loadGroupNameMap(Set<Integer> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return Map.of();
        }
        List<TerminologyGroup> groups = terminologyGroupMapper.selectBatchIds(groupIds);
        Map<Integer, String> m = new HashMap<>();
        for (TerminologyGroup g : groups) {
            if (g != null && g.getId() != null) {
                m.put(g.getId(), g.getName() != null ? g.getName() : "");
            }
        }
        return m;
    }

    private List<TerminologyEntryVO> toVoList(List<Terminology> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = rows.stream().map(Terminology::getId).toList();
        QueryWrapper<TerminologyAlias> aw = new QueryWrapper<>();
        aw.in("terminology_id", ids).orderByAsc("id");
        List<TerminologyAlias> aliasEntities = terminologyAliasMapper.selectList(aw);
        Map<Integer, List<TerminologyAlias>> byTermId = aliasEntities.stream()
                .collect(Collectors.groupingBy(TerminologyAlias::getTerminologyId, LinkedHashMap::new, Collectors.toList()));
        Set<Integer> gids = rows.stream().map(Terminology::getGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, String> gmap = loadGroupNameMap(gids);
        List<TerminologyEntryVO> vos = new ArrayList<>();
        for (Terminology t : rows) {
            String gn = t.getGroupId() != null ? gmap.getOrDefault(t.getGroupId(), "（未知组）") : null;
            vos.add(toVo(t, byTermId.getOrDefault(t.getId(), List.of()), gn));
        }
        return vos;
    }

    private TerminologyEntryVO toVo(Terminology t, List<TerminologyAlias> aliasEntities, String groupName) {
        TerminologyEntryVO vo = new TerminologyEntryVO();
        vo.setId(t.getId());
        vo.setOwnerUserId(t.getOwnerUserId());
        vo.setGroupId(t.getGroupId());
        vo.setGroupName(groupName);
        vo.setTerm(t.getTerm());
        vo.setDefinition(t.getDefinition());
        vo.setSortWeight(t.getSortWeight());
        vo.setEnabled(t.getEnabled());
        vo.setClonedFromSystemId(t.getClonedFromSystemId());
        List<TerminologyAliasVO> aliasVos = new ArrayList<>();
        for (TerminologyAlias a : aliasEntities) {
            TerminologyAliasVO av = new TerminologyAliasVO();
            av.setAlias(a.getAlias());
            av.setTargetLang(a.getTargetLang());
            aliasVos.add(av);
        }
        vo.setAliases(aliasVos);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public TerminologyEntryVO createMine(Integer userId, TerminologySaveDTO dto) {
        Terminology t = new Terminology();
        t.setOwnerUserId(userId);
        t.setGroupId(null);
        t.setTerm(dto.getTerm().trim());
        t.setDefinition(dto.getDefinition().trim());
        t.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
        t.setSortWeight(dto.getSortWeight() != null ? dto.getSortWeight() : 0);
        LocalDateTime now = LocalDateTime.now();
        t.setCreateTime(now);
        t.setUpdateTime(now);
        t.setClonedFromSystemId(null);
        terminologyMapper.insert(t);
        saveAliases(t.getId(), dto.getAliases());
        terminologyCacheService.refreshUser(userId);
        return loadOneVo(t.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public TerminologyEntryVO updateMine(Integer userId, Integer id, TerminologySaveDTO dto) {
        Terminology existing = terminologyMapper.selectById(id);
        if (existing == null || existing.getOwnerUserId() == null || !existing.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("词条不存在或无权修改");
        }
        existing.setTerm(dto.getTerm().trim());
        existing.setDefinition(dto.getDefinition().trim());
        if (dto.getEnabled() != null) {
            existing.setEnabled(dto.getEnabled());
        }
        if (dto.getSortWeight() != null) {
            existing.setSortWeight(dto.getSortWeight());
        }
        existing.setUpdateTime(LocalDateTime.now());
        terminologyMapper.updateById(existing);
        terminologyAliasMapper.delete(new QueryWrapper<TerminologyAlias>().eq("terminology_id", id));
        saveAliases(id, dto.getAliases());
        terminologyCacheService.refreshUser(userId);
        return loadOneVo(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMine(Integer userId, Integer id) {
        Terminology existing = terminologyMapper.selectById(id);
        if (existing == null || existing.getOwnerUserId() == null || !existing.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("词条不存在或无权删除");
        }
        terminologyAliasMapper.delete(new QueryWrapper<TerminologyAlias>().eq("terminology_id", id));
        terminologyMapper.deleteById(id);
        terminologyCacheService.refreshUser(userId);
    }

    /**
     * 将勾选的系统词条与/或整组下的系统词条复制到「我的术语」。
     */
    @Transactional(rollbackFor = Exception.class)
    public List<TerminologyEntryVO> importSystemTermsToMine(Integer userId, TerminologyImportSystemDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        Set<Integer> toImport = new LinkedHashSet<>();
        if (dto.getSystemTerminologyIds() != null) {
            dto.getSystemTerminologyIds().stream().filter(Objects::nonNull).forEach(toImport::add);
        }
        if (dto.getSystemGroupIds() != null) {
            for (Integer gid : new HashSet<>(dto.getSystemGroupIds())) {
                if (gid == null) {
                    continue;
                }
                TerminologyGroup grp = terminologyGroupMapper.selectById(gid);
                if (grp == null || grp.getEnabled() == null || grp.getEnabled() != 1) {
                    continue;
                }
                QueryWrapper<Terminology> q = new QueryWrapper<>();
                q.isNull("owner_user_id").eq("enabled", 1).eq("group_id", gid);
                List<Terminology> lst = terminologyMapper.selectList(q);
                if (lst.size() > MAX_IMPORT_PER_GROUP) {
                    throw new IllegalArgumentException("单组一次最多导入 " + MAX_IMPORT_PER_GROUP + " 条术语");
                }
                for (Terminology x : lst) {
                    toImport.add(x.getId());
                }
            }
        }
        if (toImport.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一条系统术语或一个术语组");
        }
        QueryWrapper<Terminology> existingQ = new QueryWrapper<>();
        existingQ.eq("owner_user_id", userId).isNotNull("cloned_from_system_id");
        List<Terminology> existingClones = terminologyMapper.selectList(existingQ);
        Set<Integer> alreadySourceIds = existingClones.stream()
                .map(Terminology::getClonedFromSystemId)
                .collect(Collectors.toSet());
        List<TerminologyEntryVO> created = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Integer sid : toImport) {
            if (sid == null || alreadySourceIds.contains(sid)) {
                continue;
            }
            Terminology src = terminologyMapper.selectById(sid);
            if (src == null) {
                continue;
            }
            Integer en = src.getEnabled();
            if (src.getOwnerUserId() != null || en == null || en != 1) {
                continue;
            }
            Terminology t = new Terminology();
            t.setOwnerUserId(userId);
            t.setGroupId(src.getGroupId());
            t.setTerm(src.getTerm());
            t.setDefinition(src.getDefinition());
            t.setEnabled(1);
            t.setSortWeight(src.getSortWeight() != null ? src.getSortWeight() : 0);
            t.setClonedFromSystemId(sid);
            t.setCreateTime(now);
            t.setUpdateTime(now);
            terminologyMapper.insert(t);
            List<TerminologyAlias> srcAliases = terminologyAliasMapper.selectList(
                    new QueryWrapper<TerminologyAlias>().eq("terminology_id", sid).orderByAsc("id"));
            for (TerminologyAlias sa : srcAliases) {
                TerminologyAlias na = new TerminologyAlias();
                na.setTerminologyId(t.getId());
                na.setAlias(sa.getAlias());
                na.setTargetLang(sa.getTargetLang());
                terminologyAliasMapper.insert(na);
            }
            alreadySourceIds.add(sid);
            TerminologyEntryVO vo = loadOneVo(t.getId());
            if (vo != null) {
                created.add(vo);
            }
        }
        terminologyCacheService.refreshUser(userId);
        return created;
    }

    private void saveAliases(Integer terminologyId, List<TerminologyAliasItemDTO> aliases) {
        if (aliases == null) {
            return;
        }
        for (TerminologyAliasItemDTO item : aliases) {
            if (item == null || item.getAlias() == null || item.getAlias().isBlank()) {
                continue;
            }
            TerminologyAlias a = new TerminologyAlias();
            a.setTerminologyId(terminologyId);
            a.setAlias(item.getAlias().trim());
            String tl = item.getTargetLang();
            a.setTargetLang(tl != null && !tl.isBlank() ? tl.trim() : null);
            terminologyAliasMapper.insert(a);
        }
    }

    private TerminologyEntryVO loadOneVo(Integer id) {
        Terminology t = terminologyMapper.selectById(id);
        if (t == null) {
            return null;
        }
        List<TerminologyAlias> list = terminologyAliasMapper.selectList(
                new QueryWrapper<TerminologyAlias>().eq("terminology_id", id).orderByAsc("id"));
        String groupName = null;
        if (t.getGroupId() != null) {
            TerminologyGroup g = terminologyGroupMapper.selectById(t.getGroupId());
            groupName = g != null && g.getName() != null ? g.getName() : "（未知组）";
        }
        return toVo(t, list, groupName);
    }
}

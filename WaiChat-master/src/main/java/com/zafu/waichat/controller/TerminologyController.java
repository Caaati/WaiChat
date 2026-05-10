package com.zafu.waichat.controller;

import com.zafu.waichat.pojo.dto.TerminologyImportSystemDTO;
import com.zafu.waichat.pojo.dto.TerminologySaveDTO;
import com.zafu.waichat.pojo.vo.TerminologyEntryVO;
import com.zafu.waichat.pojo.vo.TerminologyMineGroupedVO;
import com.zafu.waichat.pojo.vo.TerminologySystemTreeVO;
import com.zafu.waichat.service.AuthUserService;
import com.zafu.waichat.service.TerminologyService;
import com.zafu.waichat.util.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/terminology")
@Slf4j
@Api(tags = "术语库")
public class TerminologyController {

    @Autowired
    private AuthUserService authUserService;
    @Autowired
    private TerminologyService terminologyService;

    @GetMapping("/system")
    public Result listSystem() {
        try {
            return Result.success(terminologyService.listSystemEntries());
        } catch (Exception e) {
            log.error("list system terminology", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/system/groups")
    public Result listSystemGroups() {
        try {
            TerminologySystemTreeVO tree = terminologyService.listSystemGroups();
            return Result.success(tree);
        } catch (Exception e) {
            log.error("list system terminology groups", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/mine")
    public Result listMine() {
        Integer uid = authUserService.getCurrentUserIdOrNull();
        if (uid == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(terminologyService.listMine(uid));
        } catch (Exception e) {
            log.error("list mine terminology", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/mine/grouped")
    public Result listMineGrouped() {
        Integer uid = authUserService.getCurrentUserIdOrNull();
        if (uid == null) {
            return Result.error("请先登录");
        }
        try {
            TerminologyMineGroupedVO data = terminologyService.listMineGrouped(uid);
            return Result.success(data);
        } catch (Exception e) {
            log.error("list mine terminology grouped", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/mine/import-system")
    public Result importFromSystem(@RequestBody TerminologyImportSystemDTO body) {
        Integer uid = authUserService.getCurrentUserIdOrNull();
        if (uid == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(terminologyService.importSystemTermsToMine(uid, body));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("import system terminology", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/mine")
    public Result create(@RequestBody TerminologySaveDTO dto) {
        Integer uid = authUserService.getCurrentUserIdOrNull();
        if (uid == null) {
            return Result.error("请先登录");
        }
        try {
            validateSave(dto);
            return Result.success(terminologyService.createMine(uid, dto));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("create terminology", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/mine/{id}")
    public Result update(@PathVariable Integer id, @RequestBody TerminologySaveDTO dto) {
        Integer uid = authUserService.getCurrentUserIdOrNull();
        if (uid == null) {
            return Result.error("请先登录");
        }
        try {
            validateSave(dto);
            return Result.success(terminologyService.updateMine(uid, id, dto));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("update terminology", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/mine/{id}")
    public Result delete(@PathVariable Integer id) {
        Integer uid = authUserService.getCurrentUserIdOrNull();
        if (uid == null) {
            return Result.error("请先登录");
        }
        try {
            terminologyService.deleteMine(uid, id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("delete terminology", e);
            return Result.error(e.getMessage());
        }
    }

    private void validateSave(TerminologySaveDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (dto.getTerm() == null || dto.getTerm().isBlank()) {
            throw new IllegalArgumentException("术语不能为空");
        }
        if (dto.getDefinition() == null || dto.getDefinition().isBlank()) {
            throw new IllegalArgumentException("释义不能为空");
        }
    }
}

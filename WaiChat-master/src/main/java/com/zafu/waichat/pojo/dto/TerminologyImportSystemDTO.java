package com.zafu.waichat.pojo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TerminologyImportSystemDTO {
    /** 系统词条 id 列表（terminology.owner_user_id 为 NULL 的条目） */
    private List<Integer> systemTerminologyIds = new ArrayList<>();
    /** 系统术语组 id：导入该组下全部启用的系统词条（受单组条数上限约束） */
    private List<Integer> systemGroupIds = new ArrayList<>();
}

package com.zafu.waichat.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TerminologyGroupSectionVO {
    private Integer groupId;
    private String groupName;
    private List<TerminologyEntryVO> entries = new ArrayList<>();
}

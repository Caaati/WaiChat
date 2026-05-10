package com.zafu.waichat.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TerminologyMineGroupedVO {
    private List<TerminologyEntryVO> ungrouped = new ArrayList<>();
    private List<TerminologyGroupSectionVO> groups = new ArrayList<>();
}

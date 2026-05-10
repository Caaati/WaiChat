package com.zafu.waichat.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TerminologySystemTreeVO {
    private List<TerminologyGroupSectionVO> groups = new ArrayList<>();
    private List<TerminologyEntryVO> ungrouped = new ArrayList<>();
}

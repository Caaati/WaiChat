package com.zafu.waichat.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("terminology_group")
public class TerminologyGroup {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String code;
    private Integer sortWeight;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.zafu.waichat.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("terminology_alias")
public class TerminologyAlias {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer terminologyId;
    private String alias;
}

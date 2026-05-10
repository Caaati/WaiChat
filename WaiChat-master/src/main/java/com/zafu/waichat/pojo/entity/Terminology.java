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
@TableName("terminology")
public class Terminology {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /** null = 系统全局 */
    private Integer ownerUserId;
    /** 所属系统术语组 id，可为 null */
    private Integer groupId;
    private String term;
    private String definition;
    private Integer enabled;
    private Integer sortWeight;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 从系统词条复制到用户词库时的源词条 id */
    private Integer clonedFromSystemId;
}

package com.zafu.waichat.pojo.dto;

import lombok.Data;

/** 个人资料修改（已登录会话内操作，不校验旧密码） */
@Data
public class UserProfileUpdateDTO {
    private String nickname;
    private String username;
    private String newPassword;
    private String confirmPassword;
}

package com.zafu.waichat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zafu.waichat.mapper.UserMapper;
import com.zafu.waichat.pojo.dto.UserProfileUpdateDTO;
import com.zafu.waichat.pojo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserAccountService {

    private static final int MIN_PASSWORD_LEN = 6;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthUserService authUserService;

    /**
     * @return 是否已使会话失效，需要重新登录
     */
    public boolean updateProfile(UserProfileUpdateDTO dto, HttpServletRequest request) {
        User user = authUserService.getCurrentUserEntityOrNull();
        if (user == null) {
            throw new IllegalArgumentException("未登录或会话已失效");
        }

        String nickname = dto.getNickname() == null ? "" : dto.getNickname().trim();
        String username = dto.getUsername() == null ? "" : dto.getUsername().trim();
        if (!StringUtils.hasText(nickname)) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        boolean usernameChanged = !username.equals(user.getUsername());

        String newPwd = dto.getNewPassword() == null ? "" : dto.getNewPassword();
        String confirmPwd = dto.getConfirmPassword() == null ? "" : dto.getConfirmPassword();
        boolean passwordChanging = StringUtils.hasText(newPwd);

        if (passwordChanging) {
            if (!newPwd.equals(confirmPwd)) {
                throw new IllegalArgumentException("两次输入的新密码不一致");
            }
            if (newPwd.length() < MIN_PASSWORD_LEN) {
                throw new IllegalArgumentException("新密码长度至少 " + MIN_PASSWORD_LEN + " 位");
            }
        }

        if (usernameChanged) {
            Long cnt = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, username)
                            .ne(User::getId, user.getId()));
            if (cnt != null && cnt > 0) {
                throw new IllegalArgumentException("该用户名已被占用");
            }
        }

        user.setNickname(nickname);
        if (usernameChanged) {
            user.setUsername(username);
        }
        if (passwordChanging) {
            user.setPassword(passwordEncoder.encode(newPwd));
        }
        userMapper.updateById(user);

        if (usernameChanged || passwordChanging) {
            request.getSession().invalidate();
            return true;
        }
        return false;
    }
}

package com.zafu.waichat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zafu.waichat.mapper.UserMapper;
import com.zafu.waichat.pojo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthUserService {

    @Autowired
    private UserMapper userMapper;

    public Integer getCurrentUserIdOrNull() {
        User user = getCurrentUserEntityOrNull();
        return user != null ? user.getId() : null;
    }

    /** 当前登录用户实体；未登录返回 null */
    public User getCurrentUserEntityOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }
}

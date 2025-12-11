// src/main/java/com/zafu/waichat/service/impl/ChatServiceImpl.java
package com.zafu.waichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zafu.waichat.mapper.ChatMapper;
import com.zafu.waichat.pojo.dto.UserContactDTO;
import com.zafu.waichat.pojo.entity.Chat;
import com.zafu.waichat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements ChatService {

    @Autowired
    private ChatMapper chatMapper;

    @Override
    public List<Chat> getChatHistory(Long userId1, Long userId2) {
        return chatMapper.selectChatHistory(userId1, userId2);
    }

    @Override
    public boolean saveChatMessage(Chat chat) {
        return this.save(chat);
    }

    @Override
    public List<UserContactDTO> getContactList(Long userId) {
        return chatMapper.selectContactList(userId);
    }

    @Override
    public void removeHistory(Integer userId, Integer targetId) {
        int update = chatMapper.updateStatus(userId, targetId,0);
        log.info("成功更新{}行数据", update);
    }

    @Override
    public void recoverHistory(Integer userId, Integer targetId) {
        int update = chatMapper.updateStatus(userId, targetId,1);
        log.info("成功更新{}行数据", update);
    }
}

// src/main/java/com/zafu/waichat/service/impl/ChatServiceImpl.java
package com.zafu.waichat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zafu.waichat.mapper.ChatMapper;
import com.zafu.waichat.pojo.dto.UserContactDTO;
import com.zafu.waichat.pojo.entity.Chat;
import com.zafu.waichat.service.ChatService;
import com.zafu.waichat.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.zafu.waichat.util.StringUtil.getChatKey;

@Service
@Slf4j
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements ChatService {

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Chat> getChatHistory(Long userId1, Long userId2) {
        String redisKey = getChatKey(String.valueOf(userId1), String.valueOf(userId2));

        List<String> rawMsgs;
        try {
            rawMsgs = redisUtil.lRange(redisKey, 0, -1);
        } catch (DataAccessException e) {
            log.warn("Redis lRange 失败 (DataAccess)，降级 MySQL，key={}: {}", redisKey, e.getMessage());
            return loadHistoryFromDbAndMaybeBackfill(redisKey, userId1, userId2);
        } catch (Exception e) {
            log.warn("Redis lRange 失败，降级 MySQL，key={}: {}", redisKey, e.toString());
            return loadHistoryFromDbAndMaybeBackfill(redisKey, userId1, userId2);
        }

        if (rawMsgs != null && !rawMsgs.isEmpty()) {
            log.info("从 Redis 加载热数据");
            return rawMsgs.stream()
                    .map(s -> {
                        try {
                            return objectMapper.readValue(s, Chat.class);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Chat::getCreateTime))
                    .toList();
        }

        log.info("Redis 无热数据，从 MySQL 加载");
        return loadHistoryFromDbAndMaybeBackfill(redisKey, userId1, userId2);
    }

    private List<Chat> loadHistoryFromDbAndMaybeBackfill(String redisKey, Long userId1, Long userId2) {
        List<Chat> dbHistory = chatMapper.selectChatHistory(userId1, userId2);
        if (dbHistory.isEmpty()) {
            return dbHistory;
        }
        try {
            List<Chat> hotPart = dbHistory.size() > 50
                    ? dbHistory.subList(dbHistory.size() - 50, dbHistory.size())
                    : dbHistory;
            for (Chat c : hotPart) {
                try {
                    redisUtil.lPush(redisKey, objectMapper.writeValueAsString(c));
                } catch (Exception e) {
                    log.warn("Redis lPush 回填单条跳过: {}", e.getMessage());
                }
            }
            redisUtil.expire(redisKey, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 回填热数据跳过，不影响返回 MySQL 结果: {}", e.getMessage());
        }
        return dbHistory;
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
        int update = chatMapper.updateStatus(userId, targetId, 0);
        log.info("成功更新{}行数据", update);
    }

    @Override
    public void recoverHistory(Integer userId, Integer targetId) {
        int update = chatMapper.updateStatus(userId, targetId, 1);
        log.info("成功更新{}行数据", update);
    }
}

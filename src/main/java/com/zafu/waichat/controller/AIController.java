package com.zafu.waichat.controller;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zafu.waichat.mapper.LanguageMapper;
import com.zafu.waichat.pojo.dto.ChatDTO;
import com.zafu.waichat.pojo.dto.PolishDTO;
import com.zafu.waichat.pojo.dto.TranslateDTO;
import com.zafu.waichat.pojo.entity.Chat;
import com.zafu.waichat.pojo.entity.Language;
import com.zafu.waichat.pojo.vo.TranslateVO;
import com.zafu.waichat.util.MessageUtil;
import com.zafu.waichat.util.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@Slf4j
@Api(tags = "AI相关接口")
public class AIController {
    @Autowired
    private LanguageMapper LanguageMapper;

    @GetMapping("/languages")
    public Result getLanguages() {
        try {
            List<Language> languages = LanguageMapper.selectList(new QueryWrapper<>());
            languages.forEach(l->{
//                l.setName(l.getChineseName());
                l.setName(l.getDisplayName());
            });
            return Result.success(languages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/translate")
    public Result translate(@RequestBody TranslateDTO translateDTO) {
        try {
            GenerationResult back = MessageUtil.translateWithTarget(translateDTO.getText(), translateDTO.getTarget());
            String result = back.getOutput().getChoices().get(0).getMessage().getContent();
            TranslateVO vo = new TranslateVO();
            vo.setTranslated(result);
            vo.setOriginal(translateDTO.getText());
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/polish")
    public Result polish(@RequestBody PolishDTO polishDTO) {
        try {
            String text = polishDTO.getText();
            String style = polishDTO.getStyle();
            String sys = "你是一个聊天辅助工具，用于对用户输入的草稿内容进行润色或语气调整，只输出润色或语气调整后的文本，不要包含任何额外说明。当前选取的风格：";
            if (style.equals("business"))
                sys += "商务风格";
            else if (style.equals("casual"))
                sys += "休闲风格";
            else
                sys += "未指定,自由发挥";
            GenerationResult back = MessageUtil.callWithMessageNormal(sys, text);
            String result = back.getOutput().getChoices().get(0).getMessage().getContent();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/smartReply")
    public Result smartReply(@RequestBody List<ChatDTO> chats) {
        try {
            String sys = "你是一个聊天智能回复助手，用户将输入历史聊天记录，请模仿最近一条消息的发送者（即“我”）的语气风格，输出最合适的下一条回复内容。回复内容必须简短且直接，只输出回复文本，不要包含任何额外说明。";
            StringBuilder historyText = new StringBuilder();
            chats.forEach(item -> {
                // 使用 userId 来区分发言者，并以 "发言者: 内容\n" 的形式拼装
                // 示例格式: 我: 2222\n对方: 6666666666\n
                String sender = item.getUserId();
                String content = item.getContent();
                if (sender != null && content != null) {
                    historyText.append(sender).append(": ").append(content).append("\n");
                }
            });
            // 添加回复指令或标识
            historyText.append("我: ");
            // 完整的大模型输入文本 (User Prompt)
            String userPrompt = historyText.toString();
            // 4. 调用大模型
            GenerationResult back = MessageUtil.callWithMessageNormal(sys, userPrompt);
            // 解析并返回结果
            String result = back.getOutput().getChoices().get(0).getMessage().getContent().trim();

            return Result.success(result);

        } catch (Exception e) {
            // 记录详细错误信息
            e.printStackTrace();
            return Result.error("智能回复生成失败: " + e.getMessage());
        }
    }
}

package com.zafu.waichat;

import com.zafu.waichat.util.MessageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.lang.System;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

@SpringBootTest
class WaiChatApplicationTests {
    public static void main(String[] args) {
        try {
            String sys = "You are a helpful assistant.";
            String user = "这是一个测试！";
            long startTime = System.currentTimeMillis();
            GenerationResult result = MessageUtil.translateWithTarget(user,"en");
            long endTime = System.currentTimeMillis();
            System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent());
            System.out.println("耗时" + (endTime - startTime) + " milliseconds.");

            startTime = System.currentTimeMillis();
            GenerationResult result1 = MessageUtil.translateWithModel(user,"zh_tw","qwen-mt-flash");
            endTime = System.currentTimeMillis();
            System.out.println(result1.getOutput().getChoices().get(0).getMessage().getContent());
            System.out.println("耗时" + (endTime - startTime) + " milliseconds.");

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("错误信息："+e.getMessage());
            System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
        }
        System.exit(0);
    }

}

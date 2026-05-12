package com.zafu.waichat.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 降低长空闲后 Redis 连接被中间设备回收导致的读失败概率：开启 TCP keepAlive。
 */
@Configuration
public class RedisLettuceClientConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return builder -> {
            SocketOptions socketOptions = SocketOptions.builder()
                    .keepAlive(true)
                    .tcpNoDelay(true)
                    .build();
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(socketOptions)
                    .autoReconnect(true)
                    .build();
            builder.clientOptions(clientOptions);
        };
    }
}

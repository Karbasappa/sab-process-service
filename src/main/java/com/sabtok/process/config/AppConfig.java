package com.sabtok.process.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer clientConfigurationBuilderCustomizer() {
        return builder -> builder.clientOptions(ClientOptions.builder()
                // 1. Disable the "Disconnected" buffer so it fails immediately
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                // 2. Control how many times it tries to reconnect
                .socketOptions(SocketOptions.builder().connectTimeout(Duration.ofMillis(500)).build())
                .build());
    }

}

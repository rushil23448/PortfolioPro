package com.hsbc.portfolio.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with OpenAI.
 * Set app.openai.api-key in application.properties or env variable.
 * API key is optional - sentiment analysis will use defaults if not set.
 */
@Configuration
public class SpringAiConfig {

    @Value("${app.openai.api-key:}")
    private String openAiApiKey;

    @Value("${app.openai.chat.options.model:gpt-3.5-turbo}")
    private String model;

    @Value("${app.openai.chat.options.temperature:0.3}")
    private double temperature;

    @Bean
    @ConditionalOnProperty(name = "app.openai.api-key", havingValue = "true", matchIfMissing = false)
    public OpenAiChatModel openAiChatModel() {
        if (openAiApiKey == null || openAiApiKey.isBlank() || 
            openAiApiKey.startsWith("${") || openAiApiKey.equals("your-openai-api-key-here")) {
            // Don't create bean if no valid API key
            return null;
        }
        OpenAiApi openAiApi = new OpenAiApi(openAiApiKey);
        return new OpenAiChatModel(openAiApi, OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .build());
    }
}


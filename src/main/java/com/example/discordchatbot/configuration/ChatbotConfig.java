package com.example.discordchatbot.configuration;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@Configuration
public class ChatbotConfig {
    @Value("${discord.token}")
    private String token;

    @Bean
    public Mono<GatewayDiscordClient> gatewayDiscordClient() { // GatewayDiscordClient used to connect to Discord
                                                                // Mono like promise in JavaScript, used for asynchronous programming
        return DiscordClientBuilder.create(token)
                .build()
                .login()
                .doOnError(error -> {
                    System.err.println("Failed to login to Discord: " + error.getMessage());
                });
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
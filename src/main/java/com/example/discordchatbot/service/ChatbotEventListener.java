package com.example.discordchatbot.service;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class ChatbotEventListener {
    private final WeatherService weatherService;

    @Autowired
    public ChatbotEventListener(WeatherService weatherService, Mono<GatewayDiscordClient> clientMono) {
        this.weatherService = weatherService;
        clientMono.publishOn(Schedulers.boundedElastic()).doOnNext(client -> client.on(MessageCreateEvent.class)
                .flatMap(this::handleMessage)
                .subscribe()
        ).subscribe();
    }

    private Mono<Void> handleMessage(MessageCreateEvent event) {
        return event.getMessage().getAuthor()
                .filter(user -> !user.isBot())
                .map(
                        user -> {
                            String question = event.getMessage().getContent();

                            String response = weatherService.getWeather(question);

                            return event.getMessage().getChannel()
                                    .flatMap(channel -> channel.createMessage(response))
                                    .then();
                        }).orElse(Mono.empty());
    }
}
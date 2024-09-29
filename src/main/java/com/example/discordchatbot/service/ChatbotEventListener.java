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

    public Command parseCommand(String content) {
        if (content.startsWith("!weather")) {
            return Command.WEATHER;
        } else if (content.startsWith("!help")) {
            return Command.HELP;
        } else if (content.startsWith("!alert")) {
            return Command.ALERT;
        } else {
            return Command.UNKNOWN;
        }
    }

    private Mono<Void> handleMessage(MessageCreateEvent event) {
        return event.getMessage().getAuthor()
                .filter(user -> !user.isBot())
                .map (
            user -> {
                String content = event.getMessage().getContent();
                System.out.println("Received message: " + content);

                Command command = parseCommand(content);

                String finalResponse = switch (command) {
                    case WEATHER -> weatherService.handleWeather(content);
                    case HELP -> weatherService.handleHelp();
                    case ALERT -> weatherService.handleAlert(content);
                    default -> weatherService.handleUnknown();
                };
                return event.getMessage().getChannel()
                        .flatMap(channel -> channel.createMessage(finalResponse))
                        .then();
            }).orElse(Mono.empty());
    }
}
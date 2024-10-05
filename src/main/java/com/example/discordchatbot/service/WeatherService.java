package com.example.discordchatbot.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    @Value("${weather.api}")
    private String WEATHER_API_KEY;
    @Value("${gemini.api-key}")
    private String GEMINI_API_KEY;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AIService aiService;

    @Autowired
    WeatherService(AIService aiService) {
        this.aiService = aiService;
    }

    // Handle weather requests
    public String getWeather(String question) {
        // Extract question from user input
        if (question == null || question.isBlank()) {
            return "Please provide a valid weather question.";
        }
        logger.info("Question: " + question);

        // Generate URL for weather API
        String aiResponseUrl = aiService.getUrl(question, GEMINI_API_KEY);

        logger.info("AI Response URL: " + aiResponseUrl);

        // Replace <API_KEY> to actual API KEY
        String apiUrl = aiResponseUrl.replace("<API_KEY>", WEATHER_API_KEY);

        logger.info("API URL: " + apiUrl);

        // Make API call to get weather data
        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            // already have the data from weather api
            // generate answer based on the data and question
            return aiService.getUserResponse(Objects.requireNonNull(response.getBody()), question, GEMINI_API_KEY);

        } else {
            return "Failed to get weather data. Please try again later.";
        }
    }

}

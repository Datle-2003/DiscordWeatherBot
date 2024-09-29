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

    public String handleUnknown() {
        return "Unknown command, please type !help for more information.";
    }

    public String handleAlert(String content) {
        // Extract location from the content
        String location = extractLocation(content);
        if (location == null) {
            return "Please provide a location for alerts.";
        }

        logger.info("Location: " + location);

        // Build the URL with alerts parameter set to 'yes'
        String url = String.format("http://api.weatherapi.com/v1/forecast.json?key=%s&q=%s&days=1&alerts=yes", WEATHER_API_KEY, location);

        // Make API call to get weather alerts
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        logger.info("API Response: " + response);

        if (response.getStatusCode() == HttpStatus.OK) {
            // Process the API response to extract relevant alert information
            return aiService.getResponse(Objects.requireNonNull(response.getBody()), "What are the weather alerts for " + location + "? If this data doesn't contain any weather alerts for that location, please response \"No weather alerts for + location.\"", GEMINI_API_KEY);
        } else {
            return "Error fetching weather alerts for " + location;
        }
    }

    public String handleHelp() {
        return """
                Available commands:
                - !weather <question> - Get current weather.
                   Example: !weather How's the weather in Hanoi today?
                - !help - Get all information about command
                - !alert <location> - Get storm or weather alerts for a location.
                   Example: !alert Ha Noi""";
    }

    // Handle weather requests
    public String handleWeather(String content) {
        // Extract question from user input
        String question = extractQuestion(content);
        if (question == null) {
            return "Please provide a valid weather question.";
        }

        logger.info("Question: " + question);

        // Build the AI request to generate the URL
        String aiResponseUrl = aiService.getUrl(question, GEMINI_API_KEY);

        logger.info("AI Response URL: " + aiResponseUrl);

        // The generated URL should contain the API key in place of <API_KEY>
        String apiUrl = aiResponseUrl.replace("<API_KEY>", WEATHER_API_KEY);

        logger.info("API URL: " + apiUrl);

        // Make API call to get weather data
        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String data = aiService.getResponse(Objects.requireNonNull(response.getBody()), question, GEMINI_API_KEY);
            return aiService.getResponse(data, question, GEMINI_API_KEY);
        } else {
            return "Error fetching weather for " + extractLocation(question);
        }
    }

    private String extractLocation(String content) {
        // Assuming location is the first word after the command in the user's content
        String[] parts = content.split(" ");
        StringBuilder location = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            location.append(parts[i]).append(" ");
        }
        return location.toString().trim();
    }

    private String extractQuestion(String content) {
        // Remove the command prefix, remove all newline characters, and return the rest as the question
        return content.startsWith("!weather")
                ? content.replace("!weather", "").replace("\n", "").trim()
                : null;
    }


}

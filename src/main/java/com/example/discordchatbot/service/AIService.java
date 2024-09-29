package com.example.discordchatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate restTemplate;

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUrl(String question, String GEMINI_API_KEY) {
        String prompt = """
                This is the documentation for WeatherAPI.com. Please generate the URL based on a user's question regarding weather. You will need to use the following base URL and API methods, as well as the parameters listed below.
                Base URL:
                http://api.weatherapi.com/v1
                API Methods:
                    Current weather: /current.json or /current.xml
                    Forecast: /forecast.json or /forecast.xml
                    Search or Autocomplete: /search.json or /search.xml
                    History: /history.json or /history.xml
                    Marine: /marine.json or /marine.xml
                    Future: /future.json or /future.xml
                    Time Zone: /timezone.json or /timezone.xml
                    Sports: /sports.json or /sports.xml
                    Astronomy: /astronomy.json or /astronomy.xml
                    IP Lookup: /ip.json or /ip.xml
                Request Parameters:
                    key: Required | Your API key.
                    q: Required | Query parameter for location (e.g., city name like q=Paris, lat/lon, zip code, metar code, iata code, IP, or ID).
                    days: Required for the forecast API (e.g., days=3). Number of forecast days (1-14). If omitted, only today's weather is returned.
                    dt: Required for the History and Future APIs (date in yyyy-MM-dd format, for history it must be on or after Jan 1, 2010, and for future between 14 and 300 days from today).
                    unixdt: Optional | Unix timestamp for Forecast and History APIs. Either dt or unixdt, not both.
                    end_dt: Optional | For History API, to limit results to a specific range (only available for Pro plan).
                    unixend_dt: Optional | Unix timestamp for History API.
                    hour: Optional | To limit forecast or history data to a specific hour (24-hour format).
                    alerts: Optional | For forecast API, enable/disable alerts (alerts=yes or alerts=no).
                    aqi: Optional | For forecast API, enable/disable Air Quality data (aqi=yes or aqi=no).
                    tides: Optional | For Marine API, enable/disable Tide data (tides=yes or tides=no).
                    tp: Optional | For Forecast and History API, 15-minute interval data (tp=15, for Enterprise clients).
                    current_fields: Optional | Specify comma-separated fields for the current weather element (e.g., current_fields=temp_c,wind_mph).
                    day_fields: Optional | Specify comma-separated fields for day element in forecast/history (e.g., day_fields=temp_c,wind_mph).
                    hour_fields: Optional | Specify comma-separated fields for hourly data (e.g., hour_fields=temp_c,wind_mph).
                    solar: Optional | Enable solar irradiance data for History API (Enterprise clients only).
                For example, to answer the question 'What is the weather in Ha Noi for 3 days from now?', you would generate the following URL:
                http://api.weatherapi.com/v1/forecast.json?key=<API_KEY>&q=Ha%20Noi&days=3
                Please respond with the complete URL according to the user's question, including the correct base URL, API method, and parameters. Only respond with valid URL and nothing else:
                """;
        String request = prompt + question;

        return callAI(request, GEMINI_API_KEY);
    }

    private String callAI(String request, String GEMINI_API_KEY) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // JSON payload with the user's question
        String jsonPayload = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                request
        );
        logger.info("GEMINI_API_KEY: " + GEMINI_API_KEY);


        String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
        String fullApiUrl = String.format("%s?key=%s", API_URL, GEMINI_API_KEY);

        logger.info("API URL: " + fullApiUrl);

        // Create the HTTP request with the headers and body
        HttpEntity<String> httpRequest = new HttpEntity<>(jsonPayload, headers);

        logger.info("Request: " + httpRequest);

        // Make the POST request to the AI API
        ResponseEntity<String> response = restTemplate.exchange(fullApiUrl, HttpMethod.POST, httpRequest, String.class);

        logger.info("Response: " + response);

        // Check if the response was successful
        if (response.getStatusCode() == HttpStatus.OK) {
            return parseBody(response);
        } else {
            return "Error: " + response.getStatusCode();
        }
    }

    public String parseBody(ResponseEntity<String> response){
        String responseBody = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String url = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            logger.info("URL: " + url);
            return url;
        } catch (Exception e) {
            logger.error("Error parsing response body: " + e.getMessage());
        }

        return null;
    }

    public String getResponse(String data, String question, String GEMINI_API_KEY) {
        logger.info("question to ai: " + question);

        // Escape special characters in the question and data strings
        String escapedQuestion = escapeJsonString(question);
        String escapedData = escapeJsonString(data);

        // Format the JSON payload
        String questionToAI = String.format(
                "With this data, please briefly answer the question: %s. Data: %s",
                escapedQuestion, escapedData
        );

        logger.info("Question to AI: " + questionToAI);

        return callAI(questionToAI, GEMINI_API_KEY);
    }

    private String escapeJsonString(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}

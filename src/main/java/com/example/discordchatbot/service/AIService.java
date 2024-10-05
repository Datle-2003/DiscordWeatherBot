package com.example.discordchatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
This is the documentation for WeatherAPI.com. Please generate the URL based on a user's question regarding weather. You will need to use the following base URL, API methods, and parameters listed below.
Base URL:
https://api.weatherapi.com/v1
API Methods (Available in Free Plan):
    - Current Weather: /current.json
      Description: Fetches current weather data for a specified location.
      Parameters:
        - key: Your API key (required).
        - q: Location query (city name, zip code, coordinates).
        - lang: Language code for response (optional).
        - aqi: Set to yes to include air quality data (optional).
    - Forecast: /forecast.json
      Description: Provides weather forecasts for up to 14 days for a given location.
      Parameters:
        - key: Your API key (required).
        - q: Location query.
        - days: Number of forecast days (1-14). Required if a forecast is requested for more than one day.
        - lang: Language code for response (optional).
        - alerts: Include weather alerts (alerts=yes or alerts=no, optional).
        - aqi: Include air quality data (aqi=yes or aqi=no, optional).
    - Astronomy: /astronomy.json
      Description: Provides information about astronomical data (sunrise, sunset, moon phase, etc.) for a specific date.
      Parameters:
        - key: Your API key (required).
        - q: Location query.
        - dt: Date in yyyy-MM-dd format (optional, defaults to today).
    - Time Zone: /timezone.json
      Description: Returns the time zone details for a given location.
      Parameters:
        - key: Your API key (required).
        - q: Location query.
    - Search/Autocomplete: /search.json
      Description: Provides location search and autocomplete functionality.
      Parameters:
        - key: Your API key (required).
        - q: Partial location query to return suggestions.
Request Parameters:
    - key: Required | Your API key.
    - q: Required | Query parameter for location (e.g., city name, latitude/longitude, zip code).
    - days: Required for the forecast API when requesting more than today's weather (e.g., days=3).
    - dt: Required for Astronomy API to specify a date (format yyyy-MM-dd, optional).
    - alerts: Optional | For forecast API, enable/disable alerts (alerts=yes or alerts=no).
    - aqi: Optional | For current and forecast APIs, enable/disable Air Quality data (aqi=yes or aqi=no).
    - lang: Optional | For multilingual responses (e.g., lang=fr for French).
Example User Questions and Corresponding URL:
    - User Question: "What is the weather in Ha Noi for 3 days from now?"
      Generated URL: https://api.weatherapi.com/v1/forecast.json?key=<API_KEY>&q=Ha%20Noi&days=3&alerts=yes&aqi=yes
Please respond with the complete URL according to the user's question, including the correct base URL, API method, and parameters. Only respond with a valid URL and nothing else.
""";


        String request = prompt + question;

        logger.info("Request to AI: " + request);

        return callAI(request, GEMINI_API_KEY);
    }

    private String callAI(String request, String GEMINI_API_KEY) {
        // reformat the request
        request = formatString(request);

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
            return parseAIBody(response);
        } else {
            return "Error: " + response.getStatusCode();
        }
    }

    // Parse the body to get text from the LLM model
    public String parseAIBody(ResponseEntity<String> response){
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

    // Get the response based on the data and question
    public String getUserResponse(String data, String question, String GEMINI_API_KEY) {
        logger.info("question to ai: " + question);

        String questionToAI = String.format(
                "With this data, please answer the question: %s. Data: %s. Based on the weather conditions, provide some recommendations for the user, such as wearing a mask, hat, or other appropriate actions (if available).",
                question, data
        );

        logger.info("Question to AI: " + questionToAI);

        return callAI(questionToAI, GEMINI_API_KEY);
    }

    // the data from the weather api is already in json format
    // so when add it to the question, we need to escape the special characters
    // eg: "\?" -> "\\\?"
    private String formatString(String input) {
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

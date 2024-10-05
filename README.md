# Discord Weather Bot

## 1. About the Project

The **Discord Weather Bot** is a chatbot that answers real-time weather-related questions using the Weather API and Gemini API.

## 2. Installation

### Prerequisites

Before you begin, ensure you have:

- [Gemini API Key](https://ai.google.dev/gemini-api/docs/api-key)
- [Weather API Key](https://www.weatherapi.com/docs/)
- [Discord Bot Token](https://discord.com/developers/applications?new_application=true)

### Installation Steps

1. **Clone the Repository**
    ```bash
    git clone https://github.com/Datle-2003/DiscordWeatherBot.git
    ```

2. **Run the Application**
    ```bash
    cd DiscordWeatherBot
    
   # build with gradle
    ./gradlew build 
    java -jar build/libs/discord-bot.jar
    ```

## 3. Usage
To add this bot to your server, you must paste the Discord bot link into your server.

Once added, start the application and interact with the Discord chatbot. You can ask it weather-related questions like:

      "What is the weather in Ha Noi today?"

The chatbot will respond with relevant weather information.
## 4. Configuration
- `WEATHER_API_KEY`: The API key for WeatherAPI.
- `GEMINI_API_KEY`: The API key for the Google Gemini language model.
- `DISCORD_TOKEN`: The token for your Discord bot.

You must set these values in the `application.properties` file.
```properties
weather.api=YOUR_WEATHER_API_KEY
gemini.api-key=YOUR_GEMINI_API_KEY
discord.token=YOUR_DISCORD_BOT_TOKEN

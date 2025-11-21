"""Weather tool (mock implementation)"""
from .base_tool import BaseTool
import random


class WeatherTool(BaseTool):
    """Mock weather tool that returns simulated weather data"""

    def __init__(self):
        super().__init__(
            name="weather",
            description="Gets current weather information for a given location. Input should be a city name."
        )

    def execute(self, input_data: str) -> str:
        """Execute weather query"""
        self._log_call()

        location = input_data.strip()

        # Generate mock weather data
        temperature = random.randint(-10, 35)
        conditions = random.choice(["Sunny", "Cloudy", "Rainy", "Partly Cloudy", "Windy"])
        humidity = random.randint(30, 90)
        wind_speed = random.randint(0, 30)

        return f"Weather in {location}: {conditions}, {temperature}°C, Humidity: {humidity}%, Wind: {wind_speed} km/h"

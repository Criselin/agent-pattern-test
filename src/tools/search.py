"""Search tool (mock implementation)"""
from .base_tool import BaseTool
import random


class SearchTool(BaseTool):
    """Mock search tool that returns simulated search results"""

    def __init__(self):
        super().__init__(
            name="search",
            description="Searches for information on a given topic. Returns relevant information (simulated)."
        )
        self.knowledge_base = {
            "python": "Python is a high-level, interpreted programming language known for its simplicity and readability.",
            "ai": "Artificial Intelligence (AI) refers to the simulation of human intelligence in machines.",
            "machine learning": "Machine Learning is a subset of AI that enables systems to learn from data.",
            "agent": "An AI agent is a software entity that can perceive its environment and take actions.",
            "react": "ReAct is an agent pattern that combines reasoning and acting in an interleaved manner.",
            "weather": "Weather information varies by location. Current conditions include temperature, humidity, and precipitation."
        }

    def execute(self, input_data: str) -> str:
        """Execute search"""
        self._log_call()

        query = input_data.lower().strip()

        # Check knowledge base
        for key, value in self.knowledge_base.items():
            if key in query:
                return f"Search results for '{input_data}': {value}"

        # Generic response
        return f"Search results for '{input_data}': Found {random.randint(10, 100)} results. Here are some key points: {query} is a topic with multiple aspects. Further research recommended."

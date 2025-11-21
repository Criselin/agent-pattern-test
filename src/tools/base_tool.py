"""Base Tool class"""
from abc import ABC, abstractmethod
from typing import Any


class BaseTool(ABC):
    """Base class for all tools"""

    def __init__(self, name: str, description: str):
        self.name = name
        self.description = description
        self.call_count = 0

    @abstractmethod
    def execute(self, input_data: str) -> Any:
        """Execute the tool with given input"""
        pass

    def _log_call(self):
        """Log tool call"""
        self.call_count += 1

    def get_stats(self) -> dict:
        """Get tool usage statistics"""
        return {
            "name": self.name,
            "call_count": self.call_count
        }

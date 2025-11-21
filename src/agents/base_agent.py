"""Base Agent class for all agent implementations"""
from abc import ABC, abstractmethod
from typing import Dict, List, Any, Optional
from dataclasses import dataclass, field
from datetime import datetime


@dataclass
class Message:
    """Represents a message in the conversation"""
    role: str  # "system", "user", "assistant"
    content: str
    timestamp: datetime = field(default_factory=datetime.now)
    metadata: Dict[str, Any] = field(default_factory=dict)


@dataclass
class AgentResponse:
    """Response from an agent"""
    content: str
    reasoning: Optional[str] = None
    actions_taken: List[str] = field(default_factory=list)
    tool_calls: List[Dict[str, Any]] = field(default_factory=list)
    metadata: Dict[str, Any] = field(default_factory=dict)
    success: bool = True
    error: Optional[str] = None


class BaseAgent(ABC):
    """Base class for all agents"""

    def __init__(
        self,
        name: str,
        llm_client,
        system_prompt: Optional[str] = None,
        tools: Optional[List[Any]] = None
    ):
        self.name = name
        self.llm_client = llm_client
        self.tools = tools or []
        self.system_prompt = system_prompt or self._default_system_prompt()
        self.conversation_history: List[Message] = []
        self.stats = {
            "total_queries": 0,
            "successful_queries": 0,
            "failed_queries": 0,
            "total_tool_calls": 0
        }

    def _default_system_prompt(self) -> str:
        """Default system prompt for the agent"""
        return f"You are {self.name}, a helpful AI assistant."

    def add_message(self, role: str, content: str, metadata: Optional[Dict] = None):
        """Add a message to conversation history"""
        message = Message(
            role=role,
            content=content,
            metadata=metadata or {}
        )
        self.conversation_history.append(message)

    @abstractmethod
    def process(self, user_input: str, **kwargs) -> AgentResponse:
        """
        Process user input and return a response.
        This must be implemented by each agent pattern.
        """
        pass

    def reset(self):
        """Reset the agent's conversation history"""
        self.conversation_history = []

    def get_stats(self) -> Dict[str, Any]:
        """Get agent statistics"""
        return self.stats.copy()

    def _update_stats(self, success: bool, tool_calls: int = 0):
        """Update agent statistics"""
        self.stats["total_queries"] += 1
        if success:
            self.stats["successful_queries"] += 1
        else:
            self.stats["failed_queries"] += 1
        self.stats["total_tool_calls"] += tool_calls

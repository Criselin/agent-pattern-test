"""LLM Client wrapper for different providers"""
import os
from typing import List, Dict, Any, Optional
from dotenv import load_dotenv

load_dotenv()


class LLMClient:
    """Unified client for different LLM providers"""

    def __init__(
        self,
        provider: str = "openai",
        model: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000
    ):
        self.provider = provider.lower()
        self.temperature = temperature
        self.max_tokens = max_tokens

        if provider == "openai":
            import openai
            self.client = openai.OpenAI(api_key=os.getenv("OPENAI_API_KEY"))
            self.model = model or os.getenv("DEFAULT_MODEL", "gpt-4")
        elif provider == "anthropic":
            import anthropic
            self.client = anthropic.Anthropic(api_key=os.getenv("ANTHROPIC_API_KEY"))
            self.model = model or "claude-3-sonnet-20240229"
        else:
            raise ValueError(f"Unsupported provider: {provider}")

    def chat(
        self,
        messages: List[Dict[str, str]],
        **kwargs
    ) -> str:
        """Send a chat completion request"""
        try:
            if self.provider == "openai":
                response = self.client.chat.completions.create(
                    model=self.model,
                    messages=messages,
                    temperature=kwargs.get("temperature", self.temperature),
                    max_tokens=kwargs.get("max_tokens", self.max_tokens)
                )
                return response.choices[0].message.content

            elif self.provider == "anthropic":
                # Anthropic has a different API structure
                system_msg = None
                user_messages = []

                for msg in messages:
                    if msg["role"] == "system":
                        system_msg = msg["content"]
                    else:
                        user_messages.append(msg)

                response = self.client.messages.create(
                    model=self.model,
                    max_tokens=kwargs.get("max_tokens", self.max_tokens),
                    temperature=kwargs.get("temperature", self.temperature),
                    system=system_msg or "",
                    messages=user_messages
                )
                return response.content[0].text

        except Exception as e:
            raise Exception(f"LLM API call failed: {str(e)}")

    def stream_chat(self, messages: List[Dict[str, str]], **kwargs):
        """Stream chat completion (for future implementation)"""
        raise NotImplementedError("Streaming not yet implemented")

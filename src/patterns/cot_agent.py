"""Chain of Thought Agent Pattern"""
from typing import List, Dict, Any
from src.agents.base_agent import BaseAgent, AgentResponse
import re


class ChainOfThoughtAgent(BaseAgent):
    """
    Chain of Thought pattern: Breaks down complex problems into step-by-step reasoning.

    The agent:
    1. Receives a query
    2. Thinks through the problem step by step
    3. Provides a final answer based on the reasoning chain
    """

    def _default_system_prompt(self) -> str:
        return f"""You are {self.name}, an AI assistant using Chain of Thought reasoning.

When answering questions:
1. Break down complex problems into smaller steps
2. Show your reasoning process clearly
3. Number your reasoning steps
4. Draw conclusions based on your step-by-step analysis

Format your response like this:

**Reasoning:**
Step 1: [First reasoning step]
Step 2: [Second reasoning step]
Step 3: [Third reasoning step]
...

**Conclusion:**
[Your final answer based on the reasoning above]

Be thorough and explicit in your reasoning. Show all intermediate steps."""

    def process(self, user_input: str, **kwargs) -> AgentResponse:
        """Process user input using Chain of Thought reasoning"""
        self.add_message("user", user_input)

        try:
            # Build messages
            messages = self._build_messages()

            # Get LLM response
            response = self.llm_client.chat(messages)

            # Parse reasoning and conclusion
            reasoning, conclusion = self._parse_cot_response(response)

            self.add_message("assistant", response)
            self._update_stats(success=True)

            return AgentResponse(
                content=conclusion or response,
                reasoning=reasoning or "No explicit reasoning provided",
                success=True,
                metadata={
                    "reasoning_steps": self._extract_steps(reasoning) if reasoning else []
                }
            )

        except Exception as e:
            self._update_stats(success=False)
            return AgentResponse(
                content="",
                success=False,
                error=str(e)
            )

    def _build_messages(self) -> List[Dict[str, str]]:
        """Build message list for LLM"""
        messages = [{"role": "system", "content": self.system_prompt}]

        for msg in self.conversation_history:
            messages.append({
                "role": msg.role,
                "content": msg.content
            })

        return messages

    def _parse_cot_response(self, response: str) -> tuple[str, str]:
        """Parse Chain of Thought formatted response"""
        # Try to extract Reasoning and Conclusion sections
        reasoning_match = re.search(
            r'\*\*Reasoning:\*\*\s*(.+?)(?=\*\*Conclusion:\*\*|\Z)',
            response,
            re.DOTALL | re.IGNORECASE
        )
        conclusion_match = re.search(
            r'\*\*Conclusion:\*\*\s*(.+?)(?=\Z)',
            response,
            re.DOTALL | re.IGNORECASE
        )

        reasoning = reasoning_match.group(1).strip() if reasoning_match else ""
        conclusion = conclusion_match.group(1).strip() if conclusion_match else ""

        return reasoning, conclusion

    def _extract_steps(self, reasoning: str) -> List[str]:
        """Extract individual reasoning steps"""
        # Find all numbered steps
        steps = re.findall(r'Step \d+:\s*(.+?)(?=Step \d+:|\Z)', reasoning, re.DOTALL | re.IGNORECASE)
        return [step.strip() for step in steps]

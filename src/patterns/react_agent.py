"""ReAct (Reasoning and Acting) Agent Pattern"""
from typing import List, Dict, Any, Optional
from src.agents.base_agent import BaseAgent, AgentResponse
import re
import json


class ReActAgent(BaseAgent):
    """
    ReAct pattern: Interleaves reasoning and action steps.

    The agent follows a loop:
    1. Thought: Reason about the current situation
    2. Action: Decide on an action to take
    3. Observation: Observe the result
    4. Repeat until the task is complete
    """

    def __init__(self, *args, max_iterations: int = 10, **kwargs):
        super().__init__(*args, **kwargs)
        self.max_iterations = max_iterations

    def _default_system_prompt(self) -> str:
        tool_descriptions = "\n".join([
            f"- {tool.name}: {tool.description}"
            for tool in self.tools
        ]) if self.tools else "No tools available"

        return f"""You are {self.name}, an AI assistant using the ReAct (Reasoning and Acting) pattern.

For each step, you should:
1. **Thought**: Think about what you need to do next
2. **Action**: Choose an action to take (or Final Answer if done)
3. **Observation**: You'll receive the result of your action

Available tools:
{tool_descriptions}

Format your response EXACTLY like this:
Thought: [your reasoning here]
Action: [tool_name: tool_input] OR [Final Answer: your final response]

Example:
Thought: I need to search for information about X
Action: search: what is X
... (you'll get observation)
Thought: Based on the search results, I now know Y
Action: Final Answer: The answer is Y

Remember: Always include both Thought and Action in your response."""

    def process(self, user_input: str, **kwargs) -> AgentResponse:
        """Process user input using ReAct pattern"""
        self.add_message("user", user_input)

        actions_taken = []
        reasoning_steps = []
        iterations = 0

        try:
            while iterations < self.max_iterations:
                iterations += 1

                # Get LLM response
                messages = self._build_messages()
                response = self.llm_client.chat(messages)

                # Parse response
                thought, action = self._parse_react_response(response)

                if not thought or not action:
                    # If parsing fails, ask for clarification
                    reasoning_steps.append(f"Iteration {iterations}: Failed to parse response")
                    break

                reasoning_steps.append(f"Thought: {thought}")

                # Check if this is the final answer
                if action.lower().startswith("final answer:"):
                    final_answer = action[13:].strip()
                    self.add_message("assistant", final_answer)
                    self._update_stats(success=True, tool_calls=len(actions_taken))

                    return AgentResponse(
                        content=final_answer,
                        reasoning="\n".join(reasoning_steps),
                        actions_taken=actions_taken,
                        success=True
                    )

                # Execute action
                tool_result = self._execute_action(action)
                actions_taken.append(f"Action: {action}")
                reasoning_steps.append(f"Observation: {tool_result}")

                # Add observation to context
                self.add_message("assistant", f"Thought: {thought}\nAction: {action}")
                self.add_message("user", f"Observation: {tool_result}")

            # Max iterations reached
            self._update_stats(success=False)
            return AgentResponse(
                content="Maximum iterations reached without finding a final answer.",
                reasoning="\n".join(reasoning_steps),
                actions_taken=actions_taken,
                success=False,
                error="Max iterations exceeded"
            )

        except Exception as e:
            self._update_stats(success=False)
            return AgentResponse(
                content="",
                reasoning="\n".join(reasoning_steps),
                actions_taken=actions_taken,
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

    def _parse_react_response(self, response: str) -> tuple[Optional[str], Optional[str]]:
        """Parse ReAct formatted response"""
        # Try to extract Thought and Action
        thought_match = re.search(r'Thought:\s*(.+?)(?=\nAction:|\Z)', response, re.DOTALL | re.IGNORECASE)
        action_match = re.search(r'Action:\s*(.+?)(?=\n|$)', response, re.IGNORECASE)

        thought = thought_match.group(1).strip() if thought_match else None
        action = action_match.group(1).strip() if action_match else None

        return thought, action

    def _execute_action(self, action: str) -> str:
        """Execute an action using available tools"""
        # Parse action string (format: "tool_name: input")
        if ":" not in action:
            return f"Invalid action format. Expected 'tool_name: input', got '{action}'"

        tool_name, tool_input = action.split(":", 1)
        tool_name = tool_name.strip()
        tool_input = tool_input.strip()

        # Find and execute tool
        for tool in self.tools:
            if tool.name.lower() == tool_name.lower():
                try:
                    result = tool.execute(tool_input)
                    return str(result)
                except Exception as e:
                    return f"Tool execution failed: {str(e)}"

        return f"Tool '{tool_name}' not found. Available tools: {[t.name for t in self.tools]}"

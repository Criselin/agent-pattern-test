"""Plan and Execute Agent Pattern"""
from typing import List, Dict, Any
from src.agents.base_agent import BaseAgent, AgentResponse
import re


class PlanExecuteAgent(BaseAgent):
    """
    Plan and Execute pattern: First creates a plan, then executes it step by step.

    The agent:
    1. Receives a task
    2. Creates a detailed plan
    3. Executes each step of the plan
    4. Provides results
    """

    def _default_system_prompt(self) -> str:
        tool_descriptions = "\n".join([
            f"- {tool.name}: {tool.description}"
            for tool in self.tools
        ]) if self.tools else "No tools available"

        return f"""You are {self.name}, an AI assistant using the Plan and Execute pattern.

Available tools:
{tool_descriptions}

When given a task, you should:
1. Create a detailed plan with numbered steps
2. Execute each step systematically
3. Report the results

Format your response like this:

**PLAN:**
1. [First step]
2. [Second step]
3. [Third step]
...

**EXECUTION:**
Step 1: [Details of executing step 1]
Result: [What happened]

Step 2: [Details of executing step 2]
Result: [What happened]

...

**FINAL RESULT:**
[Summary of what was accomplished]
"""

    def process(self, user_input: str, **kwargs) -> AgentResponse:
        """Process user input using Plan and Execute pattern"""
        self.add_message("user", user_input)

        try:
            # Phase 1: Create plan
            plan_messages = self._build_planning_messages()
            plan_response = self.llm_client.chat(plan_messages)

            plan_steps = self._extract_plan(plan_response)

            # Phase 2: Execute plan
            execution_results = []
            actions_taken = []

            for i, step in enumerate(plan_steps, 1):
                exec_messages = self._build_execution_messages(step, i)
                exec_response = self.llm_client.chat(exec_messages)

                execution_results.append(f"Step {i}: {step}\nResult: {exec_response}")
                actions_taken.append(f"Executed step {i}: {step}")

                # If tools are involved, execute them
                tool_result = self._execute_step_tools(exec_response)
                if tool_result:
                    execution_results.append(f"Tool Result: {tool_result}")

            # Phase 3: Summarize
            summary_messages = self._build_summary_messages(plan_steps, execution_results)
            summary = self.llm_client.chat(summary_messages)

            full_response = f"**PLAN:**\n" + "\n".join([f"{i}. {s}" for i, s in enumerate(plan_steps, 1)])
            full_response += f"\n\n**EXECUTION:**\n" + "\n\n".join(execution_results)
            full_response += f"\n\n**FINAL RESULT:**\n{summary}"

            self.add_message("assistant", full_response)
            self._update_stats(success=True, tool_calls=len(actions_taken))

            return AgentResponse(
                content=summary,
                reasoning=f"Plan: {', '.join(plan_steps)}",
                actions_taken=actions_taken,
                success=True,
                metadata={
                    "plan": plan_steps,
                    "execution_details": execution_results
                }
            )

        except Exception as e:
            self._update_stats(success=False)
            return AgentResponse(
                content="",
                success=False,
                error=str(e)
            )

    def _build_planning_messages(self) -> List[Dict[str, str]]:
        """Build messages for planning phase"""
        messages = [
            {"role": "system", "content": self.system_prompt},
            {"role": "user", "content": f"Create a detailed plan to accomplish this task: {self.conversation_history[-1].content}\n\nProvide ONLY the plan as a numbered list."}
        ]
        return messages

    def _build_execution_messages(self, step: str, step_num: int) -> List[Dict[str, str]]:
        """Build messages for executing a specific step"""
        messages = [
            {"role": "system", "content": f"You are executing step {step_num} of a plan."},
            {"role": "user", "content": f"Execute this step: {step}\n\nProvide the result of executing this step."}
        ]
        return messages

    def _build_summary_messages(self, plan: List[str], results: List[str]) -> List[Dict[str, str]]:
        """Build messages for summarizing results"""
        context = f"Plan:\n" + "\n".join([f"{i}. {s}" for i, s in enumerate(plan, 1)])
        context += f"\n\nExecution Results:\n" + "\n".join(results)

        messages = [
            {"role": "system", "content": "Summarize the results of the executed plan."},
            {"role": "user", "content": f"{context}\n\nProvide a concise summary of what was accomplished."}
        ]
        return messages

    def _extract_plan(self, plan_response: str) -> List[str]:
        """Extract plan steps from response"""
        # Find numbered list items
        steps = re.findall(r'^\d+\.\s*(.+)$', plan_response, re.MULTILINE)

        if not steps:
            # Fallback: split by lines and filter
            lines = [line.strip() for line in plan_response.split('\n') if line.strip()]
            steps = [line for line in lines if line and not line.startswith('**')]

        return steps[:10]  # Limit to 10 steps

    def _execute_step_tools(self, step_response: str) -> str:
        """Execute any tools mentioned in the step response"""
        # Simple pattern matching for tool calls
        # Format: [tool_name(input)]
        tool_pattern = r'\[(\w+)\(([^)]+)\)\]'
        matches = re.findall(tool_pattern, step_response)

        results = []
        for tool_name, tool_input in matches:
            for tool in self.tools:
                if tool.name.lower() == tool_name.lower():
                    try:
                        result = tool.execute(tool_input)
                        results.append(f"{tool_name}: {result}")
                    except Exception as e:
                        results.append(f"{tool_name} failed: {str(e)}")

        return "; ".join(results) if results else None

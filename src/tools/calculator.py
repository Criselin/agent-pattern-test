"""Calculator tool for mathematical operations"""
from .base_tool import BaseTool
import re


class CalculatorTool(BaseTool):
    """Simple calculator tool for basic mathematical operations"""

    def __init__(self):
        super().__init__(
            name="calculator",
            description="Performs basic mathematical calculations. Input should be a mathematical expression (e.g., '2 + 2', '10 * 5', '100 / 4')"
        )

    def execute(self, input_data: str) -> str:
        """Execute calculation"""
        self._log_call()

        try:
            # Clean input
            expression = input_data.strip()

            # Security: Only allow numbers and basic operators
            if not re.match(r'^[\d\s\+\-\*\/\(\)\.\%]+$', expression):
                return "Invalid expression. Only numbers and operators (+, -, *, /, %, parentheses) are allowed."

            # Evaluate
            result = eval(expression)
            return f"{expression} = {result}"

        except ZeroDivisionError:
            return "Error: Division by zero"
        except Exception as e:
            return f"Calculation error: {str(e)}"

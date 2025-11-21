"""Demo script to run agent patterns"""
import sys
import os
from pathlib import Path

# Add src to path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from src.agents.llm_client import LLMClient
from src.patterns.react_agent import ReActAgent
from src.patterns.cot_agent import ChainOfThoughtAgent
from src.patterns.plan_execute_agent import PlanExecuteAgent
from src.tools.calculator import CalculatorTool
from src.tools.search import SearchTool
from src.tools.weather import WeatherTool
from rich.console import Console
from rich.panel import Panel
from rich.markdown import Markdown
import json


console = Console()


def print_header(text):
    """Print formatted header"""
    console.print(f"\n[bold cyan]{'=' * 80}[/bold cyan]")
    console.print(f"[bold yellow]{text}[/bold yellow]")
    console.print(f"[bold cyan]{'=' * 80}[/bold cyan]\n")


def print_response(response, agent_name):
    """Print agent response in a formatted way"""
    console.print(Panel(
        f"[bold green]Agent:[/bold green] {agent_name}\n\n"
        f"[bold]Answer:[/bold]\n{response.content}\n\n"
        f"[bold]Success:[/bold] {response.success}",
        title="Response",
        border_style="green" if response.success else "red"
    ))

    if response.reasoning:
        console.print("\n[bold blue]Reasoning Process:[/bold blue]")
        console.print(response.reasoning)

    if response.actions_taken:
        console.print("\n[bold magenta]Actions Taken:[/bold magenta]")
        for action in response.actions_taken:
            console.print(f"  • {action}")


def demo_mock_mode():
    """Run demo in mock mode (no API calls)"""
    print_header("DEMO MODE (Mock LLM - No API calls)")

    console.print("[yellow]This demo runs with mock responses to show the framework structure.[/yellow]")
    console.print("[yellow]To use real LLMs, set up your API keys and run with --real flag.[/yellow]\n")

    # Create mock LLM client
    class MockLLMClient:
        def __init__(self):
            self.provider = "mock"
            self.model = "mock-model"

        def chat(self, messages, **kwargs):
            # Return mock responses based on the last message
            last_msg = messages[-1]["content"]

            if "calculate" in last_msg.lower() or "math" in last_msg.lower():
                return """**Reasoning:**
Step 1: Identify the mathematical expression in the question
Step 2: Use the calculator tool to compute the result
Step 3: Format the answer clearly

**Conclusion:**
The calculation has been completed. The result is shown above."""

            elif "weather" in last_msg.lower():
                return """Thought: I need to check the weather for the requested location
Action: weather: Beijing"""

            else:
                return """**Reasoning:**
Step 1: Analyze the question to understand what information is needed
Step 2: Break down the problem into manageable parts
Step 3: Synthesize the information to provide a clear answer

**Conclusion:**
Based on the analysis, here is the answer to your question."""

    # Setup
    llm_client = MockLLMClient()
    tools = [CalculatorTool(), SearchTool(), WeatherTool()]

    # Test queries
    queries = [
        "What is 25 * 4 + 10?",
        "Explain what AI agents are",
        "What's the weather in Beijing?"
    ]

    # Test each pattern
    patterns = [
        ("Chain of Thought", ChainOfThoughtAgent("CoT-Agent", llm_client)),
        ("ReAct", ReActAgent("ReAct-Agent", llm_client, tools=tools)),
    ]

    for query in queries[:2]:  # Just show 2 examples in mock mode
        console.print(f"\n[bold white]Query:[/bold white] {query}\n")

        for pattern_name, agent in patterns:
            try:
                response = agent.process(query)
                print_response(response, f"{pattern_name} Agent")
            except Exception as e:
                console.print(f"[red]Error with {pattern_name}: {str(e)}[/red]")

        console.print("\n" + "─" * 80)


def demo_real_mode():
    """Run demo with real API calls"""
    print_header("REAL MODE (Live API calls)")

    # Check for API keys
    if not os.getenv("OPENAI_API_KEY") and not os.getenv("ANTHROPIC_API_KEY"):
        console.print("[red]ERROR: No API keys found![/red]")
        console.print("Please set OPENAI_API_KEY or ANTHROPIC_API_KEY in your .env file")
        return

    # Determine which provider to use
    provider = "openai" if os.getenv("OPENAI_API_KEY") else "anthropic"
    console.print(f"[green]Using provider: {provider}[/green]\n")

    # Setup
    llm_client = LLMClient(provider=provider)
    tools = [CalculatorTool(), SearchTool(), WeatherTool()]

    # Load test cases
    test_cases_file = project_root / "data" / "test_cases" / "all_test_cases.json"

    if not test_cases_file.exists():
        console.print("[yellow]No test cases found. Generating...[/yellow]")
        os.system(f"python {project_root / 'scripts' / 'generate_test_data.py'}")

    # Run tests
    console.print("\n[bold]Running Agent Pattern Comparison[/bold]\n")

    test_query = "What is 15 * 8, and is that number greater than 100?"

    console.print(f"[bold white]Test Query:[/bold white] {test_query}\n")

    patterns = [
        ("Chain of Thought", ChainOfThoughtAgent("CoT-Agent", llm_client)),
        ("ReAct", ReActAgent("ReAct-Agent", llm_client, tools=tools, max_iterations=5)),
        ("Plan & Execute", PlanExecuteAgent("Plan-Agent", llm_client, tools=tools))
    ]

    for pattern_name, agent in patterns:
        console.print(f"\n[bold cyan]Testing {pattern_name} Pattern...[/bold cyan]")
        try:
            response = agent.process(test_query)
            print_response(response, pattern_name)

            # Print stats
            stats = agent.get_stats()
            console.print(f"\n[dim]Stats: {json.dumps(stats, indent=2)}[/dim]")

        except Exception as e:
            console.print(f"[red]Error: {str(e)}[/red]")

        console.print("\n" + "─" * 80)


def main():
    """Main function"""
    console.print("[bold green]Agent Pattern Test Framework - Demo[/bold green]\n")

    # Check command line args
    if "--real" in sys.argv:
        demo_real_mode()
    else:
        demo_mock_mode()
        console.print("\n[bold cyan]To run with real API calls, use: python scripts/run_demo.py --real[/bold cyan]")


if __name__ == "__main__":
    main()

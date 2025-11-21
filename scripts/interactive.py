"""Interactive CLI for testing agents"""
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
from rich.prompt import Prompt
from rich.table import Table


console = Console()


class InteractiveAgent:
    """Interactive agent CLI"""

    def __init__(self):
        self.agents = {}
        self.current_agent = None
        self.tools = []

    def setup(self):
        """Setup agents and tools"""
        console.print("[bold green]Setting up Agent Pattern Test Framework...[/bold green]\n")

        # Check for API keys
        has_openai = bool(os.getenv("OPENAI_API_KEY"))
        has_anthropic = bool(os.getenv("ANTHROPIC_API_KEY"))

        if not has_openai and not has_anthropic:
            console.print("[yellow]No API keys found. Running in Mock mode.[/yellow]")
            llm_client = self._create_mock_client()
        else:
            provider = "openai" if has_openai else "anthropic"
            console.print(f"[green]Using provider: {provider}[/green]")
            llm_client = LLMClient(provider=provider)

        # Setup tools
        self.tools = [
            CalculatorTool(),
            SearchTool(),
            WeatherTool()
        ]

        # Create agents
        self.agents = {
            "1": ("Chain of Thought", ChainOfThoughtAgent("CoT-Agent", llm_client)),
            "2": ("ReAct", ReActAgent("ReAct-Agent", llm_client, tools=self.tools)),
            "3": ("Plan & Execute", PlanExecuteAgent("Plan-Agent", llm_client, tools=self.tools))
        }

        console.print("[green]✓ Setup complete![/green]\n")

    def _create_mock_client(self):
        """Create mock LLM client"""
        class MockLLMClient:
            def __init__(self):
                self.provider = "mock"
                self.model = "mock-model"

            def chat(self, messages, **kwargs):
                return """**Reasoning:**
Step 1: Understanding the question
Step 2: Processing the information
Step 3: Formulating a response

**Conclusion:**
This is a mock response. Set up API keys to get real responses."""

        return MockLLMClient()

    def show_menu(self):
        """Show main menu"""
        console.print("\n[bold cyan]Agent Pattern Test - Interactive Mode[/bold cyan]\n")

        table = Table(show_header=True, header_style="bold magenta")
        table.add_column("Option", style="cyan", width=10)
        table.add_column("Agent Pattern", style="green")
        table.add_column("Description")

        table.add_row("1", "Chain of Thought", "Step-by-step reasoning")
        table.add_row("2", "ReAct", "Reasoning + Acting with tools")
        table.add_row("3", "Plan & Execute", "Plan first, then execute")
        table.add_row("", "", "")
        table.add_row("h", "Help", "Show examples and tips")
        table.add_row("q", "Quit", "Exit the program")

        console.print(table)

    def show_help(self):
        """Show help and examples"""
        console.print("\n[bold cyan]Examples and Tips[/bold cyan]\n")

        examples = [
            ("Mathematical", "What is 25 * 4 + 10?", "Works best with ReAct"),
            ("Search", "What is Python programming?", "All patterns work"),
            ("Weather", "What's the weather in Tokyo?", "Works best with ReAct"),
            ("Multi-step", "If temperature in NYC is above 20°C, convert to Fahrenheit", "Plan & Execute or ReAct"),
            ("Reasoning", "If I have $100 and spend 30%, how much is left?", "All patterns work"),
        ]

        for category, example, note in examples:
            console.print(f"[yellow]{category}:[/yellow] {example}")
            console.print(f"  [dim]{note}[/dim]\n")

    def run(self):
        """Run interactive loop"""
        self.setup()

        while True:
            self.show_menu()

            choice = Prompt.ask("\n[bold]Select an agent pattern[/bold]", default="1")

            if choice.lower() == 'q':
                console.print("\n[green]Goodbye![/green]")
                break

            if choice.lower() == 'h':
                self.show_help()
                continue

            if choice not in self.agents:
                console.print("[red]Invalid choice. Please try again.[/red]")
                continue

            agent_name, agent = self.agents[choice]
            console.print(f"\n[green]Selected: {agent_name}[/green]\n")

            # Chat loop for selected agent
            while True:
                query = Prompt.ask("[bold blue]Enter your query[/bold blue] (or 'back' to change agent)")

                if query.lower() == 'back':
                    agent.reset()
                    break

                if query.lower() in ['quit', 'exit', 'q']:
                    console.print("\n[green]Goodbye![/green]")
                    return

                # Process query
                console.print("\n[yellow]Processing...[/yellow]\n")

                try:
                    response = agent.process(query)

                    # Display response
                    console.print(Panel(
                        f"[bold]Answer:[/bold]\n{response.content}",
                        title=f"{agent_name} Response",
                        border_style="green" if response.success else "red"
                    ))

                    if response.reasoning:
                        console.print("\n[bold blue]Reasoning:[/bold blue]")
                        console.print(response.reasoning)

                    if response.actions_taken:
                        console.print("\n[bold magenta]Actions:[/bold magenta]")
                        for action in response.actions_taken:
                            console.print(f"  • {action}")

                    if response.error:
                        console.print(f"\n[red]Error: {response.error}[/red]")

                except KeyboardInterrupt:
                    console.print("\n[yellow]Interrupted[/yellow]")
                    break
                except Exception as e:
                    console.print(f"\n[red]Error: {str(e)}[/red]")

                console.print("\n" + "─" * 80 + "\n")


def main():
    """Main function"""
    try:
        interactive = InteractiveAgent()
        interactive.run()
    except KeyboardInterrupt:
        console.print("\n\n[green]Goodbye![/green]")
    except Exception as e:
        console.print(f"\n[red]Fatal error: {str(e)}[/red]")


if __name__ == "__main__":
    main()

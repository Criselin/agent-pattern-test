"""Run comprehensive tests on all agent patterns"""
import sys
import os
import json
from pathlib import Path
from datetime import datetime
from typing import List, Dict

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
from rich.table import Table
from rich.progress import track
import yaml


console = Console()


class AgentTester:
    """Test harness for agent patterns"""

    def __init__(self, config_path=None):
        self.config = self._load_config(config_path)
        self.results = []
        self.llm_client = None
        self.tools = []

    def _load_config(self, config_path):
        """Load configuration"""
        if config_path is None:
            config_path = project_root / "config" / "agent_config.yaml"

        if config_path.exists():
            with open(config_path, 'r') as f:
                return yaml.safe_load(f)
        return {}

    def setup(self):
        """Setup LLM client and tools"""
        llm_config = self.config.get('llm', {})

        # Check for mock mode
        if "--mock" in sys.argv:
            console.print("[yellow]Running in MOCK mode (no API calls)[/yellow]\n")
            self.llm_client = self._create_mock_client()
        else:
            provider = llm_config.get('provider', 'openai')
            model = llm_config.get('model')
            self.llm_client = LLMClient(provider=provider, model=model)

        # Setup tools
        tool_config = self.config.get('tools', {})
        if tool_config.get('calculator', {}).get('enabled', True):
            self.tools.append(CalculatorTool())
        if tool_config.get('search', {}).get('enabled', True):
            self.tools.append(SearchTool())
        if tool_config.get('weather', {}).get('enabled', True):
            self.tools.append(WeatherTool())

    def _create_mock_client(self):
        """Create a mock LLM client for testing"""
        class MockLLMClient:
            def __init__(self):
                self.provider = "mock"
                self.model = "mock-model"

            def chat(self, messages, **kwargs):
                return """**Reasoning:**
Step 1: Analyzing the question
Step 2: Processing the information
Step 3: Formulating the answer

**Conclusion:**
This is a mock response for testing purposes."""

        return MockLLMClient()

    def load_test_cases(self, category=None):
        """Load test cases"""
        test_cases_dir = project_root / "data" / "test_cases"

        if not test_cases_dir.exists():
            console.print("[yellow]Test cases not found. Generating...[/yellow]")
            os.system(f"python {project_root / 'scripts' / 'generate_test_data.py'}")

        all_cases_file = test_cases_dir / "all_test_cases.json"

        with open(all_cases_file, 'r') as f:
            all_cases = json.load(f)

        # Flatten test cases
        test_cases = []
        for cat, cases in all_cases.items():
            if category is None or cat == category:
                test_cases.extend(cases)

        return test_cases

    def create_agents(self):
        """Create agent instances for each pattern"""
        agent_config = self.config.get('agents', {})
        agents = []

        if agent_config.get('chain_of_thought', {}).get('enabled', True):
            agents.append(("CoT", ChainOfThoughtAgent("CoT-Agent", self.llm_client)))

        if agent_config.get('react', {}).get('enabled', True):
            max_iter = agent_config.get('react', {}).get('max_iterations', 10)
            agents.append(("ReAct", ReActAgent("ReAct-Agent", self.llm_client, tools=self.tools, max_iterations=max_iter)))

        if agent_config.get('plan_execute', {}).get('enabled', True):
            agents.append(("Plan&Execute", PlanExecuteAgent("Plan-Agent", self.llm_client, tools=self.tools)))

        return agents

    def run_test(self, agent_name, agent, test_case):
        """Run a single test"""
        try:
            start_time = datetime.now()
            response = agent.process(test_case['query'])
            end_time = datetime.now()

            duration = (end_time - start_time).total_seconds()

            result = {
                "agent": agent_name,
                "test_id": test_case['id'],
                "query": test_case['query'],
                "category": test_case.get('category', 'unknown'),
                "success": response.success,
                "duration": duration,
                "actions_count": len(response.actions_taken),
                "response_length": len(response.content),
                "error": response.error
            }

            return result

        except Exception as e:
            return {
                "agent": agent_name,
                "test_id": test_case['id'],
                "query": test_case['query'],
                "category": test_case.get('category', 'unknown'),
                "success": False,
                "duration": 0,
                "actions_count": 0,
                "response_length": 0,
                "error": str(e)
            }

    def run_all_tests(self, category=None):
        """Run all tests"""
        console.print("\n[bold green]Starting Agent Pattern Tests[/bold green]\n")

        test_cases = self.load_test_cases(category)
        agents = self.create_agents()

        console.print(f"Test cases loaded: {len(test_cases)}")
        console.print(f"Agent patterns: {len(agents)}")
        console.print(f"Total tests: {len(test_cases) * len(agents)}\n")

        # Run tests
        for test_case in track(test_cases, description="Running tests..."):
            for agent_name, agent in agents:
                result = self.run_test(agent_name, agent, test_case)
                self.results.append(result)

                # Reset agent for next test
                agent.reset()

    def generate_report(self):
        """Generate test report"""
        if not self.results:
            console.print("[red]No results to report[/red]")
            return

        console.print("\n[bold cyan]Test Results Summary[/bold cyan]\n")

        # Overall statistics
        total_tests = len(self.results)
        successful_tests = sum(1 for r in self.results if r['success'])
        failed_tests = total_tests - successful_tests

        console.print(f"Total Tests: {total_tests}")
        console.print(f"Successful: {successful_tests} ({successful_tests/total_tests*100:.1f}%)")
        console.print(f"Failed: {failed_tests} ({failed_tests/total_tests*100:.1f}%)\n")

        # Per-agent statistics
        table = Table(title="Performance by Agent Pattern")
        table.add_column("Agent", style="cyan")
        table.add_column("Tests", justify="right")
        table.add_column("Success", justify="right", style="green")
        table.add_column("Failed", justify="right", style="red")
        table.add_column("Avg Duration (s)", justify="right")
        table.add_column("Avg Actions", justify="right")

        agents = set(r['agent'] for r in self.results)
        for agent in agents:
            agent_results = [r for r in self.results if r['agent'] == agent]
            tests = len(agent_results)
            success = sum(1 for r in agent_results if r['success'])
            failed = tests - success
            avg_duration = sum(r['duration'] for r in agent_results) / tests
            avg_actions = sum(r['actions_count'] for r in agent_results) / tests

            table.add_row(
                agent,
                str(tests),
                str(success),
                str(failed),
                f"{avg_duration:.2f}",
                f"{avg_actions:.1f}"
            )

        console.print(table)

        # Save results
        self._save_results()

    def _save_results(self):
        """Save results to file"""
        output_dir = project_root / self.config.get('testing', {}).get('output_dir', 'data/results')
        output_dir.mkdir(parents=True, exist_ok=True)

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_file = output_dir / f"test_results_{timestamp}.json"

        with open(output_file, 'w') as f:
            json.dump({
                "timestamp": timestamp,
                "total_tests": len(self.results),
                "results": self.results
            }, f, indent=2)

        console.print(f"\n[green]Results saved to: {output_file}[/green]")


def main():
    """Main function"""
    tester = AgentTester()
    tester.setup()
    tester.run_all_tests()
    tester.generate_report()


if __name__ == "__main__":
    main()

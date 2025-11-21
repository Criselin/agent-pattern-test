"""Unit tests for agent patterns"""
import pytest
import sys
from pathlib import Path

# Add src to path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from src.agents.base_agent import BaseAgent, AgentResponse
from src.patterns.react_agent import ReActAgent
from src.patterns.cot_agent import ChainOfThoughtAgent
from src.patterns.plan_execute_agent import PlanExecuteAgent
from src.tools.calculator import CalculatorTool
from src.tools.search import SearchTool


class MockLLMClient:
    """Mock LLM client for testing"""

    def __init__(self):
        self.provider = "mock"
        self.model = "mock-model"
        self.call_count = 0

    def chat(self, messages, **kwargs):
        self.call_count += 1
        last_message = messages[-1]["content"].lower()

        # Simulate different responses based on input
        if "calculate" in last_message or "math" in last_message:
            return """Thought: I need to calculate the result
Action: calculator: 25 * 4 + 10"""

        elif "final answer" in last_message:
            return """Thought: I have the result
Action: Final Answer: The answer is 110"""

        else:
            return """**Reasoning:**
Step 1: Analyzing the query
Step 2: Processing information
Step 3: Formulating response

**Conclusion:**
Test response completed."""


@pytest.fixture
def mock_client():
    """Provide mock LLM client"""
    return MockLLMClient()


@pytest.fixture
def calculator_tool():
    """Provide calculator tool"""
    return CalculatorTool()


@pytest.fixture
def search_tool():
    """Provide search tool"""
    return SearchTool()


class TestCalculatorTool:
    """Test calculator tool"""

    def test_basic_calculation(self, calculator_tool):
        result = calculator_tool.execute("2 + 2")
        assert "4" in result

    def test_multiplication(self, calculator_tool):
        result = calculator_tool.execute("25 * 4")
        assert "100" in result

    def test_complex_expression(self, calculator_tool):
        result = calculator_tool.execute("(100 - 25) / 5")
        assert "15" in result

    def test_invalid_expression(self, calculator_tool):
        result = calculator_tool.execute("invalid")
        assert "error" in result.lower()


class TestSearchTool:
    """Test search tool"""

    def test_search_python(self, search_tool):
        result = search_tool.execute("python")
        assert "python" in result.lower()
        assert "programming" in result.lower()

    def test_search_ai(self, search_tool):
        result = search_tool.execute("ai")
        assert "artificial intelligence" in result.lower()

    def test_generic_search(self, search_tool):
        result = search_tool.execute("unknown topic")
        assert "search results" in result.lower()


class TestChainOfThoughtAgent:
    """Test Chain of Thought agent"""

    def test_initialization(self, mock_client):
        agent = ChainOfThoughtAgent("TestAgent", mock_client)
        assert agent.name == "TestAgent"
        assert agent.llm_client == mock_client

    def test_process_query(self, mock_client):
        agent = ChainOfThoughtAgent("TestAgent", mock_client)
        response = agent.process("What is 2 + 2?")

        assert isinstance(response, AgentResponse)
        assert response.content is not None
        assert response.success is True

    def test_conversation_history(self, mock_client):
        agent = ChainOfThoughtAgent("TestAgent", mock_client)
        agent.process("First query")
        agent.process("Second query")

        assert len(agent.conversation_history) == 4  # 2 user + 2 assistant

    def test_reset(self, mock_client):
        agent = ChainOfThoughtAgent("TestAgent", mock_client)
        agent.process("Test query")
        agent.reset()

        assert len(agent.conversation_history) == 0


class TestReActAgent:
    """Test ReAct agent"""

    def test_initialization(self, mock_client, calculator_tool):
        agent = ReActAgent("TestAgent", mock_client, tools=[calculator_tool])
        assert agent.name == "TestAgent"
        assert len(agent.tools) == 1

    def test_parse_react_response(self, mock_client):
        agent = ReActAgent("TestAgent", mock_client)
        response = """Thought: I need to calculate
Action: calculator: 2 + 2"""

        thought, action = agent._parse_react_response(response)
        assert thought == "I need to calculate"
        assert action == "calculator: 2 + 2"

    def test_execute_action(self, mock_client, calculator_tool):
        agent = ReActAgent("TestAgent", mock_client, tools=[calculator_tool])
        result = agent._execute_action("calculator: 2 + 2")

        assert "4" in result

    def test_invalid_tool(self, mock_client):
        agent = ReActAgent("TestAgent", mock_client)
        result = agent._execute_action("nonexistent: test")

        assert "not found" in result.lower()


class TestPlanExecuteAgent:
    """Test Plan & Execute agent"""

    def test_initialization(self, mock_client):
        agent = PlanExecuteAgent("TestAgent", mock_client)
        assert agent.name == "TestAgent"

    def test_extract_plan(self, mock_client):
        agent = PlanExecuteAgent("TestAgent", mock_client)
        plan_text = """1. First step
2. Second step
3. Third step"""

        steps = agent._extract_plan(plan_text)
        assert len(steps) == 3
        assert "First step" in steps[0]


class TestAgentStats:
    """Test agent statistics tracking"""

    def test_stats_initialization(self, mock_client):
        agent = ChainOfThoughtAgent("TestAgent", mock_client)
        stats = agent.get_stats()

        assert stats["total_queries"] == 0
        assert stats["successful_queries"] == 0
        assert stats["failed_queries"] == 0

    def test_stats_update(self, mock_client):
        agent = ChainOfThoughtAgent("TestAgent", mock_client)
        agent.process("Test query")
        stats = agent.get_stats()

        assert stats["total_queries"] == 1
        assert stats["successful_queries"] == 1


if __name__ == "__main__":
    pytest.main([__file__, "-v"])

"""Generate test data for agent testing"""
import json
import os
from pathlib import Path


def generate_test_cases():
    """Generate various test cases for different agent patterns"""

    test_cases = {
        "mathematical": [
            {
                "id": "math_001",
                "query": "What is 25 * 4 + 10?",
                "expected_tools": ["calculator"],
                "difficulty": "easy",
                "category": "calculation"
            },
            {
                "id": "math_002",
                "query": "Calculate the result of (100 - 25) / 5 and then multiply it by 3",
                "expected_tools": ["calculator"],
                "difficulty": "medium",
                "category": "calculation"
            }
        ],
        "information_retrieval": [
            {
                "id": "info_001",
                "query": "What is Python programming language?",
                "expected_tools": ["search"],
                "difficulty": "easy",
                "category": "knowledge"
            },
            {
                "id": "info_002",
                "query": "Explain what AI agents are and how they work",
                "expected_tools": ["search"],
                "difficulty": "medium",
                "category": "knowledge"
            }
        ],
        "multi_step": [
            {
                "id": "multi_001",
                "query": "What's the weather in Beijing, and if it's above 20 degrees, calculate what that is in Fahrenheit",
                "expected_tools": ["weather", "calculator"],
                "difficulty": "medium",
                "category": "multi_step"
            },
            {
                "id": "multi_002",
                "query": "Search for information about machine learning, then calculate how many years ago it was first introduced if it was created in 1959",
                "expected_tools": ["search", "calculator"],
                "difficulty": "hard",
                "category": "multi_step"
            }
        ],
        "reasoning": [
            {
                "id": "reason_001",
                "query": "If a train travels at 80 km/h for 2.5 hours, how far does it go? Calculate the distance.",
                "expected_tools": ["calculator"],
                "difficulty": "easy",
                "category": "reasoning"
            },
            {
                "id": "reason_002",
                "query": "I have 100 dollars. I spend 30% on food and 25% on transport. How much money do I have left?",
                "expected_tools": ["calculator"],
                "difficulty": "medium",
                "category": "reasoning"
            }
        ],
        "planning": [
            {
                "id": "plan_001",
                "query": "Help me plan a data analysis project: what steps should I follow?",
                "expected_tools": [],
                "difficulty": "medium",
                "category": "planning"
            },
            {
                "id": "plan_002",
                "query": "Create a plan to learn Python programming in 3 months",
                "expected_tools": ["search"],
                "difficulty": "medium",
                "category": "planning"
            }
        ]
    }

    return test_cases


def save_test_cases(test_cases, output_dir):
    """Save test cases to JSON files"""
    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    # Save all test cases in one file
    all_cases_file = output_path / "all_test_cases.json"
    with open(all_cases_file, 'w', encoding='utf-8') as f:
        json.dump(test_cases, f, indent=2, ensure_ascii=False)

    print(f"✓ Saved all test cases to {all_cases_file}")

    # Save each category separately
    for category, cases in test_cases.items():
        category_file = output_path / f"{category}_test_cases.json"
        with open(category_file, 'w', encoding='utf-8') as f:
            json.dump(cases, f, indent=2, ensure_ascii=False)
        print(f"✓ Saved {len(cases)} {category} test cases to {category_file}")

    # Generate summary
    total_cases = sum(len(cases) for cases in test_cases.values())
    summary = {
        "total_cases": total_cases,
        "categories": {cat: len(cases) for cat, cases in test_cases.items()},
        "difficulty_distribution": _get_difficulty_distribution(test_cases)
    }

    summary_file = output_path / "test_summary.json"
    with open(summary_file, 'w', encoding='utf-8') as f:
        json.dump(summary, f, indent=2)

    print(f"\n✓ Generated {total_cases} test cases across {len(test_cases)} categories")
    print(f"✓ Summary saved to {summary_file}")


def _get_difficulty_distribution(test_cases):
    """Get distribution of test difficulties"""
    distribution = {"easy": 0, "medium": 0, "hard": 0}

    for cases in test_cases.values():
        for case in cases:
            difficulty = case.get("difficulty", "unknown")
            if difficulty in distribution:
                distribution[difficulty] += 1

    return distribution


def main():
    """Main function"""
    # Get project root
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    data_dir = project_root / "data" / "test_cases"

    print("Generating test data...")
    test_cases = generate_test_cases()
    save_test_cases(test_cases, data_dir)
    print("\n✅ Test data generation complete!")


if __name__ == "__main__":
    main()

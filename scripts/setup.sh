#!/bin/bash

# Setup script for Agent Pattern Test Framework

echo "================================"
echo "Agent Pattern Test - Setup"
echo "================================"
echo ""

# Check Python version
echo "Checking Python version..."
python3 --version

if [ $? -ne 0 ]; then
    echo "❌ Python 3 is not installed. Please install Python 3.8 or higher."
    exit 1
fi

echo "✓ Python is installed"
echo ""

# Create virtual environment
echo "Creating virtual environment..."
if [ ! -d "venv" ]; then
    python3 -m venv venv
    echo "✓ Virtual environment created"
else
    echo "✓ Virtual environment already exists"
fi
echo ""

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate
echo "✓ Virtual environment activated"
echo ""

# Upgrade pip
echo "Upgrading pip..."
pip install --upgrade pip -q
echo "✓ pip upgraded"
echo ""

# Install dependencies
echo "Installing dependencies..."
pip install -r requirements.txt -q
echo "✓ Dependencies installed"
echo ""

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    echo "Creating .env file from template..."
    cp .env.example .env
    echo "✓ .env file created"
    echo ""
    echo "⚠️  IMPORTANT: Please edit .env file and add your API keys!"
    echo "   You need either OPENAI_API_KEY or ANTHROPIC_API_KEY"
else
    echo "✓ .env file already exists"
fi
echo ""

# Generate test data
echo "Generating test data..."
python scripts/generate_test_data.py
echo ""

# Create necessary directories
echo "Creating directories..."
mkdir -p logs
mkdir -p data/{test_cases,generated,results}
echo "✓ Directories created"
echo ""

echo "================================"
echo "Setup Complete!"
echo "================================"
echo ""
echo "Next steps:"
echo "1. Edit .env file and add your API keys"
echo "2. Run demo: python scripts/run_demo.py"
echo "3. Run tests: python scripts/run_tests.py"
echo "4. Or use mock mode (no API): python scripts/run_demo.py --mock"
echo ""

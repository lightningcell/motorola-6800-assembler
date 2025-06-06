#!/usr/bin/env bash

# AI Module Demo Script for Motorola 6800 Assembler
# This script demonstrates the AI functionality

echo "🤖 Motorola 6800 Assembler - AI Module Demo"
echo "=========================================="
echo ""

echo "📋 Prerequisites:"
echo "1. Valid OpenAI API key"
echo "2. Internet connection"
echo "3. Project built successfully"
echo ""

echo "🚀 Starting the application..."
echo "Run the following command manually:"
echo "   .\gradlew run"
echo ""

echo "📝 Demo Workflow:"
echo ""
echo "1. From main menu, select option 9 (AI Assembly Generator)"
echo ""
echo "2. Set your API key:"
echo "   - Select option 1 (Set API Key)"
echo "   - Enter your OpenAI API key when prompted"
echo "   - You should see: 'API key set successfully!'"
echo ""
echo "3. Generate some code:"
echo "   - Select option 2 (Generate Assembly Code)"
echo "   - Try one of these sample prompts:"
echo ""
echo "     Sample Prompt 1:"
echo "     'Write a simple program that adds two numbers: 5 + 3'"
echo ""
echo "     Sample Prompt 2:"
echo "     'Create a loop that counts from 1 to 5 and stores each number in memory'"
echo ""
echo "     Sample Prompt 3:"
echo "     'Write a program that loads a value from memory, increments it, and stores it back'"
echo ""
echo "4. Review the generated code:"
echo "   - AI will display the assembly code with line numbers"
echo "   - Confirm to load it into the assembler (y/n)"
echo ""
echo "5. Test the generated code:"
echo "   - Return to main menu (option 0)"
echo "   - Select option 3 (Assemble Program)"
echo "   - If successful, try option 5 (Simulate Program)"
echo ""

echo "🔍 What to Look For:"
echo ""
echo "✅ Successful Generation:"
echo "   - Code starts with ORG directive"
echo "   - Code ends with END directive"
echo "   - Valid Motorola 6800 instructions"
echo "   - Helpful comments explaining the logic"
echo ""
echo "✅ Proper Error Handling:"
echo "   - Clear error messages for invalid API keys"
echo "   - Timeout handling for slow responses"
echo "   - Network error messages"
echo ""
echo "✅ Integration Success:"
echo "   - Generated code loads into main assembler"
echo "   - Code assembles without syntax errors"
echo "   - Can be simulated successfully"
echo ""

echo "🚨 Troubleshooting Tips:"
echo ""
echo "❌ 'API key not configured' error:"
echo "   → Make sure you've set a valid OpenAI API key"
echo ""
echo "❌ 'Failed to parse OpenAI response' error:"
echo "   → Check your internet connection"
echo "   → Verify your API key is still valid"
echo ""
echo "❌ Generated code doesn't assemble:"
echo "   → AI-generated code may need manual review"
echo "   → Try a more specific prompt"
echo ""
echo "❌ Application hangs during generation:"
echo "   → Wait up to 30 seconds for timeout"
echo "   → Restart application if needed"
echo ""

echo "📊 Expected Performance:"
echo "   - Response time: 3-10 seconds"
echo "   - Code quality: Good for simple programs"
echo "   - Success rate: >90% for basic requests"
echo ""

echo "🎯 Demo Complete!"
echo "Ready to test the AI functionality manually."

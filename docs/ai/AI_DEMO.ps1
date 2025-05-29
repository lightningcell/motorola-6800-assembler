# AI Module Demo Script for Motorola 6800 Assembler
# PowerShell version for Windows

Write-Host "🤖 Motorola 6800 Assembler - AI Module Demo" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📋 Prerequisites:" -ForegroundColor Yellow
Write-Host "1. Valid OpenAI API key"
Write-Host "2. Internet connection"
Write-Host "3. Project built successfully"
Write-Host ""

Write-Host "🚀 Starting the application..." -ForegroundColor Green
Write-Host "Run the following command manually:"
Write-Host "   .\gradlew run" -ForegroundColor White -BackgroundColor DarkBlue
Write-Host ""

Write-Host "📝 Demo Workflow:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. From main menu, select option 9 (AI Assembly Generator)" -ForegroundColor White
Write-Host ""
Write-Host "2. Set your API key:" -ForegroundColor White
Write-Host "   - Select option 1 (Set API Key)"
Write-Host "   - Enter your OpenAI API key when prompted"
Write-Host "   - You should see: 'API key set successfully!'"
Write-Host ""
Write-Host "3. Generate some code:" -ForegroundColor White
Write-Host "   - Select option 2 (Generate Assembly Code)"
Write-Host "   - Try one of these sample prompts:"
Write-Host ""
Write-Host "     Sample Prompt 1:" -ForegroundColor Cyan
Write-Host "     'Write a simple program that adds two numbers: 5 + 3'"
Write-Host ""
Write-Host "     Sample Prompt 2:" -ForegroundColor Cyan
Write-Host "     'Create a loop that counts from 1 to 5 and stores each number in memory'"
Write-Host ""
Write-Host "     Sample Prompt 3:" -ForegroundColor Cyan
Write-Host "     'Write a program that loads a value from memory, increments it, and stores it back'"
Write-Host ""
Write-Host "4. Review the generated code:" -ForegroundColor White
Write-Host "   - AI will display the assembly code with line numbers"
Write-Host "   - Confirm to load it into the assembler (y/n)"
Write-Host ""
Write-Host "5. Test the generated code:" -ForegroundColor White
Write-Host "   - Return to main menu (option 0)"
Write-Host "   - Select option 3 (Assemble Program)"
Write-Host "   - If successful, try option 5 (Simulate Program)"
Write-Host ""

Write-Host "🔍 What to Look For:" -ForegroundColor Yellow
Write-Host ""
Write-Host "✅ Successful Generation:" -ForegroundColor Green
Write-Host "   - Code starts with ORG directive"
Write-Host "   - Code ends with END directive"
Write-Host "   - Valid Motorola 6800 instructions"
Write-Host "   - Helpful comments explaining the logic"
Write-Host ""
Write-Host "✅ Proper Error Handling:" -ForegroundColor Green
Write-Host "   - Clear error messages for invalid API keys"
Write-Host "   - Timeout handling for slow responses"
Write-Host "   - Network error messages"
Write-Host ""
Write-Host "✅ Integration Success:" -ForegroundColor Green
Write-Host "   - Generated code loads into main assembler"
Write-Host "   - Code assembles without syntax errors"
Write-Host "   - Can be simulated successfully"
Write-Host ""

Write-Host "🚨 Troubleshooting Tips:" -ForegroundColor Red
Write-Host ""
Write-Host "❌ 'API key not configured' error:" -ForegroundColor Red
Write-Host "   → Make sure you've set a valid OpenAI API key" -ForegroundColor Yellow
Write-Host ""
Write-Host "❌ 'Failed to parse OpenAI response' error:" -ForegroundColor Red
Write-Host "   → Check your internet connection" -ForegroundColor Yellow
Write-Host "   → Verify your API key is still valid" -ForegroundColor Yellow
Write-Host ""
Write-Host "❌ Generated code doesn't assemble:" -ForegroundColor Red
Write-Host "   → AI-generated code may need manual review" -ForegroundColor Yellow
Write-Host "   → Try a more specific prompt" -ForegroundColor Yellow
Write-Host ""
Write-Host "❌ Application hangs during generation:" -ForegroundColor Red
Write-Host "   → Wait up to 30 seconds for timeout" -ForegroundColor Yellow
Write-Host "   → Restart application if needed" -ForegroundColor Yellow
Write-Host ""

Write-Host "📊 Expected Performance:" -ForegroundColor Cyan
Write-Host "   - Response time: 3-10 seconds"
Write-Host "   - Code quality: Good for simple programs"
Write-Host "   - Success rate: >90% for basic requests"
Write-Host ""

Write-Host "🎯 Demo Complete!" -ForegroundColor Green
Write-Host "Ready to test the AI functionality manually." -ForegroundColor Green

# Pause to let user read the instructions
Write-Host ""
Write-Host "Press any key to launch the application..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Launch the application
Write-Host ""
Write-Host "Launching Motorola 6800 Assembler..." -ForegroundColor Green
& .\gradlew run

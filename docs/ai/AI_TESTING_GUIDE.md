# AI Module Testing Guide

## 🧪 Testing Procedures

### Prerequisites
- Valid OpenAI API key
- Project successfully built (`.\gradlew build`)
- Internet connection for API calls

### Test Cases

#### 1. API Key Management Testing

**Test 1.1: Set Valid API Key**
1. Run the application: `.\gradlew run`
2. Select option `9` (AI Assembly Generator)
3. Select option `1` (Set API Key)
4. Enter a valid OpenAI API key
5. **Expected Result**: "API key set successfully!" message

**Test 1.2: Check API Key Status**
1. From AI menu, select option `3` (Check API Key Status)
2. **Expected Result**: "✓ API key is configured and ready"

**Test 1.3: Invalid API Key Handling**
1. Set an invalid API key (e.g., "invalid-key")
2. Try to generate code
3. **Expected Result**: Proper error message about authentication failure

#### 2. Code Generation Testing

**Test 2.1: Simple Program Generation**
1. Set valid API key
2. Select option `2` (Generate Assembly Code)
3. Enter prompt: "Write a simple program that adds two numbers"
4. **Expected Result**: 
   - AI generates valid Motorola 6800 assembly code
   - Code includes ORG directive at start
   - Code includes END directive at end
   - Code is displayed with line numbers
   - User is asked for confirmation

**Test 2.2: Complex Program Generation**
1. Enter prompt: "Create a program that counts from 1 to 10 and stores results in memory"
2. **Expected Result**: 
   - More complex assembly code with loops
   - Proper use of 6800 instructions
   - Memory storage operations
   - Comments explaining the logic

**Test 2.3: Integration with Assembler**
1. Generate code and confirm to load it
2. Return to main menu and select option `3` (Assemble Program)
3. **Expected Result**: Code assembles without errors

#### 3. Error Handling Testing

**Test 3.1: Network Error Handling**
1. Disconnect from internet
2. Try to generate code
3. **Expected Result**: Proper error message about network connectivity

**Test 3.2: API Rate Limiting**
1. Make multiple rapid requests
2. **Expected Result**: Graceful handling of rate limit responses

**Test 3.3: Empty Prompt Handling**
1. Try to generate code with empty description
2. **Expected Result**: Error message requesting valid description

### Sample Test Prompts

```
1. "Write a hello world program that displays ASCII characters"
2. "Create a sorting algorithm for an array in memory"
3. "Write a program that calculates factorial of a number"
4. "Create a simple calculator that adds two 8-bit numbers"
5. "Write a program that implements a simple loop counter"
```

### Expected Output Format

Generated code should follow this pattern:
```assembly
; AI-generated comment explaining the program
ORG $0100        ; Start address
LDA #$05         ; Load immediate value
STA $0200        ; Store to memory
; ... more instructions ...
END              ; End of program
```

### Performance Benchmarks

- **API Response Time**: Should be under 10 seconds for simple requests
- **Memory Usage**: AI module should not significantly impact application memory
- **Error Recovery**: Application should remain stable after API errors

### Integration Testing Checklist

- [ ] AI-generated code passes syntax validation
- [ ] Generated code assembles without errors
- [ ] Generated code can be simulated successfully
- [ ] File save/load works with AI-generated code
- [ ] Main application remains stable during AI operations
- [ ] Proper cleanup of resources after AI operations

### Known Limitations

1. **Internet Dependency**: AI features require active internet connection
2. **API Costs**: Each generation request consumes OpenAI API credits
3. **Code Quality**: Generated code quality depends on prompt clarity
4. **Instruction Set**: AI knowledge limited to training data (may not include latest 6800 documentation)

### Troubleshooting Common Issues

**Issue**: "API key not configured"
**Solution**: Ensure valid OpenAI API key is set in option 1

**Issue**: "Failed to parse OpenAI response"
**Solution**: Check internet connection and API key validity

**Issue**: "Generated code doesn't assemble"
**Solution**: AI-generated code may need manual review and correction

**Issue**: Application hangs during generation
**Solution**: 30-second timeout is configured; wait or restart application

### Security Considerations

- API keys are stored in memory only (not persisted)
- No sensitive data is sent to OpenAI except the program description
- Network communications use HTTPS encryption
- Recommend using API keys with appropriate usage limits

package assembler.parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizer for Motorola 6800 assembly language.
 * Handles proper tokenization of assembly language constructs including:
 * - Labels (ending with :)
 * - Instructions and operands
 * - Numeric literals (decimal, hex $xx, binary %xxxxxxxx)
 * - String literals
 * - Operators and punctuation
 * 
 * @author Motorola 6800 Assembler Team
 */
public class TokenParser {
    
    // Token patterns in order of priority
    private static final Pattern[] TOKEN_PATTERNS = {
        Pattern.compile("\\s+"),                    // Whitespace (ignored)
        Pattern.compile(";.*"),                     // Comments (ignored)
        Pattern.compile("[A-Za-z_][A-Za-z0-9_]*:"), // Labels
        Pattern.compile("\\$[0-9A-Fa-f]+"),         // Hexadecimal numbers
        Pattern.compile("%[01]+"),                  // Binary numbers
        Pattern.compile("[0-9]+"),                  // Decimal numbers
        Pattern.compile("#"),                       // Immediate prefix
        Pattern.compile(","),                       // Comma separator
        Pattern.compile("\\+"),                     // Plus operator
        Pattern.compile("-"),                       // Minus operator
        Pattern.compile("\\*"),                     // Multiply/current address
        Pattern.compile("/"),                       // Divide operator
        Pattern.compile("\\("),                     // Left parenthesis
        Pattern.compile("\\)"),                     // Right parenthesis
        Pattern.compile("[A-Za-z_][A-Za-z0-9_]*"),  // Identifiers/mnemonics
        Pattern.compile("\"[^\"]*\""),              // Quoted strings
        Pattern.compile("'[^']*'"),                 // Single quoted strings
        Pattern.compile("."),                       // Any other character
    };
    
    private static final int WHITESPACE = 0;
    private static final int COMMENT = 1;
    
    /**
     * Tokenize a line of assembly code into individual tokens.
     * 
     * @param line The line to tokenize
     * @return List of tokens (whitespace and comments removed)
     */
    public List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        
        if (line == null || line.trim().isEmpty()) {
            return tokens;
        }
        
        int position = 0;
        while (position < line.length()) {
            boolean matched = false;
            
            for (int i = 0; i < TOKEN_PATTERNS.length; i++) {
                Pattern pattern = TOKEN_PATTERNS[i];
                Matcher matcher = pattern.matcher(line);
                matcher.region(position, line.length());
                
                if (matcher.lookingAt()) {
                    String token = matcher.group();
                    
                    // Skip whitespace and comments
                    if (i != WHITESPACE && i != COMMENT) {
                        tokens.add(token);
                    }
                    
                    position = matcher.end();
                    matched = true;
                    break;
                }
            }
            
            if (!matched) {
                // This should not happen with the catch-all pattern, but just in case
                throw new IllegalArgumentException("Unable to tokenize at position " + position + ": " + line.substring(position));
            }
        }
        
        return tokens;
    }
    
    /**
     * Check if a token is a numeric literal.
     */
    public boolean isNumericToken(String token) {
        return token.matches("\\$[0-9A-Fa-f]+") ||  // Hex
               token.matches("%[01]+") ||           // Binary
               token.matches("[0-9]+");             // Decimal
    }
    
    /**
     * Check if a token is a label (ends with :).
     */
    public boolean isLabelToken(String token) {
        return token.endsWith(":");
    }
    
    /**
     * Check if a token is an identifier (instruction, label reference, etc.).
     */
    public boolean isIdentifierToken(String token) {
        return token.matches("[A-Za-z_][A-Za-z0-9_]*");
    }
    
    /**
     * Check if a token is the immediate addressing prefix (#).
     */
    public boolean isImmediatePrefix(String token) {
        return "#".equals(token);
    }
    
    /**
     * Check if a token is a string literal.
     */
    public boolean isStringToken(String token) {
        return (token.startsWith("\"") && token.endsWith("\"")) ||
               (token.startsWith("'") && token.endsWith("'"));
    }
    
    /**
     * Get the string content from a string literal token (removes quotes).
     */
    public String getStringContent(String stringToken) {
        if (isStringToken(stringToken)) {
            return stringToken.substring(1, stringToken.length() - 1);
        }
        return stringToken;
    }
    
    /**
     * Parse a numeric token to its integer value.
     * 
     * @param token The numeric token to parse
     * @return The integer value
     * @throws NumberFormatException if the token is not a valid number
     */
    public int parseNumericToken(String token) throws NumberFormatException {
        if (token.startsWith("$")) {
            return Integer.parseInt(token.substring(1), 16);
        } else if (token.startsWith("%")) {
            return Integer.parseInt(token.substring(1), 2);
        } else {
            return Integer.parseInt(token);
        }
    }
    
    /**
     * Validate that a numeric token is within the specified range.
     * 
     * @param token The numeric token
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return true if valid, false otherwise
     */
    public boolean isValidNumericRange(String token, int min, int max) {
        try {
            int value = parseNumericToken(token);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Join tokens back into a string (useful for error messages).
     */
    public String joinTokens(List<String> tokens) {
        return String.join(" ", tokens);
    }
    
    /**
     * Extract operand tokens from a tokenized line (everything after the mnemonic).
     * 
     * @param tokens The complete token list
     * @param mnemonicIndex The index of the mnemonic token
     * @return List of operand tokens
     */
    public List<String> extractOperandTokens(List<String> tokens, int mnemonicIndex) {
        if (mnemonicIndex + 1 >= tokens.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(tokens.subList(mnemonicIndex + 1, tokens.size()));
    }
}

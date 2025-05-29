package assembler.ai;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * AI-powered assembly code generator using OpenAI API.
 * Generates Motorola 6800 assembly code based on user descriptions.
 */
public class AIAssemblyGenerator {
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    private ObjectMapper objectMapper;
    private String apiKey;
    
    /**
     * Constructor that initializes the AI service with an API key.
     * @param apiKey The OpenAI API key
     */
    public AIAssemblyGenerator(String apiKey) {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }
    
    /**
     * Sets the API key.
     * @param apiKey The OpenAI API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * Checks if the AI service is properly initialized.
     * @return true if service is ready, false otherwise
     */
    public boolean isInitialized() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * Generates Motorola 6800 assembly code based on user description.
     * @param description User's description of what the program should do
     * @return Generated assembly code as a string
     * @throws Exception if API call fails or service not initialized
     */
    public String generateAssemblyCode(String description) throws Exception {
        if (!isInitialized()) {
            throw new IllegalStateException("AI service not initialized. Please set API key first.");
        }
        
        String requestBody = createRequestBody(description);          Request request = new Request.Builder()
            .url(OPENAI_API_URL)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(requestBody, JSON))
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI API call failed: " + response.code() + " " + response.message());
            }
            
            String responseBody = response.body().string();
            return extractAssemblyCodeFromResponse(responseBody);
        }
    }
    
    /**
     * Creates the JSON request body for OpenAI API.
     */
    private String createRequestBody(String description) {
        String systemPrompt = createSystemPrompt();
        String userPrompt = createUserPrompt(description);
        
        return "{\n" +
               "  \"model\": \"" + MODEL + "\",\n" +
               "  \"messages\": [\n" +
               "    {\n" +
               "      \"role\": \"system\",\n" +
               "      \"content\": " + escapeJson(systemPrompt) + "\n" +
               "    },\n" +
               "    {\n" +
               "      \"role\": \"user\",\n" +
               "      \"content\": " + escapeJson(userPrompt) + "\n" +
               "    }\n" +
               "  ],\n" +
               "  \"max_tokens\": 1000,\n" +
               "  \"temperature\": 0.3\n" +
               "}";
    }
    
    /**
     * Escapes JSON string content.
     */
    private String escapeJson(String input) {
        return "\"" + input.replace("\\", "\\\\")
                         .replace("\"", "\\\"")
                         .replace("\n", "\\n")
                         .replace("\r", "\\r")
                         .replace("\t", "\\t") + "\"";
    }
    
    /**
     * Extracts assembly code from OpenAI API response.
     */
    private String extractAssemblyCodeFromResponse(String responseBody) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            
            if (choices == null || choices.size() == 0) {
                throw new RuntimeException("No response from AI service");
            }
            
            JsonNode message = choices.get(0).get("message");
            String content = message.get("content").asText();
            
            return extractAssemblyCode(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage());
        }
    }
    
    /**
     * Creates the system prompt that defines the AI's role and constraints.
     */
    private String createSystemPrompt() {
        return "You are a Motorola 6800 assembly language expert. " +
               "Generate only valid Motorola 6800 assembly code based on user requirements. " +
               "Use proper instruction syntax, addressing modes, and include necessary directives like ORG and END. " +
               "Available instructions include: LDA, LDB, LDX, STA, STB, STX, ADDA, ADDB, SUBA, SUBB, " +
               "INCA, INCB, DECA, DECB, ANDA, ANDB, ORA, ORB, EORA, EORB, CMP, CMPA, CMPB, " +
               "JMP, JSR, RTS, BRA, BEQ, BNE, BCC, BCS, BPL, BMI, BVC, BVS, " +
               "NOP, SWI, WAI, RTI, SEC, CLC, SEI, CLI, SEV, CLV, TAB, TBA, etc. " +
               "Always start with ORG directive and end with END. " +
               "Include comments explaining the code. " +
               "Respond with ONLY the assembly code, no explanations.";
    }
    
    /**
     * Creates the user prompt based on the description.
     */
    private String createUserPrompt(String description) {
        return "Generate Motorola 6800 assembly code for the following requirement: " + description;
    }
    
    /**
     * Extracts assembly code from AI response, removing any markdown formatting.
     */
    private String extractAssemblyCode(String response) {
        // Remove markdown code blocks if present
        response = response.replaceAll("```[a-zA-Z]*\\n", "");
        response = response.replaceAll("```", "");
        
        // Remove any leading/trailing whitespace
        response = response.trim();
        
        return response;
    }
}

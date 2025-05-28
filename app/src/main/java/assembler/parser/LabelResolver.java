package assembler.parser;

import assembler.core.Label;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles label definition and resolution during the two-pass assembly process.
 * First pass: collects all label definitions with their addresses.
 * Second pass: resolves all label references in instructions.
 */
public class LabelResolver {
    private final Map<String, Label> labels;
    private final List<LabelReference> unresolvedReferences;
    
    /**
     * Represents an unresolved label reference that needs to be resolved in the second pass.
     */
    private static class LabelReference {
        final AssemblyLine line;
        final String labelName;
        final int position; // Position in the operand where the label appears
        
        LabelReference(AssemblyLine line, String labelName, int position) {
            this.line = line;
            this.labelName = labelName;
            this.position = position;
        }
    }
    
    /**
     * Creates a new LabelResolver.
     */
    public LabelResolver() {
        this.labels = new HashMap<>();
        this.unresolvedReferences = new ArrayList<>();
    }
    
    /**
     * Adds a label definition with its address.
     * 
     * @param labelName The name of the label
     * @param address The address where the label is defined
     * @throws ParseException if the label is already defined
     */
    public void addLabel(String labelName, int address) throws ParseException {
        if (labels.containsKey(labelName)) {
            throw new ParseException("Label '" + labelName + "' is already defined at address " + 
                                     Integer.toHexString(labels.get(labelName).getAddress()));
        }
          Label label = Label.createResolved(labelName, address);
        labels.put(labelName, label);
    }
    
    /**
     * Adds a label from a Label object.
     * 
     * @param label The label to add
     * @throws ParseException if the label is already defined
     */
    public void addLabel(Label label) throws ParseException {
        addLabel(label.getName(), label.getAddress());
    }
    
    /**
     * Checks if a label is defined.
     * 
     * @param labelName The name of the label to check
     * @return true if the label is defined, false otherwise
     */
    public boolean isLabelDefined(String labelName) {
        return labels.containsKey(labelName);
    }
    
    /**
     * Gets the address of a defined label.
     * 
     * @param labelName The name of the label
     * @return The address of the label
     * @throws ParseException if the label is not defined
     */
    public int getLabelAddress(String labelName) throws ParseException {
        Label label = labels.get(labelName);
        if (label == null) {
            throw new ParseException("Undefined label: " + labelName);
        }
        return label.getAddress();
    }
    
    /**
     * Gets a label by name.
     * 
     * @param labelName The name of the label
     * @return The Label object, or null if not found
     */
    public Label getLabel(String labelName) {
        return labels.get(labelName);
    }
    
    /**
     * Gets all defined labels.
     * 
     * @return A map of label names to Label objects
     */
    public Map<String, Label> getAllLabels() {
        return new HashMap<>(labels);
    }
    
    /**
     * Adds an unresolved label reference to be resolved in the second pass.
     * 
     * @param line The assembly line containing the reference
     * @param labelName The name of the referenced label
     * @param position The position in the operand where the label appears
     */
    public void addLabelReference(AssemblyLine line, String labelName, int position) {
        unresolvedReferences.add(new LabelReference(line, labelName, position));
    }
    
    /**
     * Resolves all label references in the given assembly line.
     * This method checks if the instruction operand contains any label references
     * and replaces them with actual addresses.
     * 
     * @param line The assembly line to process
     * @throws ParseException if any referenced label is undefined
     */
    public void resolveReferences(AssemblyLine line) throws ParseException {
        if (!line.hasInstruction()) {
            return;
        }
        
        String operand = line.getInstruction().getOperand();
        if (operand == null || operand.isEmpty()) {
            return;
        }
        
        // Check if operand is a label reference (not a numeric value)
        if (isLabelReference(operand)) {
            String labelName = operand.trim();
            if (!isLabelDefined(labelName)) {
                throw new ParseException("Undefined label '" + labelName + 
                                         "' referenced at line " + line.getLineNumber());
            }
            
            int labelAddress = getLabelAddress(labelName);
            // Update the instruction with the resolved address
            line.getInstruction().setResolvedOperand(labelAddress);
        }
        // Handle complex expressions with labels (like "LABEL+1", "LABEL-2", etc.)
        else if (containsLabelReference(operand)) {
            String resolvedOperand = resolveComplexExpression(operand);
            line.getInstruction().setOperand(resolvedOperand);
        }
    }
    
    /**
     * Resolves all pending label references.
     * This should be called during the second pass after all labels are defined.
     * 
     * @throws ParseException if any referenced label is undefined
     */
    public void resolveAllReferences() throws ParseException {
        for (LabelReference ref : unresolvedReferences) {
            resolveReferences(ref.line);
        }
        unresolvedReferences.clear();
    }
    
    /**
     * Checks if a string is a potential label reference.
     * A label reference is a string that:
     * - Starts with a letter or underscore
     * - Contains only letters, digits, and underscores
     * - Is not a numeric literal
     * 
     * @param operand The operand string to check
     * @return true if it looks like a label reference
     */
    private boolean isLabelReference(String operand) {
        if (operand == null || operand.isEmpty()) {
            return false;
        }
        
        operand = operand.trim();
        
        // Check for numeric literals
        if (operand.startsWith("$") || operand.startsWith("%") || 
            operand.matches("\\d+")) {
            return false;
        }
        
        // Check for addressing mode prefixes
        if (operand.startsWith("#") || operand.startsWith("@")) {
            operand = operand.substring(1);
        }
        
        // Remove indexing syntax if present (e.g., "LABEL,X")
        if (operand.contains(",")) {
            operand = operand.substring(0, operand.indexOf(","));
        }
        
        // Check if it's a valid identifier
        return operand.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }
    
    /**
     * Checks if an operand contains any label references.
     * 
     * @param operand The operand to check
     * @return true if the operand contains label references
     */
    private boolean containsLabelReference(String operand) {
        if (operand == null || operand.isEmpty()) {
            return false;
        }
        
        // Look for label-like patterns in expressions
        return operand.matches(".*[a-zA-Z_][a-zA-Z0-9_]*.*");
    }
    
    /**
     * Resolves complex expressions containing labels.
     * For example: "LABEL+1", "LABEL-2", etc.
     * 
     * @param expression The expression to resolve
     * @return The resolved expression with numeric values
     * @throws ParseException if any referenced label is undefined
     */
    private String resolveComplexExpression(String expression) throws ParseException {
        // This is a simplified implementation
        // A full implementation would parse arithmetic expressions
        
        // For now, handle simple cases like "LABEL+offset" or "LABEL-offset"
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            if (parts.length == 2) {
                String labelPart = parts[0].trim();
                String offsetPart = parts[1].trim();
                
                if (isLabelReference(labelPart)) {
                    int labelAddress = getLabelAddress(labelPart);
                    int offset = parseNumericValue(offsetPart);
                    return String.valueOf(labelAddress + offset);
                }
            }
        } else if (expression.contains("-")) {
            String[] parts = expression.split("-");
            if (parts.length == 2) {
                String labelPart = parts[0].trim();
                String offsetPart = parts[1].trim();
                
                if (isLabelReference(labelPart)) {
                    int labelAddress = getLabelAddress(labelPart);
                    int offset = parseNumericValue(offsetPart);
                    return String.valueOf(labelAddress - offset);
                }
            }
        }
        
        // If no arithmetic, just resolve the label
        if (isLabelReference(expression)) {
            int address = getLabelAddress(expression);
            return String.valueOf(address);
        }
        
        return expression; // Return unchanged if no labels found
    }
    
    /**
     * Parses a numeric value in various formats (decimal, hex, binary).
     * 
     * @param value The string representation of the value
     * @return The parsed integer value
     * @throws ParseException if the value format is invalid
     */
    private int parseNumericValue(String value) throws ParseException {
        try {
            value = value.trim();
            if (value.startsWith("$")) {
                // Hexadecimal
                return Integer.parseInt(value.substring(1), 16);
            } else if (value.startsWith("%")) {
                // Binary
                return Integer.parseInt(value.substring(1), 2);
            } else {
                // Decimal
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid numeric value: " + value);
        }
    }
    
    /**
     * Clears all labels and references. Used for parsing a new source file.
     */
    public void clear() {
        labels.clear();
        unresolvedReferences.clear();
    }
    
    /**
     * Returns the number of defined labels.
     */
    public int getLabelCount() {
        return labels.size();
    }
    
    /**
     * Returns the number of unresolved references.
     */
    public int getUnresolvedCount() {
        return unresolvedReferences.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LabelResolver: ").append(labels.size()).append(" labels defined\n");
        for (Label label : labels.values()) {
            sb.append("  ").append(label.toString()).append("\n");
        }
        return sb.toString();
    }
}

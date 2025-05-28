package assembler.parser;

/**
 * Exception thrown when assembly source code parsing fails.
 * Contains information about the specific parsing error that occurred.
 */
public class ParseException extends Exception {
    
    private final int lineNumber;
    private final int columnNumber;
    
    /**
     * Creates a new ParseException with a message.
     * 
     * @param message The error message
     */
    public ParseException(String message) {
        super(message);
        this.lineNumber = -1;
        this.columnNumber = -1;
    }
    
    /**
     * Creates a new ParseException with a message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = -1;
        this.columnNumber = -1;
    }
    
    /**
     * Creates a new ParseException with location information.
     * 
     * @param message The error message
     * @param lineNumber The line number where the error occurred (1-based)
     * @param columnNumber The column number where the error occurred (1-based)
     */
    public ParseException(String message, int lineNumber, int columnNumber) {
        super(message);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    
    /**
     * Gets the line number where the error occurred.
     * 
     * @return The line number (1-based), or -1 if not available
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Gets the column number where the error occurred.
     * 
     * @return The column number (1-based), or -1 if not available
     */
    public int getColumnNumber() {
        return columnNumber;
    }
    
    /**
     * Gets whether location information is available.
     * 
     * @return true if line and column numbers are available
     */
    public boolean hasLocationInfo() {
        return lineNumber > 0 && columnNumber > 0;
    }
    
    @Override
    public String getMessage() {
        if (hasLocationInfo()) {
            return String.format("Line %d, Column %d: %s", lineNumber, columnNumber, super.getMessage());
        } else if (lineNumber > 0) {
            return String.format("Line %d: %s", lineNumber, super.getMessage());
        } else {
            return super.getMessage();
        }
    }
}

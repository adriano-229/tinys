package tinys.exceptions;

public class SyntacticException extends RuntimeException {
    private final int line;
    private final int column;
    private final String description;

    public SyntacticException(int line, int column, String description) {
        super(description + " EN LINEA " + line + " (COLUMNA " + column + ")");
        this.line = line;
        this.column = column;
        this.description = description;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getDescription() {
        return description;
    }
}


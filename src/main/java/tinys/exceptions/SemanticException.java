package tinys.exceptions;

import lombok.Getter;

@Getter
public class SemanticException extends RuntimeException {
    private final int line;
    private final int column;

    public SemanticException(int line, int column, String description) {
        super(description + " EN LINEA " + line + " (COLUMNA " + column + ")");
        this.line = line;
        this.column = column;
    }
}

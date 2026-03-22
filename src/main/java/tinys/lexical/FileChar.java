package tinys.lexical;

public record FileChar(char value, int line, int col) {
    public int getLine() {
        return line;
    }

    public int getColumn() {
        return col;
    }
}

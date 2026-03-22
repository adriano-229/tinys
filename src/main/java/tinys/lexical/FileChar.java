package tinys.lexical;

public record FileChar(char value, int line, int col) {

    public char getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }
}

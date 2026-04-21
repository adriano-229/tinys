package tinys.lexical;

public record Token(TokenType type, String value, int line, int col) {
}


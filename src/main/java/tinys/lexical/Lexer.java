package tinys.lexical;

import tinys.exceptions.LexicalException;

public class Lexer {

    private final FileReader reader;
    private FileChar currentChar;
    private FileChar nextChar;

    public Lexer(FileReader reader) {
        this.reader = reader;
        this.currentChar = reader.nextChar();
        this.nextChar = reader.nextChar();
    }

    public Token nextToken() {
        while (currentChar != null) {
            char value = currentChar.getValue();

            if (isWhitespaceChar(value)) {
                advance();
                continue;
            }

            if (startsWith('/', '/')) {
                skipSingleLineComment();
                continue;
            }

            if (startsWith('/', '*')) {
                skipMultiLineComment();
                continue;
            }

            if (isIdentifierStart(value)) {
                return readIdentifier();
            }

            if (isAsciiDigit(value)) {
                return readIntegerLiteral();
            }

            if (value == '"') {
                return readStringLiteral();
            }

            Token symbolToken = readSymbolOrOperator();
            if (symbolToken != null) {
                return symbolToken;
            }

            throw errorAtCurrent("CARACTER INVALIDO " + value);
        }

        return new Token(TokenType.EOF, "", 0, 0);
    }

    private void advance() {
        currentChar = nextChar;
        nextChar = reader.nextChar();
    }

    private Token readIdentifier() {
        int line = currentChar.getLine();
        int column = currentChar.getCol();
        StringBuilder lexeme = new StringBuilder();

        while (currentChar != null && isIdentifierPart(currentChar.getValue())) {
            lexeme.append(currentChar.getValue());
            advance();
        }

        String text = lexeme.toString();
        return new Token(resolveIdentifierType(text), text, line, column);
    }

    private Token readIntegerLiteral() {
        int line = currentChar.getLine();
        int column = currentChar.getCol();
        StringBuilder lexeme = new StringBuilder();

        while (currentChar != null && isAsciiDigit(currentChar.getValue())) {
            lexeme.append(currentChar.getValue());
            advance();
        }

        return new Token(TokenType.INT_LIT, lexeme.toString(), line, column);
    }

    private Token readStringLiteral() {
        int line = currentChar.getLine();
        int column = currentChar.getCol();
        StringBuilder value = new StringBuilder();

        advance();
        while (currentChar != null) {
            char ch = currentChar.getValue();

            if (ch == '"') {
                advance();
                return new Token(TokenType.STR_LIT, value.toString(), line, column);
            }

            if (ch == '\\') {  // '\\' is a single backslash character, not an escape sequence
                advance();
                if (currentChar == null) {
                    throw new LexicalException(line, column, "CADENA SIN CERRAR");
                }

                value.append(resolveEscape(currentChar.getValue(), line, column));
                ensureStringLength(value.length(), line, column);
                advance();
                continue;
            }

            if (ch == '\n' || ch == '\r') {
                throw new LexicalException(line, column, "CADENA SIN CERRAR");
            }

            if (ch == '\0') {
                throw new LexicalException(line, column, "CADENA CONTIENE CARACTER NULO");
            }

            value.append(ch);
            ensureStringLength(value.length(), line, column);
            advance();
        }

        throw new LexicalException(line, column, "CADENA SIN CERRAR");
    }

    private char resolveEscape(char value, int line, int column) {
        return switch (value) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'r' -> '\r';
            case '"' -> '"';
            case '\'' -> '\'';
            case '\\' -> '\\';
            default -> throw new LexicalException(
                    line,
                    column,
                    "ESCAPE INVALIDO \\" + value
            );
        };
    }

    private void ensureStringLength(int length, int line, int column) {
        if (length > 1024) {
            throw new LexicalException(line, column, "CADENA SUPERA 1024 CARACTERES");
        }
    }

    private Token readSymbolOrOperator() {
        return switch (currentChar.getValue()) {
            case ':' -> singleCharToken(TokenType.COLON);
            case ';' -> singleCharToken(TokenType.SEMICOLON);
            case '.' -> singleCharToken(TokenType.DOT);
            case ',' -> singleCharToken(TokenType.COMMA);
            case '(' -> singleCharToken(TokenType.BRACKET_OPEN);
            case ')' -> singleCharToken(TokenType.BRACKET_CLOSE);
            case '[' -> singleCharToken(TokenType.SQR_BRACKET_OPEN);
            case ']' -> singleCharToken(TokenType.SQR_BRACKET_CLOSE);
            case '{' -> singleCharToken(TokenType.BRACES_OPEN);
            case '}' -> singleCharToken(TokenType.BRACES_CLOSE);
            case '=' -> readEqualsOrComparison();
            case '!' -> readBangOrComparison();
            case '<', '>' -> readComparison();
            case '+', '-' -> singleCharToken(TokenType.ADD_OP);
            case '*', '%', '/' -> singleCharToken(TokenType.MULT_OP);
            default -> null;
        };
    }

    private Token readEqualsOrComparison() {
        int line = currentChar.getLine();
        int column = currentChar.getCol();
        if (nextChar != null && nextChar.getValue() == '=') {
            advance();
            advance();
            return new Token(TokenType.COMP_OP, "==", line, column);
        }

        advance();
        return new Token(TokenType.EQUAL, "=", line, column);
    }

    private Token readBangOrComparison() {
        int line = currentChar.getLine();
        int column = currentChar.getCol();
        if (nextChar != null && nextChar.getValue() == '=') {
            advance();
            advance();
            return new Token(TokenType.COMP_OP, "!=", line, column);
        }

        advance();
        return new Token(TokenType.UNARY_OP, "!", line, column);
    }

    private Token readComparison() {
        int line = currentChar.getLine();
        int column = currentChar.getCol();
        char value = currentChar.getValue();

        if (nextChar != null && nextChar.getValue() == '=') {
            String lexeme = value == '<' ? "<=" : ">=";
            advance();
            advance();
            return new Token(TokenType.COMP_OP, lexeme, line, column);
        }

        String lexeme = String.valueOf(value);
        advance();
        return new Token(TokenType.COMP_OP, lexeme, line, column);
    }

    private Token singleCharToken(TokenType tokenType) {
        int line = currentChar.getLine();
        int column = currentChar.getCol();
        String value = String.valueOf(currentChar.getValue());
        advance();
        return new Token(tokenType, value, line, column);
    }

    private boolean startsWith(char first, char second) {
        return currentChar != null
                && nextChar != null
                && currentChar.getValue() == first
                && nextChar.getValue() == second;
    }

    private void skipSingleLineComment() {
        advance();
        advance();

        while (currentChar != null && currentChar.getValue() != '\n') {
            advance();
        }
    }

    private void skipMultiLineComment() {
        int startLine = currentChar.getLine();
        int startColumn = currentChar.getCol();

        advance();
        advance();

        while (currentChar != null) {
            if (startsWith('*', '/')) {
                advance();
                advance();
                return;
            }
            advance();
        }

        throw new LexicalException(startLine, startColumn, "COMENTARIO MULTILINEA SIN CERRAR");
    }

    private TokenType resolveIdentifierType(String value) {
        return switch (value) {
            case "class" -> TokenType.CLASS;
            case "impl" -> TokenType.IMPL;
            case "if" -> TokenType.IF;
            case "else" -> TokenType.ELSE;
            case "fn" -> TokenType.FN;
            case "ret" -> TokenType.RET;
            case "while" -> TokenType.WHILE;
            case "new" -> TokenType.NEW;
            case "st" -> TokenType.ST;
            case "pub" -> TokenType.PUB;
            case "self" -> TokenType.SELF;
            case "div" -> TokenType.DIV;
            case "void" -> TokenType.VOID;
            case "Array" -> TokenType.ARRAY;
            case "for" -> TokenType.FOR;
            case "in" -> TokenType.IN;
            case "true", "false" -> TokenType.BOOL_LIT;
            case "nil" -> TokenType.NIL_LIT;
            default -> isUpperAsciiLetter(value.charAt(0)) ? TokenType.CLASS_ID : TokenType.METHOD_ID;
        };
    }

    private boolean isWhitespaceChar(char value) {
        return value == ' ' || value == '\n' || value == '\r' || value == '\t' || value == (char) 11;
    }

    private boolean isIdentifierStart(char value) {
        return isAsciiLetter(value);
    }

    private boolean isIdentifierPart(char value) {
        return isAsciiLetter(value) || isAsciiDigit(value) || value == '_';
    }

    private boolean isAsciiLetter(char value) {
        return isLowerAsciiLetter(value) || isUpperAsciiLetter(value);
    }

    private boolean isLowerAsciiLetter(char value) {
        return value >= 'a' && value <= 'z';
    }

    private boolean isUpperAsciiLetter(char value) {
        return value >= 'A' && value <= 'Z';
    }

    private boolean isAsciiDigit(char value) {
        return value >= '0' && value <= '9';
    }

    private LexicalException errorAtCurrent(String description) {
        return new LexicalException(currentChar.getLine(), currentChar.getCol(), description);
    }
}


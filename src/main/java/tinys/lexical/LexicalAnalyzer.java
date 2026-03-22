package tinys.lexical;

import tinys.exceptions.LexicalException;

public class LexicalAnalyzer implements ILexicalAnalyzer {

    private final FileReader reader;
    private FileChar currentChar;

    public LexicalAnalyzer(FileReader reader) {
        this.reader = reader;
        this.currentChar = reader.nextChar();
    }

    private void advance() {
        currentChar = reader.nextChar();
    }

    @Override
    public Token nextToken() {

        while (currentChar != null) {

            char c = currentChar.value();

            // 1. skip whitespace
            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }

            // 2. identifier
            if (Character.isLetter(c)) {
                return identifier();
            }

            // 3. error
            throw new LexicalException(
                    "LINEA " + currentChar.getLine() +
                            " (COLUMNA " + currentChar.getColumn() +
                            ") | CARACTER INVALIDO: " + c
            );
        }

        return new Token(TokenType.EOF, "", 0, 0);
    }

    private Token identifier() {
        int startRow = currentChar.getLine();
        int startColumn = currentChar.getColumn();
        StringBuilder lexeme = new StringBuilder();

        while (currentChar != null) {
            char value = currentChar.value();
            if (!Character.isLetterOrDigit(value) && value != '_') {
                break;
            }
            lexeme.append(value);
            advance();
        }

        String value = lexeme.toString();
        return new Token(resolveIdentifierType(value), value, startRow, startColumn);
    }

    private TokenType resolveIdentifierType(String value) {
        return switch (value) {
            case "class" -> TokenType.CLASS;
            case "impl" -> TokenType.IMPL;
            case "new" -> TokenType.NEW;
            case "if" -> TokenType.IF;
            case "else" -> TokenType.ELSE;
            case "st" -> TokenType.ST;
            case "fn" -> TokenType.FN;
            case "ret" -> TokenType.RET;
            case "while" -> TokenType.WHILE;
            case "pub" -> TokenType.PUB;
            case "for" -> TokenType.FOR;
            case "in" -> TokenType.IN;
            case "true", "false" -> TokenType.BOOL_LIT;
            case "nil" -> TokenType.NIL_LIT;
            default -> Character.isUpperCase(value.charAt(0)) ? TokenType.CLASS_ID : TokenType.METHOD_ID;
        };
    }
}

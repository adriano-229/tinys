package tinys.lexical;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tinys.exceptions.LexicalException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @TempDir
    Path tempDir;

    @Test
    void tokenizesProvidedOkCase() throws IOException {
        List<Token> tokens = lexFile(Path.of("src/test/resources/cases/ok_basico.s"));

        assertEquals(TokenType.CLASS, tokens.get(0).type());
        assertEquals("class", tokens.get(0).value());
        assertEquals(TokenType.CLASS_ID, tokens.get(1).type());
        assertEquals("Persona", tokens.get(1).value());
        assertEquals(TokenType.EOF, tokens.getLast().type());
    }

    @Test
    void failsForProvidedUnclosedStringCase() {
        LexicalException exception = assertThrows(
                LexicalException.class,
                () -> lexFile(Path.of("src/test/resources/cases/error_cadena_sin_cerrar.s"))
        );

        assertEquals("CADENA SIN CERRAR", exception.getDescription());
    }

    @Test
    void failsForProvidedUnclosedMultilineCommentCase() {
        LexicalException exception = assertThrows(
                LexicalException.class,
                () -> lexFile(Path.of("src/test/resources/cases/error_comentario_multilinea.s"))
        );

        assertEquals("COMENTARIO MULTILINEA SIN CERRAR", exception.getDescription());
    }

    @Test
    void classifiesKeywordsAndIdentifiers() throws IOException {
        List<Token> tokens = lexInline("class Persona self var_1 Array div Int Bool Str start");

        assertEquals(TokenType.CLASS, tokens.get(0).type());
        assertEquals(TokenType.CLASS_ID, tokens.get(1).type());
        assertEquals(TokenType.SELF, tokens.get(2).type());
        assertEquals(TokenType.METHOD_ID, tokens.get(3).type());
        assertEquals(TokenType.ARRAY, tokens.get(4).type());
        assertEquals(TokenType.DIV, tokens.get(5).type());
        assertEquals(TokenType.TYPE_INT, tokens.get(6).type());
        assertEquals(TokenType.TYPE_BOOL, tokens.get(7).type());
        assertEquals(TokenType.TYPE_STR, tokens.get(8).type());
        assertEquals(TokenType.METHOD_ID, tokens.get(9).type());
    }

    @Test
    void supportsWhitespaceSetIncludingVerticalTab() throws IOException {
        String input = "class\u000BPersona\t\nret\rnil";
        List<Token> tokens = lexInline(input);

        assertEquals(TokenType.CLASS, tokens.get(0).type());
        assertEquals(TokenType.CLASS_ID, tokens.get(1).type());
        assertEquals(TokenType.RET, tokens.get(2).type());
        assertEquals(TokenType.NIL_LIT, tokens.get(3).type());
    }

    @Test
    void tokenizesEscapedStringLiteral() throws IOException {
        List<Token> tokens = lexInline("\"linea\\ncon\\tescape\\\\ok\"");

        assertEquals(2, tokens.size());
        assertEquals(TokenType.STR_LIT, tokens.getFirst().type());
        assertTrue(tokens.getFirst().value().contains("linea\ncon\tescape\\ok"));
    }

    @Test
    void failsForInvalidStringEscape() {
        LexicalException exception = assertThrows(
                LexicalException.class,
                () -> lexInline("\"hola\\x\"")
        );

        assertEquals("ESCAPE INVALIDO \\x", exception.getDescription());
    }

    @Test
    void failsForStringLongerThanLimit() {
        String tooLong = "\"" + "a".repeat(1025) + "\"";

        LexicalException exception = assertThrows(
                LexicalException.class,
                () -> lexInline(tooLong)
        );

        assertEquals("CADENA SUPERA 1024 CARACTERES", exception.getDescription());
    }

    @Test
    void tokenizesOperatorsAndSymbols() throws IOException {
        List<Token> tokens = lexInline("== != <= >= = ! + - * / % : ; . , ( ) [ ] { }");

        List<TokenType> types = new ArrayList<>();
        for (Token token : tokens) {
            types.add(token.type());
        }

        assertEquals(List.of(
                TokenType.COMP_OP,
                TokenType.COMP_OP,
                TokenType.COMP_OP,
                TokenType.COMP_OP,
                TokenType.EQUAL,
                TokenType.UNARY_OP,
                TokenType.ADD_OP,
                TokenType.ADD_OP,
                TokenType.MULT_OP,
                TokenType.MULT_OP,
                TokenType.MULT_OP,
                TokenType.COLON,
                TokenType.SEMICOLON,
                TokenType.DOT,
                TokenType.COMMA,
                TokenType.PAR_OPEN,
                TokenType.PAR_CLOSE,
                TokenType.SQR_BRACKET_OPEN,
                TokenType.SQR_BRACKET_CLOSE,
                TokenType.BRACES_OPEN,
                TokenType.BRACES_CLOSE,
                TokenType.EOF
        ), types);
    }

    @Test
    void tracksLineAndColumnStartingAtOne() throws IOException {
        List<Token> tokens = lexInline("class A\nret nil");

        assertEquals(1, tokens.get(0).line());
        assertEquals(1, tokens.get(0).column());
        assertEquals(2, tokens.get(2).line());
        assertEquals(1, tokens.get(2).column());
    }

    private List<Token> lexInline(String content) throws IOException {
        Path file = tempDir.resolve("inline.s");
        Files.writeString(file, content);
        return lexFile(file);
    }

    private List<Token> lexFile(Path file) throws IOException {
        Lexer lexer = new Lexer(new FileReader(file.toString()));
        List<Token> tokens = new ArrayList<>();

        while (true) {
            Token token = lexer.nextToken();
            tokens.add(token);
            if (token.type() == TokenType.EOF) {
                break;
            }
        }

        return tokens;
    }
}



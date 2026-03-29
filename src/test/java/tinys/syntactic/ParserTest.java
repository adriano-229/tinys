package tinys.syntactic;

import org.junit.jupiter.api.Test;
import tinys.exceptions.SyntacticException;
import tinys.lexical.FileReader;
import tinys.lexical.Lexer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    @Test
    void testParseStartOnlyProgram() {
        String code = "start { ret; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseDefinitionsThenStartProgram() {
        String code = "class Persona { } impl Persona { } start { ; }";
        testParsingSuccess(code);
    }

    @Test
    void testRejectsDefinitionsWithoutStart() {
        String code = "class Persona { }";
        assertThrows(SyntacticException.class, () -> parseCodeRaw(code));
    }

    @Test
    void testParseSimpleClassDefinition() {
        String code = "class Persona { }";
        testParsingSuccess(code);
    }

    @Test
    void testParseClassWithInheritance() {
        String code = "class Persona : Object { }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMultipleClasses() {
        String code = "class Persona { } class Animal { }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplBlock() {
        String code = "impl Persona { }";
        testParsingSuccess(code);
    }

    @Test
    void testParseClassWithAttribute() {
        String code = "class Persona { Int edad; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseClassWithPublicAttribute() {
        String code = "class Persona { pub Int edad; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseClassWithMultipleAttributes() {
        String code = "class Persona { Int edad; Str nombre; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithMethod() {
        String code = "impl Persona { fn test() { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithMethodReturningType() {
        String code = "impl Persona { fn Str getName() { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithStaticMethod() {
        String code = "impl Persona { st fn Int sum() { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithConstructor() {
        String code = "impl Persona { .() { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithMethodParameters() {
        String code = "impl Persona { fn Str getName(Str name, Int age) { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMethodWithLocalVariables() {
        String code = "impl Persona { fn test() { Int x; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMethodWithMultipleLocalVariables() {
        String code = "impl Persona { fn test() { Int x, y; Str name; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseSimpleExpression() {
        String code = "impl Persona { fn test() { (1); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseAssignment() {
        String code = "impl Persona { fn test() { x = 5; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseSelfAssignment() {
        String code = "impl Persona { fn test() { self.x = 5; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseIfStatement() {
        String code = "impl Persona { fn test() { if (true) { } } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseIfElseStatement() {
        String code = "impl Persona { fn test() { if (true) { } else { } } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseWhileStatement() {
        String code = "impl Persona { fn test() { while (true) { } } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseForStatement() {
        String code = "impl Persona { fn test() { for (Int idx in items) { } } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseReturnStatement() {
        String code = "impl Persona { fn test() { ret 5; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseEmptyReturnStatement() {
        String code = "impl Persona { fn test() { ret; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseLiteralInteger() {
        String code = "impl Persona { fn test() { (42); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseLiteralBoolean() {
        String code = "impl Persona { fn test() { (true); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseLiteralString() {
        String code = "impl Persona { fn test() { (\"hello\"); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseLiteralNil() {
        String code = "impl Persona { fn test() { (nil); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseUnaryExpression() {
        String code = "impl Persona { fn test() { (-5); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseUnaryNotExpression() {
        String code = "impl Persona { fn test() { (!true); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseBinaryAddition() {
        String code = "impl Persona { fn test() { (5 + 3); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseBinarySubtraction() {
        String code = "impl Persona { fn test() { (5 - 3); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseBinaryMultiplication() {
        String code = "impl Persona { fn test() { (5 * 3); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseArrayType() {
        String code = "class Persona { Array Int nums; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseNewExpression() {
        String code = "impl Persona { fn test() { (new Persona()); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseNewArrayExpression() {
        String code = "impl Persona { fn test() { (new Int[10]); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMethodCallExpression() {
        String code = "impl Persona { fn test() { (obj.method()); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseArrayAccessExpression() {
        String code = "impl Persona { fn test() { (arr[0]); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseSelfAccess() {
        String code = "impl Persona { fn test() { (self); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseSelfDotAccess() {
        String code = "impl Persona { fn test() { (self.field); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMissingBrace() {
        String code = "class Persona {";
        assertThrows(SyntacticException.class, () -> parseCode(code));
    }

    @Test
    void testParseMissingClassName() {
        String code = "class { }";
        assertThrows(SyntacticException.class, () -> parseCode(code));
    }

    @Test
    void testParseMultipleSentences() {
        String code = "impl Persona { fn test() { ; ; ; } }";
        testParsingSuccess(code);
    }

    // Helper method
    private void testParsingSuccess(String code) {
        assertDoesNotThrow(() -> parseCode(code));
    }

    private void parseCode(String code) throws IOException {
        String normalizedProgram = normalizeProgram(code);
        parseCodeRaw(normalizedProgram);
    }

    private void parseCodeRaw(String code) throws IOException {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".s");
        try {
            java.nio.file.Files.write(tempFile, code.getBytes());

            FileReader reader = new FileReader(tempFile.toString());
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            parser.parseProgram();
        } finally {
            java.nio.file.Files.delete(tempFile);
        }
    }

    private String normalizeProgram(String code) {
        if (code.matches("(?s).*\\bstart\\b.*")) {
            return code;
        }
        return code + " start { }";
    }
}






package tinys.syntactic;

import org.junit.jupiter.api.Test;
import tinys.exceptions.SyntacticException;
import tinys.lexical.FileReader;
import tinys.lexical.Lexical;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SyntacticAnalyzerTest {

    @Test
    void testParseSimpleClassDefinition() throws IOException {
        String code = "class Persona { }";
        testParsingSuccess(code);
    }

    @Test
    void testParseClassWithInheritance() throws IOException {
        String code = "class Persona : Object { }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMultipleClasses() throws IOException {
        String code = "class Persona { } class Animal { }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplBlock() throws IOException {
        String code = "impl Persona { }";
        testParsingSuccess(code);
    }

    @Test
    void testParseClassWithAttribute() throws IOException {
        String code = "class Persona { Int edad; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseClassWithPublicAttribute() throws IOException {
        String code = "class Persona { pub Int edad; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseClassWithMultipleAttributes() throws IOException {
        String code = "class Persona { Int edad; Str nombre; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithMethod() throws IOException {
        String code = "impl Persona { fn test() { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithMethodReturningType() throws IOException {
        String code = "impl Persona { fn Str getName() { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithStaticMethod() throws IOException {
        String code = "impl Persona { st fn Int sum() { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithConstructor() throws IOException {
        String code = "impl Persona { .() { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseImplWithMethodParameters() throws IOException {
        String code = "impl Persona { fn Str getName(Str name, Int age) { } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMethodWithLocalVariables() throws IOException {
        String code = "impl Persona { fn test() { Int x; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMethodWithMultipleLocalVariables() throws IOException {
        String code = "impl Persona { fn test() { Int x, y; Str name; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseSimpleExpression() throws IOException {
        String code = "impl Persona { fn test() { (1); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseAssignment() throws IOException {
        String code = "impl Persona { fn test() { x = 5; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseSelfAssignment() throws IOException {
        String code = "impl Persona { fn test() { self.x = 5; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseIfStatement() throws IOException {
        String code = "impl Persona { fn test() { if (true) { } } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseIfElseStatement() throws IOException {
        String code = "impl Persona { fn test() { if (true) { } else { } } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseWhileStatement() throws IOException {
        String code = "impl Persona { fn test() { while (true) { } } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseForStatement() throws IOException {
        String code = "impl Persona { fn test() { for (Int idx in items) { } } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseReturnStatement() throws IOException {
        String code = "impl Persona { fn test() { ret 5; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseEmptyReturnStatement() throws IOException {
        String code = "impl Persona { fn test() { ret; } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseLiteralInteger() throws IOException {
        String code = "impl Persona { fn test() { (42); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseLiteralBoolean() throws IOException {
        String code = "impl Persona { fn test() { (true); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseLiteralString() throws IOException {
        String code = "impl Persona { fn test() { (\"hello\"); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseLiteralNil() throws IOException {
        String code = "impl Persona { fn test() { (nil); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseUnaryExpression() throws IOException {
        String code = "impl Persona { fn test() { (-5); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseUnaryNotExpression() throws IOException {
        String code = "impl Persona { fn test() { (!true); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseBinaryAddition() throws IOException {
        String code = "impl Persona { fn test() { (5 + 3); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseBinarySubtraction() throws IOException {
        String code = "impl Persona { fn test() { (5 - 3); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseBinaryMultiplication() throws IOException {
        String code = "impl Persona { fn test() { (5 * 3); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseArrayType() throws IOException {
        String code = "class Persona { Array Int nums; }";
        testParsingSuccess(code);
    }

    @Test
    void testParseNewExpression() throws IOException {
        String code = "impl Persona { fn test() { (new Persona()); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseNewArrayExpression() throws IOException {
        String code = "impl Persona { fn test() { (new Int[10]); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseMethodCallExpression() throws IOException {
        String code = "impl Persona { fn test() { (obj.method()); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseArrayAccessExpression() throws IOException {
        String code = "impl Persona { fn test() { (arr[0]); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseSelfAccess() throws IOException {
        String code = "impl Persona { fn test() { (self); } }";
        testParsingSuccess(code);
    }

    @Test
    void testParseSelfDotAccess() throws IOException {
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
    void testParseMultipleSentences() throws IOException {
        String code = "impl Persona { fn test() { ; ; ; } }";
        testParsingSuccess(code);
    }

    // Helper method
    private void testParsingSuccess(String code) {
        assertDoesNotThrow(() -> parseCode(code));
    }

    private void parseCode(String code) throws IOException {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".s");
        try {
            java.nio.file.Files.write(tempFile, code.getBytes());

            FileReader reader = new FileReader(tempFile.toString());
            Lexical lexical = new Lexical(reader);
            SyntacticAnalyzer analyzer = new SyntacticAnalyzer(lexical);
            analyzer.parseProgram();
        } finally {
            java.nio.file.Files.delete(tempFile);
        }
    }
}






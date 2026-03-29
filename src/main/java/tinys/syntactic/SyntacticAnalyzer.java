package tinys.syntactic;

import tinys.exceptions.SyntacticException;
import tinys.lexical.Lexical;
import tinys.lexical.Token;
import tinys.lexical.TokenType;

public class SyntacticAnalyzer {
    private final Lexical lexical;
    private Token currentToken;
    private Token nextToken;

    public SyntacticAnalyzer(Lexical lexical) {
        this.lexical = lexical;
        this.currentToken = lexical.nextToken();
        this.nextToken = lexical.nextToken();
    }

    public void parseProgram() {
        if (isDefinitionStart(currentToken)) {
            parseDefsList();
            if (isStartKeyword(currentToken)) {
                parseStart();
            }
        } else if (isStartKeyword(currentToken)) {
            parseStart();
        } else {
            error("SE ESPERABA DEFINICION O START");
        }

        match(TokenType.EOF, "EOF");
    }

    private void parseDefsList() {
        do {
            if (currentToken.type() == TokenType.CLASS) {
                parseClassDef();
            } else if (currentToken.type() == TokenType.IMPL) {
                parseImplDef();
            } else {
                error("SE ESPERABA CLASS O IMPL");
            }
        } while (isDefinitionStart(currentToken));
    }

    private void parseStart() {
        if (!isStartKeyword(currentToken)) {
            error("SE ESPERABA START");
        }
        advance();
        parseMethodBlock();
    }

    private void parseClassDef() {
        match(TokenType.CLASS, "class");
        match(TokenType.CLASS_ID, "IDCLASS");

        if (currentToken.type() == TokenType.COLON) {
            advance();
            parseType();
        }

        match(TokenType.BRACES_OPEN, "{");
        while (isAttributeStart(currentToken)) {
            parseAttribute();
        }
        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseImplDef() {
        match(TokenType.IMPL, "impl");
        match(TokenType.CLASS_ID, "IDCLASS");
        match(TokenType.BRACES_OPEN, "{");

        while (isMemberStart(currentToken)) {
            parseMember();
        }

        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseAttribute() {
        if (currentToken.type() == TokenType.PUB) {
            advance();
        }
        parseType();
        parseVarsDeclList();
        match(TokenType.SEMICOLON, ";");
    }

    private void parseMember() {
        if (currentToken.type() == TokenType.DOT) {
            parseConstructor();
            return;
        }
        parseMethod();
    }

    private void parseConstructor() {
        match(TokenType.DOT, ".");
        parseFormalArgs();
        parseMethodBlock();
    }

    private void parseMethod() {
        if (currentToken.type() == TokenType.ST) {
            advance();
            match(TokenType.FN, "fn");
            parseMethodSignatureAndBody();
            return;
        }

        match(TokenType.FN, "fn");
        parseMethodSignatureAndBody();
    }

    private void parseMethodSignatureAndBody() {
        if (currentToken.type() == TokenType.VOID || isTypeStart(currentToken)) {
            parseMethodType();
        }
        match(TokenType.METHOD_ID, "IDMETAT");
        parseFormalArgs();
        parseMethodBlock();
    }

    private void parseMethodType() {
        if (currentToken.type() == TokenType.VOID) {
            advance();
            return;
        }
        parseType();
    }

    private void parseFormalArgs() {
        match(TokenType.PARENTHESIS_OPEN, "(");
        if (isTypeStart(currentToken)) {
            parseFormalArg();
            while (currentToken.type() == TokenType.COMMA) {
                advance();
                parseFormalArg();
            }
        }
        match(TokenType.PARENTHESIS_CLOSE, ")");
    }

    private void parseFormalArg() {
        parseType();
        match(TokenType.METHOD_ID, "IDMETAT");
    }

    private void parseMethodBlock() {
        match(TokenType.BRACES_OPEN, "{");

        while (isLocalVarDeclStart()) {
            parseLocalVarDecl();
        }

        while (isSentenceStart(currentToken)) {
            parseSentence();
        }

        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseLocalVarDecl() {
        parseType();
        parseVarsDeclList();
        match(TokenType.SEMICOLON, ";");
    }

    private void parseVarsDeclList() {
        match(TokenType.METHOD_ID, "IDMETAT");
        while (currentToken.type() == TokenType.COMMA) {
            advance();
            match(TokenType.METHOD_ID, "IDMETAT");
        }
    }

    private void parseType() {
        if (currentToken.type() == TokenType.ARRAY) {
            advance();
            parsePrimitiveType();
            return;
        }

        if (isPrimitiveType(currentToken)) {
            parsePrimitiveType();
            return;
        }

        if (currentToken.type() == TokenType.CLASS_ID) {
            advance();
            return;
        }

        error("SE ESPERABA TYPE");
    }

    private void parsePrimitiveType() {
        if (!isPrimitiveType(currentToken)) {
            error("SE ESPERABA TIPO PRIMITIVO");
        }
        advance();
    }

    private void parseSentence() {
        if (currentToken.type() == TokenType.SEMICOLON) {
            advance();
            return;
        }

        if (currentToken.type() == TokenType.IF) {
            parseIfSentence();
            return;
        }

        if (currentToken.type() == TokenType.WHILE) {
            parseWhileSentence();
            return;
        }

        if (currentToken.type() == TokenType.FOR) {
            parseForSentence();
            return;
        }

        if (currentToken.type() == TokenType.BRACES_OPEN) {
            parseBlock();
            return;
        }

        if (currentToken.type() == TokenType.RET) {
            parseReturnSentence();
            return;
        }

        if (currentToken.type() == TokenType.PARENTHESIS_OPEN) {
            parseSimpleSentence();
            match(TokenType.SEMICOLON, ";");
            return;
        }

        parseAssignment();
        match(TokenType.SEMICOLON, ";");
    }

    private void parseIfSentence() {
        match(TokenType.IF, "if");
        match(TokenType.PARENTHESIS_OPEN, "(");
        parseExp();
        match(TokenType.PARENTHESIS_CLOSE, ")");
        parseSentence();

        if (currentToken.type() == TokenType.ELSE) {
            advance();
            parseSentence();
        }
    }

    private void parseWhileSentence() {
        match(TokenType.WHILE, "while");
        match(TokenType.PARENTHESIS_OPEN, "(");
        parseExp();
        match(TokenType.PARENTHESIS_CLOSE, ")");
        parseSentence();
    }

    private void parseForSentence() {
        match(TokenType.FOR, "for");
        match(TokenType.PARENTHESIS_OPEN, "(");
        parsePrimitiveType();
        match(TokenType.METHOD_ID, "IDMETAT");
        match(TokenType.IN, "in");
        match(TokenType.METHOD_ID, "IDMETAT");
        match(TokenType.PARENTHESIS_CLOSE, ")");
        parseSentence();
    }

    private void parseReturnSentence() {
        match(TokenType.RET, "ret");
        if (isExpStart(currentToken)) {
            parseExp();
        }
        match(TokenType.SEMICOLON, ";");
    }

    private void parseBlock() {
        match(TokenType.BRACES_OPEN, "{");
        while (isSentenceStart(currentToken)) {
            parseSentence();
        }
        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseAssignment() {
        if (currentToken.type() == TokenType.SELF) {
            advance();
            while (currentToken.type() == TokenType.DOT) {
                advance();
                match(TokenType.METHOD_ID, "ID");
            }
        } else {
            parseAssignableIdChain();
        }

        match(TokenType.EQUAL, "=");
        parseExp();
    }

    private void parseAssignableIdChain() {
        match(TokenType.METHOD_ID, "ID");

        while (currentToken.type() == TokenType.DOT || currentToken.type() == TokenType.SQR_BRACKET_OPEN) {
            if (currentToken.type() == TokenType.DOT) {
                advance();
                match(TokenType.METHOD_ID, "ID");
            } else {
                advance();
                parseExp();
                match(TokenType.SQR_BRACKET_CLOSE, "]");
            }
        }
    }

    private void parseSimpleSentence() {
        match(TokenType.PARENTHESIS_OPEN, "(");
        parseExp();
        match(TokenType.PARENTHESIS_CLOSE, ")");
    }

    private void parseExp() {
        parseOrExp();
    }

    private void parseOrExp() {
        parseAndExp();
        while (isLogicalKeyword(currentToken, "or")) {
            advance();
            parseAndExp();
        }
    }

    private void parseAndExp() {
        parseEqExp();
        while (isLogicalKeyword(currentToken, "and")) {
            advance();
            parseEqExp();
        }
    }

    private void parseEqExp() {
        parseCompoundExp();
        while (isEqOperator(currentToken)) {
            advance();
            parseCompoundExp();
        }
    }

    private void parseCompoundExp() {
        parseAddExp();
        while (isCompoundOperator(currentToken)) {
            advance();
            parseAddExp();
        }
    }

    private void parseAddExp() {
        parseMultExp();
        while (currentToken.type() == TokenType.ADD_OP) {
            advance();
            parseMultExp();
        }
    }

    private void parseMultExp() {
        parseUnaryExp();
        while (currentToken.type() == TokenType.MULT_OP) {
            advance();
            parseUnaryExp();
        }
    }

    private void parseUnaryExp() {
        if (currentToken.type() == TokenType.ADD_OP || currentToken.type() == TokenType.UNARY_OP) {
            advance();
            parseUnaryExp();
            return;
        }

        parseOperand();
    }

    private void parseOperand() {
        if (isLiteral(currentToken)) {
            advance();
            return;
        }

        parsePrimary();
    }

    private void parsePrimary() {
        if (currentToken.type() == TokenType.PARENTHESIS_OPEN) {
            parseSimpleSentence();
            parseOptionalChaining();
            return;
        }

        if (currentToken.type() == TokenType.SELF) {
            advance();
            parseOptionalChaining();
            return;
        }

        if (currentToken.type() == TokenType.NEW) {
            parseNewExpr();
            return;
        }

        if (currentToken.type() == TokenType.CLASS_ID) {
            parseStaticCallOrRef();
            return;
        }

        if (currentToken.type() == TokenType.METHOD_ID) {
            parseIdPrimary();
            return;
        }

        error("SE ESPERABA OPERANDO");
    }

    private void parseIdPrimary() {
        match(TokenType.METHOD_ID, "ID");

        if (currentToken.type() == TokenType.PARENTHESIS_OPEN) {
            parseCurrentArguments();
        } else if (currentToken.type() == TokenType.SQR_BRACKET_OPEN) {
            advance();
            parseExp();
            match(TokenType.SQR_BRACKET_CLOSE, "]");
        }

        parseOptionalChaining();
    }

    private void parseStaticCallOrRef() {
        match(TokenType.CLASS_ID, "IDCLASS");
        if (currentToken.type() == TokenType.DOT) {
            advance();
            match(TokenType.METHOD_ID, "ID");
            parseCurrentArguments();
        }
        parseOptionalChaining();
    }

    private void parseNewExpr() {
        match(TokenType.NEW, "new");

        if (isPrimitiveType(currentToken)) {
            parsePrimitiveType();
            match(TokenType.SQR_BRACKET_OPEN, "[");
            parseExp();
            match(TokenType.SQR_BRACKET_CLOSE, "]");
            return;
        }

        match(TokenType.CLASS_ID, "IDCLASS");
        parseCurrentArguments();
        parseOptionalChaining();
    }

    private void parseCurrentArguments() {
        match(TokenType.PARENTHESIS_OPEN, "(");
        if (isExpStart(currentToken)) {
            parseExp();
            while (currentToken.type() == TokenType.COMMA) {
                advance();
                parseExp();
            }
        }
        match(TokenType.PARENTHESIS_CLOSE, ")");
    }

    private void parseOptionalChaining() {
        while (currentToken.type() == TokenType.DOT) {
            advance();
            match(TokenType.METHOD_ID, "ID");

            if (currentToken.type() == TokenType.PARENTHESIS_OPEN) {
                parseCurrentArguments();
            } else if (currentToken.type() == TokenType.SQR_BRACKET_OPEN) {
                advance();
                parseExp();
                match(TokenType.SQR_BRACKET_CLOSE, "]");
            }
        }
    }

    private boolean isDefinitionStart(Token token) {
        return token.type() == TokenType.CLASS || token.type() == TokenType.IMPL;
    }

    private boolean isAttributeStart(Token token) {
        return token.type() == TokenType.PUB || isTypeStart(token);
    }

    private boolean isMemberStart(Token token) {
        return token.type() == TokenType.DOT || token.type() == TokenType.ST || token.type() == TokenType.FN;
    }

    private boolean isStartKeyword(Token token) {
        return token.type() == TokenType.METHOD_ID && "start".equals(token.value());
    }

    private boolean isTypeStart(Token token) {
        return token.type() == TokenType.ARRAY || isPrimitiveType(token) || token.type() == TokenType.CLASS_ID;
    }

    private boolean isPrimitiveType(Token token) {
        if (token.type() != TokenType.CLASS_ID) {
            return false;
        }
        String value = token.value();
        return "Int".equals(value) || "Bool".equals(value) || "Str".equals(value);
    }

    private boolean isSentenceStart(Token token) {
        return token.type() == TokenType.SEMICOLON
                || token.type() == TokenType.IF
                || token.type() == TokenType.WHILE
                || token.type() == TokenType.FOR
                || token.type() == TokenType.BRACES_OPEN
                || token.type() == TokenType.RET
                || token.type() == TokenType.PARENTHESIS_OPEN
                || token.type() == TokenType.SELF
                || token.type() == TokenType.METHOD_ID;
    }

    private boolean isLocalVarDeclStart() {
        if (!isTypeStart(currentToken)) {
            return false;
        }
        return nextToken.type() == TokenType.METHOD_ID;
    }

    private boolean isExpStart(Token token) {
        return token.type() == TokenType.PARENTHESIS_OPEN
                || token.type() == TokenType.SELF
                || token.type() == TokenType.NEW
                || token.type() == TokenType.CLASS_ID
                || token.type() == TokenType.METHOD_ID
                || token.type() == TokenType.INT_LIT
                || token.type() == TokenType.BOOL_LIT
                || token.type() == TokenType.STR_LIT
                || token.type() == TokenType.NIL_LIT
                || token.type() == TokenType.ADD_OP
                || token.type() == TokenType.UNARY_OP;
    }

    private boolean isLiteral(Token token) {
        return token.type() == TokenType.INT_LIT
                || token.type() == TokenType.BOOL_LIT
                || token.type() == TokenType.STR_LIT
                || token.type() == TokenType.NIL_LIT;
    }

    private boolean isLogicalKeyword(Token token, String value) {
        return token.type() == TokenType.METHOD_ID && value.equals(token.value());
    }

    private boolean isEqOperator(Token token) {
        return token.type() == TokenType.COMP_OP && ("==".equals(token.value()) || "!=".equals(token.value()));
    }

    private boolean isCompoundOperator(Token token) {
        return token.type() == TokenType.COMP_OP && ("<".equals(token.value())
                || ">".equals(token.value())
                || "<=".equals(token.value())
                || ">=".equals(token.value()));
    }

    private void match(TokenType expectedType, String expectedName) {
        if (currentToken.type() != expectedType) {
            error("SE ESPERABA " + expectedName + " PERO SE ENCONTRO " + tokenLabel(currentToken));
        }
        advance();
    }

    private void advance() {
        currentToken = nextToken;
        nextToken = lexical.nextToken();
    }

    private String tokenLabel(Token token) {
        if (token.type() == TokenType.EOF) {
            return "EOF";
        }
        return token.value();
    }

    private void error(String message) {
        throw new SyntacticException(currentToken.line(), currentToken.column(), message);
    }
}

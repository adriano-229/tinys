package tinys.syntactic;

import tinys.exceptions.SyntacticException;
import tinys.lexical.Lexer;
import tinys.lexical.Token;
import tinys.lexical.TokenType;
import tinys.semantic.refs.ArrayTypeRef;
import tinys.semantic.refs.ClassTypeRef;
import tinys.semantic.refs.PrimitiveTypeRef;
import tinys.semantic.refs.TypeRef;
import tinys.semantic.st.Start;
import tinys.semantic.st.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Token currentToken;
    private final SymbolTable symbolTable = new SymbolTable();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
    }

    public void parseProgram() {
        if (currentToken.type() == TokenType.CLASS || currentToken.type() == TokenType.IMPL) {
            parseDefList();
            parseStart();
            match(TokenType.EOF, "EOF");
            return;
        }

        if (currentToken.type() == TokenType.METHOD_ID) {
            parseStart();
            match(TokenType.EOF, "EOF");
            return;
        }

        error("SE ESPERABA DEFINICION O START");
    }

    private void parseDefList() {
        parseDef();
        if (currentToken.type() == TokenType.CLASS || currentToken.type() == TokenType.IMPL) {
            parseDefList();
        }
    }

    private void parseDef() {
        if (currentToken.type() == TokenType.CLASS) {
            parseClassDef();
            return;
        }

        if (currentToken.type() == TokenType.IMPL) {
            parseImplDef();
            return;
        }

        error("SE ESPERABA CLASS O IMPL");
    }

    private void parseStart() {
        Token start = match(TokenType.METHOD_ID, "start");
        if (!"start".equals(start.value())) {
            error("SE ESPERABA METODO \"start\" PERO SE ENCONTRO " + start.value());
        }

        symbolTable.setStart(new Start(start.line(), start.col()));
        symbolTable.setCurrentMethod(symbolTable.getStart());
        parseMethodBlock();
    }

    private void parseClassDef() {
        match(TokenType.CLASS, "class");

        Token classId = match(TokenType.CLASS_ID, "IDCLASS");
        symbolTable.declareClass(classId.value(), classId.line(), classId.col());

        TypeRef typeRef = parseInheritanceOpt();
        symbolTable.setCurrentClassParent(typeRef);
        match(TokenType.BRACES_OPEN, "{");

        parseAttributeListOpt();
        match(TokenType.BRACES_CLOSE, "}");
    }

    private TypeRef parseInheritanceOpt() {
        if (currentToken.type() == TokenType.COLON) {
            match(TokenType.COLON, ":");
            return parseType();
        }
        return null;
    }

    private void parseAttributeListOpt() {
        if (currentToken.type() == TokenType.PUB
                || currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseAttributeList();
        }
    }

    private void parseAttributeList() {
        parseAttribute();
        if (currentToken.type() == TokenType.PUB
                || currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseAttributeList();
        }
    }

    private void parseAttribute() {
        boolean visibility = parseVisibilityOpt();
        TypeRef typeRef = parseType();
        List<String> variableNames = parseVarsDeclList();
        match(TokenType.SEMICOLON, ";");

        symbolTable.addAttributesToCurrentClass(visibility, typeRef, variableNames);
    }

    // retorna true si es público
    private boolean parseVisibilityOpt() {
        if (currentToken.type() == TokenType.PUB) {
            match(TokenType.PUB, "pub");
            return true;
        }
        return false;
    }

    private void parseImplDef() {
        match(TokenType.IMPL, "impl");
        match(TokenType.CLASS_ID, "IDCLASS");
        match(TokenType.BRACES_OPEN, "{");
        parseMemberListOpt();
        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseMemberListOpt() {
        if (currentToken.type() == TokenType.DOT
                || currentToken.type() == TokenType.ST
                || currentToken.type() == TokenType.FN) {
            parseMemberList();
        }
    }

    private void parseMemberList() {
        parseMember();
        if (currentToken.type() == TokenType.DOT
                || currentToken.type() == TokenType.ST
                || currentToken.type() == TokenType.FN) {
            parseMemberList();
        }
    }

    private void parseMember() {
        if (currentToken.type() == TokenType.DOT) {
            parseConstructor();
            return;
        }

        if (currentToken.type() == TokenType.ST || currentToken.type() == TokenType.FN) {
            parseMethod();
            return;
        }

        error("SE ESPERABA MIEMBRO");
    }

    private void parseConstructor() {
        match(TokenType.DOT, ".");
        parseFormalArgs();
        parseMethodBlock();
    }

    private void parseMethod() {
        if (currentToken.type() == TokenType.ST) {
            match(TokenType.ST, "st");
            match(TokenType.FN, "fn");
            parseMethodSignatureAndBody();
            return;
        }

        match(TokenType.FN, "fn");
        parseMethodSignatureAndBody();
    }

    private void parseMethodSignatureAndBody() {
        parseMethodTypeOpt();
        match(TokenType.METHOD_ID, "IDMETAT");
        parseFormalArgs();
        parseMethodBlock();
    }

    private void parseMethodTypeOpt() {
        if (currentToken.type() == TokenType.VOID
                || currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseMethodType();
        }
    }

    private void parseMethodType() {
        if (currentToken.type() == TokenType.VOID) {
            match(TokenType.VOID, "void");
            return;
        }
        parseType();
    }

    private void parseMethodBlock() {
        match(TokenType.BRACES_OPEN, "{");
        parseLocalVarsDeclListOpt();
        parseSentenceListOpt();
        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseLocalVarsDeclListOpt() {
        if (currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseLocalVarsDeclList();
        }
    }

    private void parseLocalVarsDeclList() {
        parseLocalVarDecl();
        if (currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseLocalVarsDeclList();
        }
    }

    private record LocalVarsDeclList(TypeRef typeRef, List<String> variableNames) {}
    private LocalVarsDeclList parseLocalVarDecl() {
        TypeRef typeRef = parseType();
        List<String> variableNames = parseVarsDeclList();
        match(TokenType.SEMICOLON, ";");

        return new LocalVarsDeclList(typeRef, variableNames);
    }

    private List<String> parseVarsDeclList() {
        Token variableName = match(TokenType.METHOD_ID, "IDMETAT");
        List<String> restOfVarNames = parseVarsDeclTail();

        List<String> variableNames = new ArrayList<>();
        variableNames.add(variableName.value());
        variableNames.addAll(restOfVarNames);

        return variableNames;
    }

    private List<String> parseVarsDeclTail() {
        if (currentToken.type() == TokenType.COMMA) {
            match(TokenType.COMMA, ",");
            Token variableName = match(TokenType.METHOD_ID, "IDMETAT");
            List<String> restOfVarNames = parseVarsDeclTail();

            List<String> variableNames = new ArrayList<>();
            variableNames.add(variableName.value());
            variableNames.addAll(restOfVarNames);

            return variableNames;
        }
        return List.of();
    }

    private void parseFormalArgs() {
        match(TokenType.PAR_OPEN, "(");
        parseFormalArgsListOpt();
        match(TokenType.PAR_CLOSE, ")");
    }

    private void parseFormalArgsListOpt() {
        if (currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseFormalArgsList();
        }
    }

    private void parseFormalArgsList() {
        parseFormalArg();
        parseFormalArgsTail();
    }

    private void parseFormalArgsTail() {
        if (currentToken.type() == TokenType.COMMA) {
            match(TokenType.COMMA, ",");
            parseFormalArg();
            parseFormalArgsTail();
        }
    }

    private void parseFormalArg() {
        parseType();
        match(TokenType.METHOD_ID, "IDMETAT");
    }

    private TypeRef parseType() {
        if (currentToken.type() == TokenType.ARRAY) {
            return parseArrayType();
        }

        if (currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR) {
            return parsePrimitiveType();
        }

        if (currentToken.type() == TokenType.CLASS_ID) {
            String className = currentToken.value();
            match(TokenType.CLASS_ID, "IDCLASS");
            return new ClassTypeRef(className);
        }

        error("SE ESPERABA TYPE");
        return null;
    }

    private ArrayTypeRef parseArrayType() {
        match(TokenType.ARRAY, "Array");
        PrimitiveTypeRef primitiveTypeRef = parsePrimitiveType();
        return new ArrayTypeRef(primitiveTypeRef);
    }

    private PrimitiveTypeRef parsePrimitiveType() {
        if (currentToken.type() == TokenType.TYPE_INT) {
            match(TokenType.TYPE_INT, "Int");
            return new PrimitiveTypeRef("Int");
        }
        if (currentToken.type() == TokenType.TYPE_BOOL) {
            match(TokenType.TYPE_BOOL, "Bool");
            return new PrimitiveTypeRef("Bool");
        }
        if (currentToken.type() == TokenType.TYPE_STR) {
            match(TokenType.TYPE_STR, "Str");
            return new PrimitiveTypeRef("Str");
        }

        error("SE ESPERABA TIPO PRIMITIVO");
        return null;
    }

    private void parseSentenceListOpt() {
        if (currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.IF
                || currentToken.type() == TokenType.WHILE
                || currentToken.type() == TokenType.FOR
                || currentToken.type() == TokenType.BRACES_OPEN
                || currentToken.type() == TokenType.RET) {
            parseSentenceList();
        }
    }

    private void parseSentenceList() {
        parseSentence();
        if (currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.IF
                || currentToken.type() == TokenType.WHILE
                || currentToken.type() == TokenType.FOR
                || currentToken.type() == TokenType.BRACES_OPEN
                || currentToken.type() == TokenType.RET) {
            parseSentenceList();
        }
    }

    private void parseSentence() {
        if (currentToken.type() == TokenType.SEMICOLON) {
            match(TokenType.SEMICOLON, ";");
            return;
        }

        if (currentToken.type() == TokenType.IF) {
            match(TokenType.IF, "if");
            match(TokenType.PAR_OPEN, "(");
            parseExp();
            match(TokenType.PAR_CLOSE, ")");
            parseSentence();
            parseElseOpt();
            return;
        }

        if (currentToken.type() == TokenType.WHILE) {
            match(TokenType.WHILE, "while");
            match(TokenType.PAR_OPEN, "(");
            parseExp();
            match(TokenType.PAR_CLOSE, ")");
            parseSentence();
            return;
        }

        if (currentToken.type() == TokenType.FOR) {
            match(TokenType.FOR, "for");
            match(TokenType.PAR_OPEN, "(");
            parsePrimitiveType();
            match(TokenType.METHOD_ID, "IDMETAT");
            match(TokenType.IN, "in");
            match(TokenType.METHOD_ID, "IDMETAT");
            match(TokenType.PAR_CLOSE, ")");
            parseSentence();
            return;
        }

        if (currentToken.type() == TokenType.BRACES_OPEN) {
            parseBlock();
            return;
        }

        if (currentToken.type() == TokenType.RET) {
            match(TokenType.RET, "ret");
            parseRetOpt();
            match(TokenType.SEMICOLON, ";");
            return;
        }

        if (currentToken.type() == TokenType.PAR_OPEN) {
            parseSimpleSentence();
            match(TokenType.SEMICOLON, ";");
            return;
        }

        if (currentToken.type() == TokenType.SELF || currentToken.type() == TokenType.METHOD_ID) {
            parseAssignment();
            match(TokenType.SEMICOLON, ";");
            return;
        }

        error("SE ESPERABA SENTENCIA");
    }

    private void parseElseOpt() {
        if (currentToken.type() == TokenType.ELSE) {
            match(TokenType.ELSE, "else");
            parseSentence();
        }
    }

    private void parseRetOpt() {
        if (currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.NEW
                || currentToken.type() == TokenType.CLASS_ID
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.INT_LIT
                || currentToken.type() == TokenType.BOOL_LIT
                || currentToken.type() == TokenType.STR_LIT
                || currentToken.type() == TokenType.NIL_LIT
                || currentToken.type() == TokenType.ADD_OP
                || currentToken.type() == TokenType.UNARY_OP) {
            parseExp();
        }
    }

    private void parseBlock() {
        match(TokenType.BRACES_OPEN, "{");
        parseSentenceListOpt();
        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseAssignment() {
        parseAssignable();
        match(TokenType.EQUAL, "=");
        parseExp();
    }

    private void parseAssignable() {
        if (currentToken.type() == TokenType.SELF) {
            match(TokenType.SELF, "self");
            parseSelfChainOpt();
            return;
        }

        match(TokenType.METHOD_ID, "ID");
        parseAssignableTail();
    }

    private void parseSelfChainOpt() {
        if (currentToken.type() == TokenType.DOT) {
            match(TokenType.DOT, ".");
            match(TokenType.METHOD_ID, "ID");
            parseSelfChainOpt();
        }
    }

    private void parseAssignableTail() {
        if (currentToken.type() == TokenType.DOT) {
            match(TokenType.DOT, ".");
            match(TokenType.METHOD_ID, "ID");
            parseAssignableTail();
            return;
        }

        if (currentToken.type() == TokenType.SQR_BRACKET_OPEN) {
            match(TokenType.SQR_BRACKET_OPEN, "[");
            parseExp();
            match(TokenType.SQR_BRACKET_CLOSE, "]");
            parseAssignableTail();
        }
    }

    private void parseSimpleSentence() {
        match(TokenType.PAR_OPEN, "(");
        parseExp();
        match(TokenType.PAR_CLOSE, ")");
    }

    private void parseExp() {
        parseOrExp();
    }

    private void parseOrExp() {
        parseAndExp();
        parseOrExpTail();
    }

    private void parseOrExpTail() {
        if (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value())) {
            advance();
            parseAndExp();
            parseOrExpTail();
        }
    }

    private void parseAndExp() {
        parseEqExp();
        parseAndExpTail();
    }

    private void parseAndExpTail() {
        if (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value())) {
            advance();
            parseEqExp();
            parseAndExpTail();
        }
    }

    private void parseEqExp() {
        parseCompoundExp();
        parseEqExpTail();
    }

    private void parseEqExpTail() {
        if (currentToken.type() == TokenType.COMP_OP
                && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value()))) {
            advance();
            parseCompoundExp();
            parseEqExpTail();
        }
    }

    private void parseCompoundExp() {
        parseAddExp();
        parseCompoundExpTail();
    }

    private void parseCompoundExpTail() {
        if (currentToken.type() == TokenType.COMP_OP
                && ("<".equals(currentToken.value())
                || ">".equals(currentToken.value())
                || "<=".equals(currentToken.value())
                || ">=".equals(currentToken.value()))) {
            advance();
            parseAddExp();
            parseCompoundExpTail();
        }
    }

    private void parseAddExp() {
        parseMultExp();
        parseAddExpTail();
    }

    private void parseAddExpTail() {
        if (currentToken.type() == TokenType.ADD_OP) {
            advance();
            parseMultExp();
            parseAddExpTail();
        }
    }

    private void parseMultExp() {
        parseUnaryExp();
        parseMultExpTail();
    }

    private void parseMultExpTail() {
        if (currentToken.type() == TokenType.MULT_OP) {
            advance();
            parseUnaryExp();
            parseMultExpTail();
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
        if (currentToken.type() == TokenType.INT_LIT
                || currentToken.type() == TokenType.BOOL_LIT
                || currentToken.type() == TokenType.STR_LIT
                || currentToken.type() == TokenType.NIL_LIT) {
            advance();
            return;
        }

        parsePrimary();
    }

    private void parsePrimary() {
        if (currentToken.type() == TokenType.PAR_OPEN) {
            parseSimpleSentence();
            parseOptionalChaining();
            return;
        }

        if (currentToken.type() == TokenType.SELF) {
            match(TokenType.SELF, "self");
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
        parseIdPrimaryTail();
        parseOptionalChaining();
    }

    private void parseIdPrimaryTail() {
        if (currentToken.type() == TokenType.PAR_OPEN) {
            parseCurrentArguments();
            return;
        }

        if (currentToken.type() == TokenType.SQR_BRACKET_OPEN) {
            match(TokenType.SQR_BRACKET_OPEN, "[");
            parseExp();
            match(TokenType.SQR_BRACKET_CLOSE, "]");
        }
    }

    private void parseStaticCallOrRef() {
        match(TokenType.CLASS_ID, "IDCLASS");
        if (currentToken.type() == TokenType.DOT) {
            match(TokenType.DOT, ".");
            match(TokenType.METHOD_ID, "ID");
            parseCurrentArguments();
        }
        parseOptionalChaining();
    }

    private void parseNewExpr() {
        match(TokenType.NEW, "new");
        parseNewTail();
    }

    private void parseNewTail() {
        if (currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR) {
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
        match(TokenType.PAR_OPEN, "(");
        parseExpsListOpt();
        match(TokenType.PAR_CLOSE, ")");
    }

    private void parseExpsListOpt() {
        if (currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.NEW
                || currentToken.type() == TokenType.CLASS_ID
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.INT_LIT
                || currentToken.type() == TokenType.BOOL_LIT
                || currentToken.type() == TokenType.STR_LIT
                || currentToken.type() == TokenType.NIL_LIT
                || currentToken.type() == TokenType.ADD_OP
                || currentToken.type() == TokenType.UNARY_OP) {
            parseExpsList();
        }
    }

    private void parseExpsList() {
        parseExp();
        parseExpsTail();
    }

    private void parseExpsTail() {
        if (currentToken.type() == TokenType.COMMA) {
            match(TokenType.COMMA, ",");
            parseExp();
            parseExpsTail();
        }
    }

    private void parseOptionalChaining() {
        if (currentToken.type() == TokenType.DOT) {
            match(TokenType.DOT, ".");
            match(TokenType.METHOD_ID, "ID");
            parseChainAccess();
            parseOptionalChaining();
        }
    }

    private void parseChainAccess() {
        if (currentToken.type() == TokenType.PAR_OPEN) {
            parseCurrentArguments();
            return;
        }

        if (currentToken.type() == TokenType.SQR_BRACKET_OPEN) {
            match(TokenType.SQR_BRACKET_OPEN, "[");
            parseExp();
            match(TokenType.SQR_BRACKET_CLOSE, "]");
        }
    }


    private Token match(TokenType expectedType, String expectedName) {
        if (currentToken.type() != expectedType) {
            error("SE ESPERABA " + expectedName + " PERO SE ENCONTRO " + tokenLabel(currentToken));
        }
        Token consumedToken = currentToken;
        advance();
        return consumedToken;
    }

    private void advance() {
        currentToken = lexer.nextToken();
    }

    private String tokenLabel(Token token) {
        if (token.type() == TokenType.EOF) {
            return "EOF";
        }
        return token.value();
    }

    private void error(String message) {
        throw new SyntacticException(currentToken.line(), currentToken.col(), message);
    }
}


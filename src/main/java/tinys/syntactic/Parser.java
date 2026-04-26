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
    private final SymbolTable symbolTable = new SymbolTable();
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
    }

    public void parseProgram() {
        parseDefList();
        parseStart();
        match(TokenType.EOF, "EOF");
    }

    private void parseDefList() {
        if (currentToken.type() == TokenType.CLASS || currentToken.type() == TokenType.IMPL) {
            parseDef();
            parseDefList();
            return;
        }

        if (currentToken.type() == TokenType.METHOD_ID && "start".equals(currentToken.value())) {
            return;
        }

        error("SE ESPERABA DEFINICION O START");
    }

    private void parseDef() {
        if (currentToken.type() == TokenType.CLASS) {
            parseClassDef();
            return;
        }

        if (currentToken.type() == TokenType.IMPL) {
            parseImpl();
            return;
        }

        error("SE ESPERABA CLASS O IMPL");
    }

    private void parseStart() {
        Token startToken = match(TokenType.METHOD_ID, "start");
        if (!"start".equals(startToken.value())) {
            error("SE ESPERABA METODO \"start\" PERO SE ENCONTRO " + startToken.value());
        }

        symbolTable.setStart(new Start(startToken.line(), startToken.col()));
        symbolTable.setCurrentMethod(symbolTable.getStart());

        parseMethodBlock();
    }

    private void parseClassDef() {
        match(TokenType.CLASS, "class");

        Token classId = match(TokenType.CLASS_ID, "IDCLASS");
        symbolTable.declareClass(classId.value(), classId.line(), classId.col());

        TypeRef parentRef = parseInheritanceOpt();
        symbolTable.setCurrentClassParent(parentRef);

        match(TokenType.BRACES_OPEN, "{");
        parseAttributeList();
        match(TokenType.BRACES_CLOSE, "}");
    }

    private TypeRef parseInheritanceOpt() {
        if (currentToken.type() == TokenType.COLON) {
            match(TokenType.COLON, ":");
            return parseType();
        }

        if (currentToken.type() == TokenType.BRACES_OPEN) {
            return null;
        }

        error("SE ESPERABA ':' O '{'");
        return null;
    }

    private void parseAttributeList() {
        if (currentToken.type() == TokenType.PUB
                || currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseAttribute();
            parseAttributeList();
            return;
        }

        if (currentToken.type() == TokenType.BRACES_CLOSE) {
            return;
        }

        error("SE ESPERABA ATRIBUTO O '}'");
    }

    private void parseAttribute() {
        boolean isPublic = parseVisibilityOpt();
        TypeRef typeRef = parseType();

        List<String> variableNames = new ArrayList<>();
        Token firstName = match(TokenType.METHOD_ID, "IDMETAT");
        variableNames.add(firstName.value());
        variableNames.addAll(parseVarsDeclList());

        match(TokenType.SEMICOLON, ";");

        symbolTable.addAttributesToCurrentClass(isPublic, typeRef, variableNames);
    }

    private boolean parseVisibilityOpt() {
        if (currentToken.type() == TokenType.PUB) {
            match(TokenType.PUB, "pub");
            return true;
        }

        if (currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            return false;
        }

        error("SE ESPERABA VISIBILIDAD O TIPO");
        return false;
    }

    private void parseImpl() {
        match(TokenType.IMPL, "impl");
        match(TokenType.CLASS_ID, "IDCLASS");
        match(TokenType.BRACES_OPEN, "{");
        parseMemberList();
        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseMemberList() {
        if (currentToken.type() == TokenType.ST
                || currentToken.type() == TokenType.FN
                || currentToken.type() == TokenType.DOT) {
            parseMember();
            parseMemberList();
            return;
        }

        if (currentToken.type() == TokenType.BRACES_CLOSE) {
            return;
        }

        error("SE ESPERABA MIEMBRO O '}'");
    }

    private void parseMember() {
        if (currentToken.type() == TokenType.ST || currentToken.type() == TokenType.FN) {
            parseMethod();
            return;
        }

        if (currentToken.type() == TokenType.DOT) {
            parseConstructor();
            return;
        }

        error("SE ESPERABA METODO O CONSTRUCTOR");
    }

    private void parseConstructor() {
        match(TokenType.DOT, ".");
        parseFormalArgs();
        parseMethodBlock();
    }

    private void parseMethod() {
        parseMethodFormOpt();
        match(TokenType.FN, "fn");
        parseMethodSignatureAndBody();
    }

    private void parseMethodFormOpt() {
        if (currentToken.type() == TokenType.ST) {
            match(TokenType.ST, "st");
            return;
        }

        if (currentToken.type() == TokenType.FN) {
            return;
        }

        error("SE ESPERABA 'st' O 'fn'");
    }

    private void parseMethodSignatureAndBody() {
        parseMethodTypeOpt();
        match(TokenType.METHOD_ID, "IDMETAT");
        parseFormalArgs();
        parseMethodBlock();
    }

    private void parseMethodTypeOpt() {
        if (currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseType();
            return;
        }

        if (currentToken.type() == TokenType.VOID) {
            match(TokenType.VOID, "void");
            return;
        }

        if (currentToken.type() == TokenType.METHOD_ID) {
            return;
        }

        error("SE ESPERABA TIPO, VOID O IDMETAT");
    }

    private void parseMethodBlock() {
        match(TokenType.BRACES_OPEN, "{");
        parseLocalVarDeclList();
        parseSentenceList();
        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseLocalVarDeclList() {
        if (currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseLocalVarDecl();
            parseLocalVarDeclList();
            return;
        }

        if (currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.IF
                || currentToken.type() == TokenType.WHILE
                || currentToken.type() == TokenType.FOR
                || currentToken.type() == TokenType.BRACES_OPEN
                || currentToken.type() == TokenType.RET
                || currentToken.type() == TokenType.BRACES_CLOSE) {
            return;
        }

        error("SE ESPERABA DECLARACION LOCAL, SENTENCIA O '}'");
    }

    private LocalVarsDecl parseLocalVarDecl() {
        TypeRef typeRef = parseType();

        List<String> variableNames = new ArrayList<>();
        Token firstName = match(TokenType.METHOD_ID, "IDMETAT");
        variableNames.add(firstName.value());
        variableNames.addAll(parseVarsDeclList());

        match(TokenType.SEMICOLON, ";");

        return new LocalVarsDecl(typeRef, variableNames);
    }

    // VarDeclList and VarDeclTail from grammar4
    private List<String> parseVarsDeclList() {
        return parseVarsDeclTail();
    }

    private List<String> parseVarsDeclTail() {
        if (currentToken.type() == TokenType.COMMA) {
            match(TokenType.COMMA, ",");
            Token variableName = match(TokenType.METHOD_ID, "IDMETAT");
            List<String> names = new ArrayList<>();
            names.add(variableName.value());
            names.addAll(parseVarsDeclTail());
            return names;
        }

        if (currentToken.type() == TokenType.SEMICOLON || currentToken.type() == TokenType.PAR_CLOSE || currentToken.type() == TokenType.BRACES_CLOSE) {
            return List.of();
        }

        error("SE ESPERABA ',' O FIN DE LISTA DE VARIABLES");
        return List.of();
    }

    // Formal arguments productions
    private void parseFormalArgs() {
        match(TokenType.PAR_OPEN, "(");
        parseFormalArgsList();
        match(TokenType.PAR_CLOSE, ")");
    }

    private void parseFormalArgsList() {
        if (currentToken.type() == TokenType.ARRAY
                || currentToken.type() == TokenType.TYPE_INT
                || currentToken.type() == TokenType.TYPE_BOOL
                || currentToken.type() == TokenType.TYPE_STR
                || currentToken.type() == TokenType.CLASS_ID) {
            parseFormalArg();
            parseFormalArgsTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE) {
            return;
        }

        error("SE ESPERABA ARGUMENTO FORMAL O ')' ");
    }

    private void parseFormalArgsTail() {
        if (currentToken.type() == TokenType.COMMA) {
            match(TokenType.COMMA, ",");
            parseFormalArg();
            parseFormalArgsTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE) {
            return;
        }

        error("SE ESPERABA ',' O ')' ");
    }

    private void parseFormalArg() {
        parseType();
        match(TokenType.METHOD_ID, "IDMETAT");
    }

    // Type productions
    private TypeRef parseType() {
        if (currentToken.type() == TokenType.ARRAY) {
            return parseArrayType();
        }

        if (currentToken.type() == TokenType.TYPE_INT || currentToken.type() == TokenType.TYPE_BOOL || currentToken.type() == TokenType.TYPE_STR) {
            return parsePrimitiveType();
        }

        if (currentToken.type() == TokenType.CLASS_ID) {
            return parseReferenceType();
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

    private ClassTypeRef parseReferenceType() {
        Token className = match(TokenType.CLASS_ID, "IDCLASS");
        return new ClassTypeRef(className.value());
    }

    // Sentence productions
    private void parseSentenceList() {
        if (currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.IF
                || currentToken.type() == TokenType.WHILE
                || currentToken.type() == TokenType.FOR
                || currentToken.type() == TokenType.BRACES_OPEN
                || currentToken.type() == TokenType.RET) {
            parseSentence();
            parseSentenceList();
            return;
        }

        if (currentToken.type() == TokenType.BRACES_CLOSE) {
            return;
        }

        error("SE ESPERABA SENTENCIA O '}'");
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

        if (currentToken.type() == TokenType.METHOD_ID || currentToken.type() == TokenType.SELF) {
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
            return;
        }

        if (currentToken.type() == TokenType.BRACES_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.IF
                || currentToken.type() == TokenType.WHILE
                || currentToken.type() == TokenType.FOR
                || currentToken.type() == TokenType.BRACES_OPEN
                || currentToken.type() == TokenType.RET) {
            return;
        }

        error("SE ESPERABA 'else' O FIN DE IF");
    }

    private void parseRetOpt() {
        if (currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.CLASS_ID
                || currentToken.type() == TokenType.NEW
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.INT_LIT
                || currentToken.type() == TokenType.BOOL_LIT
                || currentToken.type() == TokenType.STR_LIT
                || currentToken.type() == TokenType.NIL_LIT
                || currentToken.type() == TokenType.UNARY_OP
                || (currentToken.type() == TokenType.ADD_OP
                && ("+".equals(currentToken.value()) || "-".equals(currentToken.value())))) {
            parseExp();
            return;
        }

        if (currentToken.type() == TokenType.SEMICOLON) {
            return;
        }

        error("SE ESPERABA EXPRESION O ';'");
    }

    private void parseBlock() {
        match(TokenType.BRACES_OPEN, "{");
        parseSentenceList();
        match(TokenType.BRACES_CLOSE, "}");
    }

    private void parseAssignment() {
        parseAssignable();
        match(TokenType.EQUAL, "=");
        parseExp();
    }

    private void parseAssignable() {
        if (currentToken.type() == TokenType.METHOD_ID) {
            match(TokenType.METHOD_ID, "IDMETAT");
            parseVarAccessTail();
            return;
        }

        if (currentToken.type() == TokenType.SELF) {
            match(TokenType.SELF, "self");
            parseSelfAccessTail();
            return;
        }

        error("SE ESPERABA ASIGNABLE");
    }

    private void parseSelfAccessTail() {
        if (currentToken.type() == TokenType.DOT) {
            match(TokenType.DOT, ".");
            match(TokenType.METHOD_ID, "IDMETAT");
            parseSelfAccessTail();
            return;
        }

        if (currentToken.type() == TokenType.EQUAL) {
            return;
        }

        error("SE ESPERABA '.' O '='");
    }

    private void parseVarAccessTail() {
        if (currentToken.type() == TokenType.DOT) {
            match(TokenType.DOT, ".");
            match(TokenType.METHOD_ID, "IDMETAT");
            parseVarAccessTail();
            return;
        }

        if (currentToken.type() == TokenType.SQR_BRACKET_OPEN) {
            match(TokenType.SQR_BRACKET_OPEN, "[");
            parseExp();
            match(TokenType.SQR_BRACKET_CLOSE, "]");
            parseVarAccessTail();
            return;
        }

        if (currentToken.type() == TokenType.EQUAL) {
            return;
        }

        error("SE ESPERABA ACCESO DE VARIABLE O '='");
    }

    private void parseSimpleSentence() {
        match(TokenType.PAR_OPEN, "(");
        parseExp();
        match(TokenType.PAR_CLOSE, ")");
    }

    // Expression productions
    private void parseExp() {
        parseOrExp();
    }

    private void parseOrExp() {
        parseAndExp();
        parseOrExpTail();
    }

    private void parseOrExpTail() {
        // El lexer actual representa OR como METHOD_ID con valor "or".
        if (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value())) {
            advance();
            parseAndExp();
            parseOrExpTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE) {
            return;
        }

        error("SE ESPERABA OPERADOR OR O FIN DE EXPRESION");
    }

    private void parseAndExp() {
        parseEqExp();
        parseAndExpTail();
    }

    private void parseAndExpTail() {
        // El lexer actual representa AND como METHOD_ID con valor "and".
        if (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value())) {
            advance();
            parseEqExp();
            parseAndExpTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE
                || (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value()))) {
            return;
        }

        error("SE ESPERABA OPERADOR AND O FIN DE EXPRESION");
    }

    private void parseEqExp() {
        parseCompoundExp();
        parseEqExpTail();
    }

    private void parseEqExpTail() {
        if (currentToken.type() == TokenType.COMP_OP && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value()))) {
            parseEqOp();
            parseCompoundExp();
            parseEqExpTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE
                || (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value()))
                || (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value()))) {
            return;
        }

        error("SE ESPERABA EQOP O FIN DE EXPRESION");
    }

    private void parseCompoundExp() {
        parseAddExp();
        parseCompoundExpTail();
    }

    private void parseCompoundExpTail() {
        if (currentToken.type() == TokenType.COMP_OP && ("<".equals(currentToken.value()) || ">".equals(currentToken.value()) || "<=".equals(currentToken.value()) || ">=".equals(currentToken.value()))) {
            parseCompoundOp();
            parseAddExp();
            parseCompoundExpTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE
                || (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value()))
                || (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value()))
                || (currentToken.type() == TokenType.COMP_OP
                && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value())))) {
            return;
        }

        error("SE ESPERABA COMPOUNDOP O FIN DE EXPRESION");
    }

    private void parseAddExp() {
        parseMultExp();
        parseAddExpTail();
    }

    private void parseAddExpTail() {
        if (currentToken.type() == TokenType.ADD_OP) {
            parseAddOp();
            parseMultExp();
            parseAddExpTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE
                || (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value()))
                || (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value()))
                || (currentToken.type() == TokenType.COMP_OP
                && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.COMP_OP
                && ("<".equals(currentToken.value())
                || ">".equals(currentToken.value())
                || "<=".equals(currentToken.value())
                || ">=".equals(currentToken.value())))) {
            return;
        }

        error("SE ESPERABA ADDOP O FIN DE EXPRESION");
    }

    private void parseMultExp() {
        parseUnaryExp();
        parseMultExpTail();
    }

    private void parseMultExpTail() {
        if (currentToken.type() == TokenType.MULT_OP && ("*".equals(currentToken.value()) || "/".equals(currentToken.value()))) {
            parseMultOp();
            parseUnaryExp();
            parseMultExpTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE
                || (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value()))
                || (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value()))
                || (currentToken.type() == TokenType.COMP_OP
                && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.COMP_OP
                && ("<".equals(currentToken.value())
                || ">".equals(currentToken.value())
                || "<=".equals(currentToken.value())
                || ">=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.ADD_OP
                && ("+".equals(currentToken.value()) || "-".equals(currentToken.value())))) {
            return;
        }

        error("SE ESPERABA MULTOP O FIN DE EXPRESION");
    }

    private void parseUnaryExp() {
        if (currentToken.type() == TokenType.UNARY_OP
                || (currentToken.type() == TokenType.ADD_OP
                && ("+".equals(currentToken.value()) || "-".equals(currentToken.value())))) {
            parseUnaryOp();
            parseUnaryExp();
            return;
        }

        parseOperand();
    }

    private void parseEqOp() {
        if (currentToken.type() == TokenType.COMP_OP && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value()))) {
            advance();
            return;
        }

        error("SE ESPERABA EQOP");
    }

    private void parseCompoundOp() {
        if (currentToken.type() == TokenType.COMP_OP && ("<".equals(currentToken.value()) || ">".equals(currentToken.value()) || "<=".equals(currentToken.value()) || ">=".equals(currentToken.value()))) {
            advance();
            return;
        }

        error("SE ESPERABA COMPOUNDOP");
    }

    private void parseAddOp() {
        if (currentToken.type() == TokenType.ADD_OP && ("+".equals(currentToken.value()) || "-".equals(currentToken.value()))) {
            advance();
            return;
        }

        error("SE ESPERABA ADDOP");
    }

    private void parseMultOp() {
        if (currentToken.type() == TokenType.MULT_OP && ("*".equals(currentToken.value()) || "/".equals(currentToken.value()))) {
            advance();
            return;
        }

        error("SE ESPERABA MULTOP");
    }

    private void parseUnaryOp() {
        if (currentToken.type() == TokenType.UNARY_OP) {
            match(TokenType.UNARY_OP, "UNARY_OP");
            return;
        }

        if (currentToken.type() == TokenType.ADD_OP && ("+".equals(currentToken.value()) || "-".equals(currentToken.value()))) {
            advance();
            return;
        }

        error("SE ESPERABA UNARYOP");
    }

    private void parseOperand() {
        if (currentToken.type() == TokenType.NIL_LIT
                || currentToken.type() == TokenType.BOOL_LIT
                || currentToken.type() == TokenType.INT_LIT
                || currentToken.type() == TokenType.STR_LIT) {
            parseLiteral();
            return;
        }

        parsePrimary();
    }

    private void parseLiteral() {
        if (currentToken.type() == TokenType.NIL_LIT || currentToken.type() == TokenType.BOOL_LIT || currentToken.type() == TokenType.INT_LIT || currentToken.type() == TokenType.STR_LIT) {
            advance();
            return;
        }

        error("SE ESPERABA LITERAL");
    }

    private void parsePrimary() {
        if (currentToken.type() == TokenType.PAR_OPEN) {
            parseBracketedExp();
            return;
        }

        if (currentToken.type() == TokenType.SELF) {
            parseSelfAccess();
            return;
        }

        if (currentToken.type() == TokenType.CLASS_ID) {
            parseStaticMethodCall();
            return;
        }

        if (currentToken.type() == TokenType.NEW) {
            parseClassConstructorCall();
            return;
        }

        if (currentToken.type() == TokenType.METHOD_ID) {
            parseIdPrimary();
            return;
        }

        error("SE ESPERABA PRIMARY");
    }

    private void parseBracketedExp() {
        match(TokenType.PAR_OPEN, "(");
        parseExp();
        match(TokenType.PAR_CLOSE, ")");
        parseChainingOpt();
    }

    private void parseSelfAccess() {
        match(TokenType.SELF, "self");
        parseChainingOpt();
    }

    private void parseIdPrimary() {
        match(TokenType.METHOD_ID, "IDMETAT");
        parseIdPrimaryTail();
        parseChainingOpt();
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
            return;
        }

        if (currentToken.type() == TokenType.DOT
                || currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE
                || (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value()))
                || (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value()))
                || (currentToken.type() == TokenType.COMP_OP
                && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.COMP_OP
                && ("<".equals(currentToken.value())
                || ">".equals(currentToken.value())
                || "<=".equals(currentToken.value())
                || ">=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.ADD_OP
                && ("+".equals(currentToken.value()) || "-".equals(currentToken.value())))) {
            return;
        }

        error("SE ESPERABA LLAMADA, INDEXADO O FIN DE PRIMARY");
    }

    private void parseStaticMethodCall() {
        match(TokenType.CLASS_ID, "IDCLASS");
        match(TokenType.DOT, ".");
        match(TokenType.METHOD_ID, "IDMETAT");
        parseCurrentArguments();
        parseChainingOpt();
    }

    private void parseClassConstructorCall() {
        match(TokenType.NEW, "new");
        parseClassConstructorCallTail();
    }

    private void parseClassConstructorCallTail() {
        if (currentToken.type() == TokenType.CLASS_ID) {
            match(TokenType.CLASS_ID, "IDCLASS");
            parseCurrentArguments();
            parseChainingOpt();
            return;
        }

        if (currentToken.type() == TokenType.TYPE_INT || currentToken.type() == TokenType.TYPE_BOOL || currentToken.type() == TokenType.TYPE_STR) {
            parsePrimitiveType();
            match(TokenType.SQR_BRACKET_OPEN, "[");
            parseExp();
            match(TokenType.SQR_BRACKET_CLOSE, "]");
            return;
        }

        error("SE ESPERABA CONSTRUCTOR DE CLASE O ARRAY PRIMITIVO");
    }

    private void parseCurrentArguments() {
        match(TokenType.PAR_OPEN, "(");
        parseExpsList();
        match(TokenType.PAR_CLOSE, ")");
    }

    private void parseExpsList() {
        if (currentToken.type() == TokenType.PAR_OPEN
                || currentToken.type() == TokenType.SELF
                || currentToken.type() == TokenType.CLASS_ID
                || currentToken.type() == TokenType.NEW
                || currentToken.type() == TokenType.METHOD_ID
                || currentToken.type() == TokenType.INT_LIT
                || currentToken.type() == TokenType.BOOL_LIT
                || currentToken.type() == TokenType.STR_LIT
                || currentToken.type() == TokenType.NIL_LIT
                || currentToken.type() == TokenType.UNARY_OP
                || (currentToken.type() == TokenType.ADD_OP
                && ("+".equals(currentToken.value()) || "-".equals(currentToken.value())))) {
            parseExp();
            parseExpsTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE) {
            return;
        }

        error("SE ESPERABA EXPRESION O ')' ");
    }

    private void parseExpsTail() {
        if (currentToken.type() == TokenType.COMMA) {
            match(TokenType.COMMA, ",");
            parseExp();
            parseExpsTail();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE) {
            return;
        }

        error("SE ESPERABA ',' O ')' ");
    }

    private void parseChainingOpt() {
        if (currentToken.type() == TokenType.DOT) {
            match(TokenType.DOT, ".");
            match(TokenType.METHOD_ID, "IDMETAT");
            parseChainBindOpt();
            parseChainingOpt();
            return;
        }

        if (currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE
                || (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value()))
                || (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value()))
                || (currentToken.type() == TokenType.COMP_OP
                && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.COMP_OP
                && ("<".equals(currentToken.value())
                || ">".equals(currentToken.value())
                || "<=".equals(currentToken.value())
                || ">=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.ADD_OP
                && ("+".equals(currentToken.value()) || "-".equals(currentToken.value())))) {
            return;
        }

        error("SE ESPERABA CHAINING O FIN DE PRIMARY");
    }

    private void parseChainBindOpt() {
        if (currentToken.type() == TokenType.PAR_OPEN) {
            parseCurrentArguments();
            return;
        }

        if (currentToken.type() == TokenType.SQR_BRACKET_OPEN) {
            match(TokenType.SQR_BRACKET_OPEN, "[");
            parseExp();
            match(TokenType.SQR_BRACKET_CLOSE, "]");
            return;
        }

        if (currentToken.type() == TokenType.DOT
                || currentToken.type() == TokenType.PAR_CLOSE
                || currentToken.type() == TokenType.SEMICOLON
                || currentToken.type() == TokenType.COMMA
                || currentToken.type() == TokenType.SQR_BRACKET_CLOSE
                || (currentToken.type() == TokenType.METHOD_ID && "or".equals(currentToken.value()))
                || (currentToken.type() == TokenType.METHOD_ID && "and".equals(currentToken.value()))
                || (currentToken.type() == TokenType.COMP_OP
                && ("==".equals(currentToken.value()) || "!=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.COMP_OP
                && ("<".equals(currentToken.value())
                || ">".equals(currentToken.value())
                || "<=".equals(currentToken.value())
                || ">=".equals(currentToken.value())))
                || (currentToken.type() == TokenType.ADD_OP
                && ("+".equals(currentToken.value()) || "-".equals(currentToken.value())))) {
            return;
        }

        error("SE ESPERABA ARGUMENTOS, INDEXADO O FIN DE CHAIN");
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

    private record LocalVarsDecl(TypeRef typeRef, List<String> variableNames) {
    }
}


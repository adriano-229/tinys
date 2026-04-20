package tinys.lexical;

public enum TokenType {
    // identificadores
    CLASS_ID,
    METHOD_ID,

    // tipos
    TYPE_INT,
    TYPE_BOOL,
    TYPE_STR,
    ARRAY,

    // literales
    INT_LIT,
    BOOL_LIT,
    STR_LIT,
    NIL_LIT,

    // palabras reservadas
    CLASS,
    IMPL,
    NEW,
    IF,
    ELSE,
    ST,
    FN,
    RET,
    WHILE,
    PUB,
    SELF,
    DIV,
    VOID,
    FOR,
    IN,

    // puntuación
    COLON,
    SEMICOLON,
    DOT,
    COMMA,
    PAR_OPEN,
    PAR_CLOSE,
    SQR_BRACKET_OPEN,
    SQR_BRACKET_CLOSE,
    BRACES_OPEN,
    BRACES_CLOSE,
    EQUAL,

    // operadores
    COMP_OP,
    ADD_OP,
    MULT_OP,
    UNARY_OP,

    EOF,
}


package tinys.lexical;

public enum TokenType {
    CLASS_ID,
    METHOD_ID,

    TYPE_INT,
    TYPE_BOOL,
    TYPE_STR,
    ARRAY,

    INT_LIT,
    BOOL_LIT,
    STR_LIT,
    NIL_LIT,

    CLASS,
    IMPL,
    START,
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

    COMP_OP,
    ADD_OP,
    MULT_OP,
    UNARY_OP,

    EOF,
}


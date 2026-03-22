package tinys.lexical;

public enum TokenType {
    CLASS_ID,
    METHOD_ID,

    INT_LIT,
    BOOL_LIT,
    STR_LIT,
    NIL_LIT,

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
    ARRAY,
    FOR,
    IN,

    COLON,
    SEMICOLON,
    DOT,
    COMMA,
    BRACKET_OPEN,
    BRACKET_CLOSE,
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

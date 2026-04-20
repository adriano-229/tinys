### Notas Parser

Notas para comprender el flujo de ejecución del Analizador Sintáctico respecto a la lectura de la gramática implementada (grammar5.txt) para aplicar cambios de forma mecánica si se es requerido.

##### Para una regla de producción _ɑ → β1 β2 ... βn_ existen 3 tipos de terminaciones auxiliares:
1. List: la regla permite recursividad con >=1 elementos → do-while
2. Opt: la regla puede producir λ → if
3. ListOpt: la regla permite recursividad con >=0 elementos → while


###### Si el ɑ tiene _List_ al final entonces la lógica, repetitiva debido a la recursividad, de la regla está contenida dentro de un bucle do-while

_Ejemplo_:

Para la regla:

```
DefList ->
    Def DefList |
    Def
```

El código asociado es:

```java
private void parseDefsList() {
    do {
        parseDef();
    } while (isDefinitionStart(currentToken));
}

private void parseDef() {
    if (currentToken.type() == TokenType.CLASS) {
        parseClassDef();
    } else if (currentToken.type() == TokenType.IMPL) {
        parseImplDef();
    } else {
        error("SE ESPERABA CLASS O IMPL");
    }
}
```
###### Si el ɑ tiene _Opt_ al final entonces la lógica está contenida dentro de un condicional

_Ejemplo_:

Para la regla:

```
Attribute -> VisibilityOpt Type VarsDeclList ;

VisibilityOpt ->
    Visibility |
    λ
```

El código asociado es:

```java
private void parseAttribute() {
    if (currentToken.type() == TokenType.PUB) {
        advance();
    }
    parseType();
    parseVarsDeclList();
    match(TokenType.SEMICOLON, ";");
}

```

###### Si el ɑ tiene _ListOpt_ al final entonces la lógica recursiva o nula está contenida dentro de un while:

_Ejemplo_:

Para la regla:

```
MethodBlock -> { LocalVarsDeclListOpt SentenceListOpt }
```

El código asociado es:

```java
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

```

Notar que se requiere un método verificador que indique si la producción empieza con lambda o no, de ahí el _Start_ de isLocalVarDecl*Start*() 
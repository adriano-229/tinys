# tinyS - an OOL compiler



## Etapa 1 (Analizador lexico)

Proyecto de analisis lexico para tinyS en Java.

### Estructura

Carpeta principal: `src/main/java/tinys`

- `/Phase1.java`: entrypoint principal (`java -jar etapa1.jar ...`).
- `/executors/LexicalExecutor.java`: ejecuta el lexer y formatea la salida.

- `/lexical/Lexer.java`: analizador lexico.
- `/lexical/Token.java` y `/lexical/TokenType.java`: modelo de token.
- `/exceptions/LexicalException.java`: errores lexicos.
- `/lexical/FileReader.java` y `/lexical/FileChar.java`: lectura caracter a caracter y posicion de un archivo.


### Ejecutar

```bash
java -jar target/etapa1.jar <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]
```

### Tests (JUnit)

Se agregaron tests en:

- `src/test/java/tinys/lexical/LexerTest.java`

Casos base en archivos `.s`:

- `src/test/resources/cases/*.s`

Correr tests:

```bash
mvn test
```

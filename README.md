# tinyS

Implementacion en Java del analizador lexico y sintactico de tinyS.

## Estructura minima

Carpeta principal: `src/main/java/tinys`

- `Phase1.java`: runner de etapa 1 (lexico) y modo sintactico opcional.
- `Phase2.java`: runner oficial de etapa 2.
- `executors/LexicalExec.java`: ejecuta y formatea salida lexico.
- `executors/SyntacticExec.java`: ejecuta y formatea salida sintactico.
- `lexical/Lexical.java`: analizador lexico.
- `lexical/Token.java` y `lexical/TokenType.java`: modelo de token.
- `exceptions/LexicalException.java` y `exceptions/SyntacticException.java`: errores.
- `lexical/FileReader.java` y `lexical/FileChar.java`: lectura caracter a caracter.

## Build

```bash
mvn package
```

Genera `target/etapa2.jar`.

## Ejecutar

Etapa 2 (formato catedra):

```bash
java -jar target/etapa2.jar <ARCHIVO_FUENTE>
```

Etapa 1 (lexico, con salida opcional):

```bash
java -cp target/etapa2.jar tinys.Phase1 <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]
```

Modo sintactico desde `Phase1`:

```bash
java -cp target/etapa2.jar tinys.Phase1 --syntactic <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]
```

## Formato de salida

- Exito sintactico: `CORRECTO: ANALISIS SINTACTICO`
- Error lexico:
  - `ERROR: LEXICO`
  - `| NUMERO DE LINEA (NUMERO DE COLUMNA) | DESCRIPCION: |`
- Error sintactico:
  - `ERROR: SINTACTICO`
  - `| NUMERO DE LINEA (NUMERO DE COLUMNA) | DESCRIPCION: |`

## Tests

```bash
mvn test
```

Tests principales:

- `src/test/java/tinys/lexical/LexicalTest.java`
- `src/test/java/tinys/syntactic/SyntacticAnalyzerTest.java`


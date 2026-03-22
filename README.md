# tinyS - Etapa 1 (Analizador Lexico)

Proyecto de analisis lexico para tinyS con dos modulos diferenciados:

- `tinys.lexical`: analizador lexico y estructuras de token.
- `tinys.executors`: ejecutador que invoca el analizador y genera la salida.

## Compilacion

```bash
mvn clean package
```

El artefacto generado queda en `target/etapa1.jar`.

## Ejecucion (formato obligatorio)

```bash
java -jar target/etapa1.jar <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]
```

Ejemplos:

```bash
java -jar target/etapa1.jar testcases/ok_basico.s
java -jar target/etapa1.jar testcases/ok_basico.s salida.txt
```

Si se informa `<ARCHIVO_SALIDA>`, la salida se guarda en ese archivo.

## Formato de salida

### Exito

```text
CORRECTO: ANALISIS LEXICO
| TOKEN | LEXEMA | NÚMERO DE LÍNEA (NÚMERO DE COLUMNA) |
| CLASS | class | LINEA 1 (COLUMNA 1) |
```

### Error lexico

```text
ERROR: LEXICO
| NUMERO DE LINEA (NUMERO DE COLUMNA) | DESCRIPCION: |
| LINEA 1 (COLUMNA 1) | IDENTIFICADOR NO VALIDO Hol@ |
```

La politica frente a error es abortar el analisis lexico.

## Casos de prueba incluidos (`testcases/*.s`)

- `ok_basico.s`: caso exitoso (keywords, identificadores, enteros y comentarios).
- `error_cadena_sin_cerrar.s`: caso con cadena no cerrada.
- `error_comentario_multilinea.s`: caso con comentario multilinea no cerrado.

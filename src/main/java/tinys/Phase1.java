package tinys;

import tinys.exceptions.LexicalException;
import tinys.exceptions.SyntacticException;
import tinys.executors.LexicalExec;
import tinys.executors.SyntacticExec;
import tinys.lexical.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Phase1 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            System.err.println("USO LEXICO: java -jar etapa1.jar <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]");
            System.err.println("USO SINTACTICO: java -jar etapa1.jar --syntactic <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]");
            return;
        }

        boolean syntacticMode = "--syntactic".equals(args[0]);

        String sourcePath;
        String outputPath;
        if (syntacticMode) {
            if (args.length < 2) {
                System.err.println("USO SINTACTICO: java -jar etapa1.jar --syntactic <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]");
                return;
            }
            sourcePath = args[1];
            outputPath = args.length == 3 ? args[2] : null;
        } else {
            if (args.length > 2) {
                System.err.println("USO LEXICO: java -jar etapa1.jar <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]");
                return;
            }
            sourcePath = args[0];
            outputPath = args.length == 2 ? args[1] : null;
        }

        try {
            if (syntacticMode) {
                runSyntactic(sourcePath, outputPath);
            } else {
                runLexical(sourcePath, outputPath);
            }
        } catch (IOException e) {
            System.err.println("ERROR DE I/O: " + e.getMessage());
        }
    }

    private static void runLexical(String sourcePath, String outputPath) throws IOException {
        LexicalExec executor = new LexicalExec();
        try {
            List<Token> tokens = executor.analyze(sourcePath);
            writeLines(executor.formatSuccessOutput(tokens), outputPath);
        } catch (LexicalException e) {
            writeLines(executor.formatErrorOutput(e.getLine(), e.getColumn(), e.getDescription()), outputPath);
        }
    }

    private static void runSyntactic(String sourcePath, String outputPath) throws IOException {
        SyntacticExec executor = new SyntacticExec();
        try {
            executor.analyze(sourcePath);
            writeLines(executor.formatSuccessOutput(), outputPath);
        } catch (LexicalException e) {
            writeLines(formatLexicalError(e), outputPath);
        } catch (SyntacticException e) {
            writeLines(executor.formatErrorOutput(e.getLine(), e.getColumn(), e.getDescription()), outputPath);
        }
    }

    private static List<String> formatLexicalError(LexicalException e) {
        return List.of(
                "ERROR: LEXICO",
                "| NUMERO DE LINEA (NUMERO DE COLUMNA) | DESCRIPCION: |",
                "| LINEA " + e.getLine() + " (COLUMNA " + e.getColumn() + ") | " + e.getDescription() + " |"
        );
    }

    private static void writeLines(List<String> lines, String outputPath) throws IOException {
        if (outputPath == null) {
            for (String line : lines) {
                System.out.println(line);
            }
            return;
        }

        Path path = Path.of(outputPath);
        Files.write(path, lines, StandardCharsets.UTF_8);
    }
}
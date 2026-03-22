package tinys;

import tinys.exceptions.LexicalException;
import tinys.executors.LexicalExecutor;
import tinys.lexical.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Phase1 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("USO: java -jar etapa1.jar <ARCHIVO_FUENTE> [<ARCHIVO_SALIDA>]");
            return;
        }

        String sourcePath = args[0];
        String outputPath = args.length == 2 ? args[1] : null;
        LexicalExecutor executor = new LexicalExecutor();

        try {
            List<Token> tokens = executor.analyze(sourcePath);
            writeLines(executor.formatSuccessOutput(tokens), outputPath);
        } catch (LexicalException e) {
            try {
                writeLines(executor.formatErrorOutput(e.getLine(), e.getColumn(), e.getDescription()), outputPath);
            } catch (IOException ioException) {
                System.err.println("ERROR DE I/O: " + ioException.getMessage());
            }
        } catch (IOException e) {
            System.err.println("ERROR DE I/O: " + e.getMessage());
        }
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
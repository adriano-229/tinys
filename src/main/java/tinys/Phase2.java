package tinys;

import tinys.exceptions.LexicalException;
import tinys.exceptions.SyntacticException;
import tinys.executors.SyntacticExec;

import java.io.IOException;

public class Phase2 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("USO: java -jar etapa2.jar <ARCHIVO_FUENTE>");
            return;
        }

        String sourcePath = args[0];
        SyntacticExec executor = new SyntacticExec();

        try {
            executor.analyze(sourcePath);
            for (String line : executor.formatSuccessOutput()) {
                System.out.println(line);
            }
        } catch (LexicalException e) {
            for (String line : formatLexicalError(e)) {
                System.out.println(line);
            }
        } catch (SyntacticException e) {
            for (String line : executor.formatErrorOutput(e.getLine(), e.getColumn(), e.getDescription())) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("ERROR DE I/O: " + e.getMessage());
        }
    }

    private static java.util.List<String> formatLexicalError(LexicalException e) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("ERROR: LEXICO");
        lines.add("| NUMERO DE LINEA (NUMERO DE COLUMNA) | DESCRIPCION: |");
        lines.add("| LINEA " + e.getLine() + " (COLUMNA " + e.getColumn() + ") | " + e.getDescription() + " |");
        return lines;
    }
}


package tinys.executors;

import tinys.exceptions.SyntacticException;
import tinys.lexical.FileReader;
import tinys.lexical.Lexical;
import tinys.syntactic.SyntacticAnalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Executor for the syntactic analyzer.
 * Runs the parser on the input source file and returns formatted output.
 */
public class SyntacticExec {

    public void analyze(String sourcePath) throws SyntacticException, IOException {
        FileReader reader = new FileReader(sourcePath);
        Lexical lexical = new Lexical(reader);
        SyntacticAnalyzer analyzer = new SyntacticAnalyzer(lexical);

        analyzer.parseProgram();
    }

    public List<String> formatSuccessOutput() {
        List<String> lines = new ArrayList<>();
        lines.add("CORRECTO: ANALISIS SINTACTICO");
        return lines;
    }

    public List<String> formatErrorOutput(int line, int column, String description) {
        List<String> lines = new ArrayList<>();
        lines.add("ERROR: SINTACTICO");
        lines.add("| NUMERO DE LINEA (NUMERO DE COLUMNA) | DESCRIPCION: |");
        lines.add("| LINEA " + line + " (COLUMNA " + column + ") | " + description + " |");
        return lines;
    }
}



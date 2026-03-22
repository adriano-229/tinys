package tinys.executors;

import tinys.lexical.FileReader;
import tinys.lexical.LexicalAnalyzer;
import tinys.lexical.Token;
import tinys.lexical.TokenType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LexicalExecutor {

    public List<Token> analyze(String sourcePath) throws IOException {
        LexicalAnalyzer lexer = new LexicalAnalyzer(new FileReader(sourcePath));
        List<Token> tokens = new ArrayList<>();

        while (true) {
            Token token = lexer.nextToken();
            tokens.add(token);
            if (token.type() == TokenType.EOF) {
                break;
            }
        }

        return tokens;
    }

    public List<String> formatSuccessOutput(List<Token> tokens) {
        List<String> lines = new ArrayList<>();
        lines.add("CORRECTO: ANALISIS LEXICO");
        lines.add("| TOKEN | LEXEMA | NÚMERO DE LÍNEA (NÚMERO DE COLUMNA) |");

        for (Token token : tokens) {
            if (token.type() == TokenType.EOF) {
                continue;
            }
            lines.add("| " + token.type() + " | " + token.value() + " | LINEA "
                    + token.line() + " (COLUMNA " + token.column() + ") |");
        }

        return lines;
    }

    public List<String> formatErrorOutput(int line, int column, String description) {
        List<String> lines = new ArrayList<>();
        lines.add("ERROR: LEXICO");
        lines.add("| NUMERO DE LINEA (NUMERO DE COLUMNA) | DESCRIPCION: |");
        lines.add("| LINEA " + line + " (COLUMNA " + column + ") | " + description + " |");
        return lines;
    }
}


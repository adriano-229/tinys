package tinys.lexical;

import java.io.IOException;

public class LexicalMain {
    public static void main(String[] args) throws IOException {
        String inputPath = args.length > 0
                ? args[0]
                : "/home/adriano/repos/tinys/src/main/java/tinys/lexical/prueba.txt";

        LexicalAnalyzer lexer = new LexicalAnalyzer(new FileReader(inputPath));
        while (true) {
            Token token = lexer.nextToken();
            System.out.printf("%s\t%s\t(%d,%d)%n", token.type(), token.value(), token.line(), token.column());
            if (token.type() == TokenType.EOF) {
                break;
            }
        }
    }
}

package tinys.lexical;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LexicalMain {
    public static void main(String[] args) throws IOException {
        FileReader fileReader = new FileReader("/home/gonza/prueba.txt");

        while (true) {
            FileChar ch = fileReader.nextChar();
            System.out.println(Integer.toHexString(ch.getValue()) + "\tfila:" + ch.getRow() + "\t(" + ch.getCol() + ')');
        }

//        String sourceCode = Files.readString(Path.of("input.s"));
//        LexicalAnalyzer lexer = new LexicalAnalyzer(sourceCode);
//
//        while (true) {
//            Token token = lexer.nextToken();
//            System.out.println(token.getType());
//            if (token.getType() == TokenType.EOF) {
//                break;
//            }
//        }
    }
}

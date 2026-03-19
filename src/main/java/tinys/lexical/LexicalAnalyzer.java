package tinys.lexical;

import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;

@AllArgsConstructor
public class LexicalAnalyzer implements ILexicalAnalyzer {

    private FileReader reader;
    private char currentChar;

    @Override
    public Token nextToken() throws IOException {

        FileChar ch = reader.nextChar();

        return new Token(TokenType.EOF, "", 0, 0);
    }
}

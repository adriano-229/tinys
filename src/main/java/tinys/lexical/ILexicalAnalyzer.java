package tinys.lexical;

import java.io.IOException;

public interface ILexicalAnalyzer {
    Token nextToken() throws IOException;
}

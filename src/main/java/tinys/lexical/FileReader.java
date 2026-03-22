package tinys.lexical;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReader {
    private final String file;
    private int index = 0;
    private int line = 1;
    private int col = 1;

    public FileReader(String filePath) throws IOException {
        this.file = Files.readString(Path.of(filePath));
    }

    public FileChar nextChar() {
        if (index >= file.length()) {
            return null;
        }

        FileChar ch = new FileChar(
                file.charAt(index),
                line,
                col
        );

        index++;
        col++;

        if (ch.value() == '\n') {
            line++;
            col = 1;
        }

        return ch;
    }
}

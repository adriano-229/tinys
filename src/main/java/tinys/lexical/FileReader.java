package tinys.lexical;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReader {
    private String file;
    private int index = 0;
    private int row = 0;
    private int col = 0;

    public FileReader(String filePath) throws IOException {
        this.file = Files.readString(Path.of(filePath));
    }

    public FileChar nextChar() {
        FileChar ch = new FileChar(
                file.charAt(index),
                col,
                row
        );

        index++;
        col++;

        if (ch.getValue() == '\n') {
            row++;
            col = 0;
        }

        return ch;
    }
}

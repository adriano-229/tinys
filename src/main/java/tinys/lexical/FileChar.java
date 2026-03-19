package tinys.lexical;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileChar {
    private char value;
    private int col;
    private int row;
}

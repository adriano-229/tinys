package tinys.semantic.st;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MethodParam {
    private final String name;
    private final String type;
    private final int pos;
}

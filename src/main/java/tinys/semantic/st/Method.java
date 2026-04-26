package tinys.semantic.st;

import lombok.Getter;
import lombok.Setter;
import tinys.semantic.types.Type;

import java.util.HashMap;

@Getter
@Setter
public class Method {
    private final String name;
    private final HashMap<String, MethodParam> parameters = new HashMap<>();
    private final HashMap<String, MethodVariable> variables = new HashMap<>();
    private final Type returnType;
    private final boolean isStatic;

    public Method(String name, int line, int col, Type returnType, boolean isStatic) {
        this.name = name;
        this.returnType = returnType;
        this.isStatic = isStatic;
    }
}

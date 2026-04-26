package tinys.semantic.types;

import lombok.Getter;
import lombok.Setter;
import tinys.semantic.refs.TypeRef;
import tinys.semantic.st.Attribute;
import tinys.semantic.st.Constructor;
import tinys.semantic.st.Method;

import java.util.HashMap;

@Getter
@Setter
public class ClassType extends Type {
    private final String name;
    private final HashMap<String, Attribute> attributes = new HashMap<>();
    private final HashMap<String, Method> methods = new HashMap<>();
    private Integer line;
    private Integer col;
    private ClassType parentClass;
    private TypeRef parentClassRef;
    private Constructor constructor;

    public ClassType(String name, int line, int col) {
        this.name = name;
        this.line = line;
        this.col = col;
    }

    public ClassType(String name) {
        this.name = name;
    }
}

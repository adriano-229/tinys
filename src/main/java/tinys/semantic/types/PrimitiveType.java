package tinys.semantic.types;

import lombok.Getter;

@Getter
public class PrimitiveType extends ClassType {
    public PrimitiveType(String name) {
        super(name);
    }
}

package tinys.semantic.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArrayType extends Type {
    private final PrimitiveType primitiveType;
}

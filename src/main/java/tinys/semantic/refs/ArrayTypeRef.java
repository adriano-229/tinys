package tinys.semantic.refs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ArrayTypeRef extends TypeRef {
    private final PrimitiveTypeRef primitiveTypeRef;
}

package tinys.semantic.refs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tinys.semantic.types.Type;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PrimitiveTypeRef extends TypeRef {
    private final String name;
}

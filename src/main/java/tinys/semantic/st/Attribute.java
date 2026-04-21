package tinys.semantic.st;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tinys.semantic.refs.TypeRef;
import tinys.semantic.types.Type;

@Getter
@RequiredArgsConstructor
public class Attribute {
    private final String name;
    private final boolean isPublic;
    private final TypeRef typeRef;
    private Type type;
}

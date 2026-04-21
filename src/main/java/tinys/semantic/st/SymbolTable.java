package tinys.semantic.st;

import lombok.*;
import tinys.exceptions.SemanticException;
import tinys.semantic.refs.ClassTypeRef;
import tinys.semantic.refs.TypeRef;
import tinys.semantic.types.ArrayType;
import tinys.semantic.types.ClassType;
import tinys.semantic.types.PrimitiveType;
import tinys.semantic.types.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Getter @Setter
public class SymbolTable {
    private final HashMap<String, ClassType> classes = new HashMap<>();         // Contiene las clases declaradas
    private final HashMap<Type, ArrayType> arrayTypes = new HashMap<>();    // Contiene los distintos tipos de array
    private ClassType currentClass;
    private Method currentMethod;
    private Method start;

    public SymbolTable() {
        initializeBuiltIns();
    }

    private void initializeBuiltIns() {
        classes.put("Object", new ClassType("Object"));
        classes.put("IO", new ClassType("IO"));
        classes.put("Array", new ClassType("Array"));

        PrimitiveType intType = new PrimitiveType("Int");
        PrimitiveType boolType = new PrimitiveType("Bool");
        PrimitiveType strType = new PrimitiveType("Str");

        classes.put("Int", intType);
        classes.put("Bool", boolType);
        classes.put("Str", strType);

        arrayTypes.put(intType, new ArrayType(intType));
        arrayTypes.put(boolType, new ArrayType(boolType));
        arrayTypes.put(strType, new ArrayType(strType));

        // todo faltan métodos de algunas clases
    }

    public void declareClass(String name, int line, int col) {
        if (classes.containsKey(name)) {
            currentClass = classes.get(name);
        } else {
            ClassType newClass = new ClassType(name, line, col);
            classes.put(name, newClass);
            currentClass = newClass;
        }
    }

    public void implementClass(String name, int line, int col) {
        if (classes.containsKey(name)) {
            currentClass = classes.get(name);
        } else {
            ClassType newClass = classes.put(name, new ClassType(name, line, col));
            classes.put(name, newClass);
            currentClass = newClass;
        }
    }

    public void setCurrentClassParent(TypeRef parentRef) {
        // todo chequear que no herede de clases de las que no se puede heredar

        // si ya existe una herencia, debe ser igual
        if (currentClass.getParentClassRef() != null && !Objects.equals(currentClass.getParentClassRef(), parentRef))
            throw new SemanticException(currentClass.getLine(), currentClass.getCol(), "CLASE TIENE DOS HERENCIAS DISTINTAS");  // todo mejorar mensaje

        // si no, actualizamos
        if (currentClass.getParentClass() == null) {
            if (parentRef == null)
                currentClass.setParentClassRef(new ClassTypeRef("Object"));
            else
                currentClass.setParentClassRef(parentRef);
        }
    }

    public void addAttributesToCurrentClass(boolean isPublic, TypeRef typeRef, List<String> attrbNames) {
        for (String attrbName : attrbNames) {
            if (currentClass.getAttributes().containsKey(attrbName))
                throw new SemanticException(0, 0, "ATRIBUTO YA DEFINIDO");

            Attribute attribute = new Attribute(attrbName, isPublic, typeRef);
            currentClass.getAttributes().put(attrbName, attribute);
        }
    }
}

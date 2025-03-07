package processors;

import annotations.HierarchyRoot;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeName;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("annotations.HierarchyRoot")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class HierarchyRootProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing started");
        for (Element element : roundEnv.getElementsAnnotatedWith(HierarchyRoot.class)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found element: " + element);
            if (element.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@HierarchyRoot can only be applied to interfaces", element);
                return true;
            }
            TypeElement interfaceElement = (TypeElement) element;
            generateRootClass(interfaceElement);
        }
        return true;
    }

    private void generateRootClass(TypeElement interfaceElement) {
        String rootClassName = interfaceElement.getSimpleName() + "Root";
        TypeName interfaceType = TypeName.get(interfaceElement.asType());

        // Генерация абстрактного класса
        TypeSpec rootClass = TypeSpec.classBuilder(rootClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addSuperinterface(interfaceType)
                .addField(interfaceType, "next", Modifier.PROTECTED) // Поле для цепочки
                .build();

        // Создание Java-файла
        JavaFile javaFile = JavaFile.builder("models", rootClass)
                .build();

        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                    "models." + rootClassName, interfaceElement);
            try (Writer writer = sourceFile.openWriter()) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Failed to generate root class: " + e.getMessage());
        }
    }
}

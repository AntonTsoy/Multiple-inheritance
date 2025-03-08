package ru.nsu.mh;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("ru.nsu.mh.InheritanceRoot")
@SupportedSourceVersion(SourceVersion.RELEASE_23)
public class HierarchyRootProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(InheritanceRoot.class)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE, "Found element @HierarchyRoot " + element
            );
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
        String packageName = processingEnv.getElementUtils().getPackageOf(interfaceElement).getQualifiedName().toString();
        String rootClassName = interfaceElement.getSimpleName() + "Root";
        String fullClassName = packageName.isEmpty() ? rootClassName : packageName + "." + rootClassName;

        TypeName interfaceType = TypeName.get(interfaceElement.asType());

        // Генерация абстрактного класса
        var rootClassBuilder = TypeSpec.classBuilder(rootClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addSuperinterface(interfaceType)
            .addField(interfaceType, "next", Modifier.PROTECTED);

        for (Element methodElement : interfaceElement.getEnclosedElements()) {
            if (methodElement.getKind() == ElementKind.METHOD) {
                ExecutableElement executableMethodElement = (ExecutableElement) methodElement;

                rootClassBuilder.addMethod(getMethod(executableMethodElement));

                rootClassBuilder.addMethod(getNextMethod(executableMethodElement));
            }
        }

        JavaFile javaFile = JavaFile.builder(packageName, rootClassBuilder.build())
                .build();

        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                    fullClassName, interfaceElement);
            try (Writer writer = sourceFile.openWriter()) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Failed to generate root class: " + e.getMessage());
        }
    }

    private MethodSpec getMethod(ExecutableElement executableMethodElement) {
        String methodName = executableMethodElement.getSimpleName().toString();
        TypeName returnType = TypeName.get(executableMethodElement.getReturnType());
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(returnType)
            .build();
    }

    private MethodSpec getNextMethod(ExecutableElement executableMethodElement) {
        String methodName = executableMethodElement.getSimpleName().toString();
        String nextMethodName = "next" + capitalize(methodName);
        TypeName returnType = TypeName.get(executableMethodElement.getReturnType());
        return MethodSpec.methodBuilder(nextMethodName)
            .addModifiers(Modifier.PROTECTED)
            .returns(returnType)
            // TODO: Заменить на реальную логику констрирования одного из предков и его вызова
            .addCode(
                    (returnType.equals(TypeName.VOID) ? "" : "return ") +
                            "next." + methodName + "();")
            .build();
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

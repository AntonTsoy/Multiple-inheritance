package ru.nsu.multher;

import com.squareup.javapoet.*;

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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("ru.nsu.multher.InheritanceRoot")
@SupportedSourceVersion(SourceVersion.RELEASE_23)
public class HierarchyRootProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(InheritanceRoot.class)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE, "Found element @InheritanceRoot " + element
            );
            if (element.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@InheritanceRoot can only be applied to interfaces", element);
                return true;
            }
            TypeElement interfaceElement = (TypeElement) element;
            generateRootClass(interfaceElement);
        }
        return true;
    }

    private void generateRootClass(TypeElement interfaceElement) {
        String packageName = processingEnv.getElementUtils().getPackageOf(interfaceElement)
                                          .getQualifiedName().toString();
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

        JavaFile javaFile = JavaFile.builder(packageName, rootClassBuilder.build()).build();

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

        var methodBuilder = MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        methodBuilder.addParameters(getParams(executableMethodElement));

        return methodBuilder.returns(returnType).build();
    }

    private MethodSpec getNextMethod(ExecutableElement executableMethodElement) {
        String methodName = executableMethodElement.getSimpleName().toString();
        String nextMethodName = "next" + capitalize(methodName);
        TypeName returnType = TypeName.get(executableMethodElement.getReturnType());

        var methodBuilder = MethodSpec.methodBuilder(nextMethodName).addModifiers(Modifier.PROTECTED);

        var params = getParams(executableMethodElement);
        methodBuilder.addParameters(params);

        String paramsStr = params.stream().map(param -> param.name)
                                .collect(Collectors.joining(", "));
        // TODO: Заменить на реальную логику констрирования одного из предков и его вызова
        methodBuilder.addCode((returnType.equals(TypeName.VOID) ? "" : "return ")
                               + "next." + methodName + "(" + paramsStr + ");");

        return methodBuilder.returns(returnType).build();
    }

    private List<ParameterSpec> getParams(ExecutableElement executableMethodElement) {
        return executableMethodElement
                .getParameters()
                .stream()
                .map(ParameterSpec::get)
                .toList();
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

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
import java.util.ArrayList;

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
                        "@InheritanceRoot can only be applied to interfaces", element
                );
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

        var rootClassBuilder = TypeSpec.classBuilder(rootClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addSuperinterface(interfaceType)
                .addFields(getFields(interfaceType))
                .addMethod(getConstructor(interfaceElement))
                .addMethod(getAncestorsDiagnosticGetter());

        for (Element methodElement : interfaceElement.getEnclosedElements()) {
            if (methodElement.getKind() == ElementKind.METHOD) {
                ExecutableElement executableMethodElement = (ExecutableElement) methodElement;
                rootClassBuilder.addMethod(getMethod(executableMethodElement));
                rootClassBuilder.addMethod(getNextMethod(executableMethodElement, rootClassName));
            }
        }

        JavaFile javaFile = JavaFile.builder(packageName, rootClassBuilder.build()).build();

        try {
            JavaFileObject sourceFile = processingEnv.getFiler()
                    .createSourceFile(fullClassName, interfaceElement);
            try (Writer writer = sourceFile.openWriter()) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Failed to generate root class: " + e.getMessage()
            );
        }
    }

    private ArrayList<FieldSpec> getFields(TypeName interfaceType) {
        ArrayList<FieldSpec> fieldSpecs = new ArrayList<>();
        fieldSpecs.add(FieldSpec
                .builder(
                        ParameterizedTypeName.get(ClassName.get(ArrayList.class), interfaceType),
                        "ancestors"
                )
                .addModifiers(Modifier.PRIVATE)
                .build()
        );
        fieldSpecs.add(FieldSpec
                .builder(
                        ParameterizedTypeName.get(ClassName.get(ArrayList.class), interfaceType),
                        "possibleNextInsts"
                )
                .addModifiers(Modifier.PRIVATE)
                .build()
        );
        return fieldSpecs;
    }

    private MethodSpec getConstructor(TypeElement interfaceElement) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addStatement("this.ancestors = new ArrayList<>()")
                .addStatement("this.possibleNextInsts = new ArrayList<>()")
                .addStatement("var classes = $L.getTopstoredAncestorsClasses(this.getClass())", ClassName.get(AncestorsTopSorter.class))
                .addCode(
                        "for (Class<?> ancestorClass : classes) {\n" +
                                "  try {\n" +
                                "    this.ancestors.add((" + interfaceElement.getSimpleName() + ") ancestorClass.getDeclaredConstructor().newInstance());\n" +
                                "  } catch (Exception e) {\n" +
                                "    throw new RuntimeException(\"Failed to instantiate ancestor \" + ancestorClass.getName(), e);\n" +
                                "  }\n" +
                                "}\n"
                )
                .build();
    }

    private MethodSpec getAncestorsDiagnosticGetter() {
        var argClassType = ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("?"));
        var retClassType = ParameterizedTypeName.get(ClassName.get(ArrayList.class), argClassType);
        return MethodSpec.methodBuilder("getAncestorsClasses")
                .addModifiers(Modifier.PUBLIC)
                .returns(retClassType)
                .addStatement("var ancestorsClasses = new ArrayList<Class<?>>()")
                .addCode(
                        "for (var ancestor : ancestors) {\n" +
                                "  ancestorsClasses.add(ancestor.getClass());\n" +
                                "}\n"
                )
                .addStatement("return ancestorsClasses")
                .build();
    }

    private MethodSpec getMethod(ExecutableElement executableMethodElement) {
        String methodName = executableMethodElement.getSimpleName().toString();
        TypeName returnType = TypeName.get(executableMethodElement.getReturnType());

        var methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        methodBuilder.addParameters(getParams(executableMethodElement));

        return methodBuilder.returns(returnType).build();
    }

    private MethodSpec getNextMethod(ExecutableElement executableMethodElement, String rootClassName) {
        String methodName = executableMethodElement.getSimpleName().toString();
        String nextMethodName = "next" + capitalize(methodName);
        TypeName returnType = TypeName.get(executableMethodElement.getReturnType());
        String paramsStr = getParams(executableMethodElement)
                .stream().map(param -> param.name)
                .collect(Collectors.joining(", "));

        var methodBuilder = MethodSpec.methodBuilder(nextMethodName)
                .addModifiers(Modifier.PROTECTED)
                .addParameters(getParams(executableMethodElement));

        methodBuilder.addStatement(
                "if (ancestors.isEmpty()) throw new $L($L)", TypeName.get(IllegalAccessError.class),
                "\"Only types with ancestors specified by @ExtendsMultiple allowed to call next methods\""
        );

        methodBuilder.addStatement("possibleNextInsts.addAll(ancestors)");
        methodBuilder.addStatement("$L nextInstance = ($L) possibleNextInsts.remove(0)", rootClassName, rootClassName);
        methodBuilder.addStatement("nextInstance.possibleNextInsts.addAll(possibleNextInsts)");
        methodBuilder.addStatement("possibleNextInsts.clear()");

        if (!returnType.equals(TypeName.VOID)) {
            methodBuilder.addStatement("var result = nextInstance.$L($L)", methodName, paramsStr);
        } else {
            methodBuilder.addStatement("nextInstance.$L($L)", methodName, paramsStr);
        }

        // Очищаем потенциальных последователей у предка (на случай, если next у предка не был вызван)
        methodBuilder.addStatement("nextInstance.possibleNextInsts.clear()");

        if (!returnType.equals(TypeName.VOID))
            methodBuilder.addStatement("return result");

        return methodBuilder.returns(returnType).build();
    }

    private List<ParameterSpec> getParams(ExecutableElement executableMethodElement) {
        return executableMethodElement.getParameters().stream()
                .map(ParameterSpec::get)
                .toList();
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

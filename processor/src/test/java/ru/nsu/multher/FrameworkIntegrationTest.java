package ru.nsu.multher;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.ServiceLoader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.google.testing.compile.CompilationSubject.assertThat;


public class FrameworkIntegrationTest {

    @Test
    public void testSingleAncestorHierarchy() {
        JavaFileObject interfaceFile = JavaFileObjects.forSourceLines(
                "test.MathOperations",
                "package test;",
                "import ru.nsu.multher.InheritanceRoot;",
                "@InheritanceRoot",
                "public interface MathOperations {",
                "    int add(int a, int b);",
                "}"
        );

        JavaFileObject implFile = JavaFileObjects.forSourceLines(
                "test.BasicMath",
                "package test;",
                "import ru.nsu.multher.ExtendsMultiple;",
                "@ExtendsMultiple({MathOperations.class})",
                "public class BasicMath extends MathOperationsRoot {",
                "    public int add(int a, int b) {",
                "        return a + b;",
                "    }",
                "}"
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new HierarchyRootProcessor())
                .compile(interfaceFile, implFile);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.MathOperationsRoot")
                .contentsAsUtf8String()
                .contains("public abstract class MathOperationsRoot implements MathOperations");
    }

    @Test
    public void testMultipleAncestorsWithNext() {
        JavaFileObject baseInterface = JavaFileObjects.forSourceLines(
                "test.Logger",
                "package test;",
                "import ru.nsu.multher.InheritanceRoot;",
                "@InheritanceRoot",
                "public interface Logger {",
                "    String log(String message);",
                "}"
        );

        JavaFileObject firstImpl = JavaFileObjects.forSourceLines(
                "test.SimpleLogger",
                "package test;",
                "import ru.nsu.multher.ExtendsMultiple;",
                "@ExtendsMultiple({Logger.class})",
                "public class SimpleLogger extends LoggerRoot {",
                "    public String log(String message) {",
                "        return \"Simple: \" + message;",
                "    }",
                "}"
        );

        JavaFileObject secondImpl = JavaFileObjects.forSourceLines(
                "test.DetailedLogger",
                "package test;",
                "import ru.nsu.multher.ExtendsMultiple;",
                "@ExtendsMultiple({SimpleLogger.class})",
                "public class DetailedLogger extends LoggerRoot {",
                "    public String log(String message) {",
                "        String parentResult = nextLog(message);",
                "        return \"Detailed: \" + parentResult;",
                "    }",
                "}"
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new HierarchyRootProcessor())
                .compile(baseInterface, firstImpl, secondImpl);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.LoggerRoot")
                .contentsAsUtf8String()
                .contains("protected String nextLog(String message)");
    }

    @Test
    public void testInvalidAnnotationUsage() {
        JavaFileObject wrongClass = JavaFileObjects.forSourceLines(
                "test.WrongClass",
                "package test;",
                "import ru.nsu.multher.InheritanceRoot;",
                "@InheritanceRoot",
                "public class WrongClass {",
                "    public void doSomething() {}",
                "}"
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new HierarchyRootProcessor())
                .compile(wrongClass);

        assertThat(compilation).failed();
        assertThat(compilation)
                .hadErrorContaining("@InheritanceRoot can only be applied to interfaces");
    }

    @Test
    public void testComplexInterfaceWithMultipleMethods() throws IOException {
        JavaFileObject complexInterface = JavaFileObjects.forSourceLines(
                "test.ComplexOperations",
                "package test;",
                "import ru.nsu.multher.InheritanceRoot;",
                "@InheritanceRoot",
                "public interface ComplexOperations {",
                "    String process(String input, int count);",
                "    double calculate(double a, double b);",
                "    void execute();",
                "}"
        );

        JavaFileObject impl = JavaFileObjects.forSourceLines(
                "test.ComplexImpl",
                "package test;",
                "import ru.nsu.multher.ExtendsMultiple;",
                "@ExtendsMultiple({ComplexOperations.class})",
                "public class ComplexImpl extends ComplexOperationsRoot implements ComplexOperations {",
                "    public String process(String input, int count) {",
                "        return input.repeat(count);",
                "    }",
                "    public double calculate(double a, double b) {",
                "        return a + b;",
                "    }",
                "    public void execute() {",
                "        System.out.println(\"Executing\");",
                "    }",
                "}"
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new HierarchyRootProcessor())
                .compile(complexInterface, impl);

        assertThat(compilation).succeeded();
        Optional<JavaFileObject> generatedFile = compilation.generatedSourceFile("test.ComplexOperationsRoot");
        assertTrue(generatedFile.isPresent(), "Generated file should be present");

        String generatedSource = generatedFile.get().getCharContent(true).toString();

        assertTrue(generatedSource.contains("protected String nextProcess(String input, int count)"),
                "Should contain nextProcess method");
        assertTrue(generatedSource.contains("protected double nextCalculate(double a, double b)"),
                "Should contain nextCalculate method");
        assertTrue(generatedSource.contains("protected void nextExecute()"),
                "Should contain nextExecute method");
    }

    @Test
    public void testMultipleInheritanceMechanism() throws Exception {
        JavaFileObject baseA = JavaFileObjects.forSourceLines(
                "test.BaseA",
                "package test;",
                "import ru.nsu.multher.InheritanceRoot;",
                "@InheritanceRoot",
                "public interface BaseA {",
                "    void methodA();",
                "}"
        );

        JavaFileObject baseB = JavaFileObjects.forSourceLines(
                "test.BaseB",
                "package test;",
                "import ru.nsu.multher.InheritanceRoot;",
                "@InheritanceRoot",
                "public interface BaseB {",
                "    void methodB();",
                "}"
        );

        JavaFileObject derived = JavaFileObjects.forSourceLines(
                "test.Derived",
                "package test;",
                "import ru.nsu.multher.ExtendsMultiple;",
                "@ExtendsMultiple({BaseA.class, BaseB.class})",
                "public interface Derived extends BaseA, BaseB { }"
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new HierarchyRootProcessor())
                .compile(baseA, baseB, derived);

        CompilationSubject.assertThat(compilation).succeeded();
    }
}

package ru.nsu.multher;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;


public class HierarchyRootProcessorTest {

//    @Test
    public void testBasicInterfaceProcessing() {
        // Создаем тестовый файл с интерфейсом, аннотированным @InheritanceRoot
        JavaFileObject inputFile = JavaFileObjects.forSourceLines(
                "test.SimpleInterface",
                "package test;",
                "",
                "import ru.nsu.multher.InheritanceRoot;",
                "",
                "@InheritanceRoot",
                "public interface SimpleInterface {",
                "    void doSomething(String param);",
                "    int calculate(int a, int b);",
                "}"
        );

        // Запускаем компиляцию с процессором
        Compilation compilation = Compiler.javac()
                .withProcessors(new HierarchyRootProcessor())
                .compile(inputFile);

        // Проверяем, что компиляция успешна
        CompilationSubject.assertThat(compilation).succeeded();

        // Проверяем, что сгенерирован нужный файл и проверяем его содержимое
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("test.SimpleInterfaceRoot")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceLines(
                        "test.SimpleInterfaceRoot",
                        "package test;",
                        "",
                        "import java.lang.String;",
                        "",
                        "public abstract class SimpleInterfaceRoot implements SimpleInterface {",
                        "  protected SimpleInterface next;",
                        "",
                        "  public abstract void doSomething(String param);",
                        "",
                        "  protected void nextDoSomething(String param) {",
                        "    next.doSomething(param);",
                        "  }",
                        "",
                        "  public abstract int calculate(int a, int b);",
                        "",
                        "  protected int nextCalculate(int a, int b) {",
                        "    return next.calculate(a, b);",
                        "  }",
                        "}"
                ));
    }
}
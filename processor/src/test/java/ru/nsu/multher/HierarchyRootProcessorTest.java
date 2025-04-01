package ru.nsu.multher;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;


public class HierarchyRootProcessorTest {

    @Test
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
                        "import java.lang.Class;",
                        "import java.lang.String;",
                        "import java.util.ArrayList;",
                        "",
                        "public abstract class SimpleInterfaceRoot implements SimpleInterface {",
                        "  private ArrayList<SimpleInterface> ancestors;",
                        "",
                        "  private ArrayList<SimpleInterface> possibleNextInsts;",
                        "",
                        "  protected SimpleInterfaceRoot() {",
                        "    this.ancestors = new ArrayList<>();",
                        "    this.possibleNextInsts = new ArrayList<>();",
                        "    var classes = ru.nsu.multher.AncestorsTopSorter.getTopSortedAncestorsClasses(this.getClass());",
                        "    for (Class<?> ancestorClass : classes) {",
                        "      try {",
                        "        this.ancestors.add((SimpleInterface) ancestorClass.getDeclaredConstructor().newInstance());",
                        "      } catch (Exception e) {",
                        "        throw new RuntimeException(\"Failed to instantiate ancestor \" + ancestorClass.getName(), e);",
                        "      }",
                        "    }",
                        "  }",
                        "",
                        "  public ArrayList<Class<?>> getAncestorsClasses() {",
                        "    var ancestorsClasses = new ArrayList<Class<?>>();",
                        "    for (var ancestor : ancestors) {",
                        "      ancestorsClasses.add(ancestor.getClass());",
                        "    }",
                        "    return ancestorsClasses;",
                        "  }",
                        "",
                        "  public abstract void doSomething(String param);",
                        "",
                        "  protected void nextDoSomething(String param) {",
                        "    if (ancestors.isEmpty()) throw new java.lang.IllegalAccessError(\"Only types with ancestors specified by @ExtendsMultiple allowed to call next methods\");",
                        "    possibleNextInsts.addAll(ancestors);",
                        "    SimpleInterfaceRoot nextInstance = (SimpleInterfaceRoot) possibleNextInsts.remove(0);",
                        "    nextInstance.possibleNextInsts.addAll(possibleNextInsts);",
                        "    possibleNextInsts.clear();",
                        "    nextInstance.doSomething(param);",
                        "    nextInstance.possibleNextInsts.clear();",
                        "  }",
                        "",
                        "  public abstract int calculate(int a, int b);",
                        "",
                        "  protected int nextCalculate(int a, int b) {",
                        "    if (ancestors.isEmpty()) throw new java.lang.IllegalAccessError(\"Only types with ancestors specified by @ExtendsMultiple allowed to call next methods\");",
                        "    possibleNextInsts.addAll(ancestors);",
                        "    SimpleInterfaceRoot nextInstance = (SimpleInterfaceRoot) possibleNextInsts.remove(0);",
                        "    nextInstance.possibleNextInsts.addAll(possibleNextInsts);",
                        "    possibleNextInsts.clear();",
                        "    var result = nextInstance.calculate(a, b);",
                        "    nextInstance.possibleNextInsts.clear();",
                        "    return result;",
                        "  }",
                        "}"
                ));
    }
}
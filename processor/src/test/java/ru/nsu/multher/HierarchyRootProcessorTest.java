package ru.nsu.multher;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class HierarchyRootProcessorTest {

    @Test
    void testHierarchyRootProcessor_GeneratesRootClass() {
        // Исходный код тестового интерфейса с аннотацией @InheritanceRoot
        JavaFileObject interfaceSource = JavaFileObjects.forSourceString(
                "ru.nsu.multher.TestInterface",
                """
                package ru.nsu.multher;
    
                @InheritanceRoot
                public interface TestInterface {
                    void doSomething();
                }
                """
        );

        // Запускаем компиляцию с нашим аннотированным процессором
        Compilation compilation = javac()
                .withProcessors(new HierarchyRootProcessor())
                .compile(interfaceSource);

        // Проверяем, что компиляция завершилась успешно
        assertThat(compilation).succeeded();

        // Ожидаемое содержимое сгенерированного корневого класса
        JavaFileObject expectedRootClass = JavaFileObjects.forSourceString(
                "ru.nsu.multher.TestInterfaceRoot",
                """
                package ru.nsu.multher;
    
                public abstract class TestInterfaceRoot implements TestInterface {
                    protected TestInterface next;
    
                    public abstract void doSomething();
    
                    protected void nextDoSomething() {
                        next.doSomething();
                    }
                }
                """
        );

        // Проверяем, что сгенерированный код соответствует ожиданиям
        assertThat(compilation)
                .generatedSourceFile("ru.nsu.multher.TestInterfaceRoot")
                .hasSourceEquivalentTo(expectedRootClass);
    }
}

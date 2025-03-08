package processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Set;

@SupportedAnnotationTypes("processor.LogIt")
@SupportedSourceVersion(SourceVersion.RELEASE_23)
public class Processor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(LogIt.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                generateCopy((TypeElement) element);
            }
        }
        return true;
    }

    private void generateCopy(TypeElement origin) {
        String packageName = processingEnv.getElementUtils().getPackageOf(origin).getQualifiedName().toString();
        String rootClassName = origin.getSimpleName() + "Gen";
        String fullClassName = packageName.isEmpty() ? rootClassName : packageName + "." + rootClassName;

        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler()
                .createSourceFile(fullClassName).openWriter())) {
            writer.println("package " + packageName + ";");
            writer.println("public class " + rootClassName + " extends " + origin.getQualifiedName() + " {");
            writer.println("public void new_test_method() {System.out.println(\"Aboba\");}");
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package ru.nsu.app;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static ArrayList<Class<?>> getParentsClassesRecursively(Class<?> clazz, Class<?> startClass) {
        var ancestorsClasses = new ArrayList<Class<?>>();
        var annotation = clazz.getAnnotation(ru.nsu.multher.ExtendsMultiple.class);;
        if (annotation != null) {
            for (Class<?> ancestorClass : annotation.value()) {
                if (ancestorClass.equals(startClass)) {
                    throw new java.lang.IllegalAccessError("Cycled inheritance is prohibited");
                }
                ancestorsClasses.add(ancestorClass);
                ancestorsClasses.addAll(getParentsClassesRecursively(
                        ancestorClass, startClass
                ));
            }
        }
        return ancestorsClasses;
    }

    private static List<Class<?>> getAncestorsClasses(Class<?> childClass) {
        var allAncestorsClasses = getParentsClassesRecursively(
                childClass, childClass
        );
        var ancestorsOrdered = new ArrayList<Class<?>>();

        for (var clazz : allAncestorsClasses.reversed()) {
            if (!ancestorsOrdered.contains(clazz)) {
                ancestorsOrdered.add(clazz);
            }
        }

        return ancestorsOrdered.reversed();
    }

    public static void main(String[] args) {

        System.out.println(BottomChild.class.getName() + ": " + getAncestorsClasses(BottomChild.class));
        System.out.println(ChildA.class.getName() + ": " + getAncestorsClasses(ChildA.class));
        System.out.println(MultiChild.class.getName() + ": " + getAncestorsClasses(MultiChild.class));
        System.out.println(StateChild.class.getName() + ": " + getAncestorsClasses(StateChild.class));
        System.out.println(StateBottomChild.class.getName() + ": " + getAncestorsClasses(StateBottomChild.class));


        BottomChild bottomChild = new BottomChild();
        basicTestPipeline(bottomChild);
        basicTestPipeline(bottomChild);
        System.out.println();

        ChildA childA = new ChildA();
        basicTestPipeline(childA);
        System.out.println();

        MultiChild multiChild = new MultiChild();
        basicTestPipeline(multiChild);
        System.out.println();

        StateChild stateChild = new StateChild(4);
        basicTestPipeline(stateChild);
        System.out.println();

        StateBottomChild stateBottomChild  = new StateBottomChild(2);
        basicTestPipeline(stateBottomChild);
        System.out.println();
    }

    private static void basicTestPipeline(ITestRoot inherited) {
        inherited.say();
        System.out.println(inherited.getPlus1(0));
    }
}
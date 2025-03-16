package ru.nsu.multher;

import java.util.ArrayList;

public class AncestorsTopSorter {
    public static ArrayList<Class<?>> getTopstoredAncestorsClasses(Class<?> childClass) {
        var allAncestorsClasses = getParentsClassesRecursively(childClass, childClass);
        var ancestorsOrdered = new ArrayList<Class<?>>();
        for (var clazz : allAncestorsClasses.reversed()) {
            if (!ancestorsOrdered.contains(clazz)) {
                ancestorsOrdered.add(clazz);
            }
        }
        return new ArrayList<>(ancestorsOrdered.reversed());
    }

    private static ArrayList<Class<?>> getParentsClassesRecursively(Class<?> clazz,
                                                                    Class<?> startClass) {
        var ancestorsClasses = new ArrayList<Class<?>>();
        ru.nsu.multher.ExtendsMultiple annotation = clazz.getAnnotation(ru.nsu.multher.ExtendsMultiple.class);
        if (annotation != null) {
            for (Class<?> ancestorClass : annotation.value()) {
                if (ancestorClass.equals(startClass)) {
                    throw new java.lang.IllegalAccessError("Cycled inheritance is prohibited");
                }
                ancestorsClasses.add(ancestorClass);
                ancestorsClasses.addAll(getParentsClassesRecursively(ancestorClass, startClass));
            }
        }
        return ancestorsClasses;
    }
}

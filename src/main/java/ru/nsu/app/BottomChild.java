package ru.nsu.app;

import ru.nsu.multher.ExtendsMultiple;

@ExtendsMultiple({ ChildA.class, ChildB.class })
public class BottomChild extends ITestRoot {
    @Override
    public void say() {
        System.out.println("Bottom");
        super.nextSay();
    }
}

package ru.nsu.app;

import ru.nsu.multher.ExtendsMultiple;

@ExtendsMultiple({ RealRoot.class })
public class ChildB extends ITestRoot {
    @Override
    public void say() {
        System.out.println("B");
        super.nextSay();
    }
}

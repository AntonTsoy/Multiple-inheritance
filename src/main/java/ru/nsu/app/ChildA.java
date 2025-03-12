package ru.nsu.app;

import ru.nsu.multher.ExtendsMultiple;

@ExtendsMultiple({ RealRoot.class })
public class ChildA extends ITestRoot {
    @Override
    public void say() {
        System.out.println("A");
        super.nextSay();
    }

    @Override
    public int getPlus1(int num) {
        return super.nextGetPlus1(num) + 1;
    }
}

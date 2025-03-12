package ru.nsu.app;

import ru.nsu.multher.ExtendsMultiple;

@ExtendsMultiple({ ChildA.class, ChildB.class })
public class StateChild extends ITestRoot {
    private int plusVal = 0;

    public StateChild() {}

    public StateChild(int plusVal) {
        this.plusVal = plusVal;
    }

    @Override
    public void say() {
        System.out.println("State");
        super.nextSay();
    }

    @Override
    public int getPlus1(int num) {
        return super.nextGetPlus1(num) + plusVal;
    }
}

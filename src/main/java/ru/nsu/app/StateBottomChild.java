package ru.nsu.app;

import ru.nsu.multher.ExtendsMultiple;

@ExtendsMultiple({ StateChild.class })
public class StateBottomChild extends ITestRoot {
    private final int plusVal;

    public StateBottomChild(int plusVal) {
        this.plusVal = plusVal;
    }

    @Override
    public void say() {
        System.out.println("Bottom state");
        super.nextSay();
    }

    @Override
    public int getPlus1(int num) {
        return super.nextGetPlus1(num) + plusVal;
    }
}

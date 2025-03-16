package ru.nsu.app;

import ru.nsu.multher.ExtendsMultiple;

@ExtendsMultiple({ ChildA.class })
public class BreakingChild extends ITestRoot {
    @Override
    public void say() {
        System.out.println("Breaking");
    }

    @Override
    public int getPlus1(int num) {
        return 1;
    }
}

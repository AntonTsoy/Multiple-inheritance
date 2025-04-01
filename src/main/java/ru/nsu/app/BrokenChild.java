package ru.nsu.app;

import ru.nsu.multher.ExtendsMultiple;

@ExtendsMultiple({ BottomChild.class, BreakingChild.class })
public class BrokenChild extends ITestRoot {
    @Override
    public void say() {
        System.out.println("Under broking");
        super.nextSay();
    }

    @Override
    public int getPlus1(int num) {
        return super.nextGetPlus1(num) + 1;
    }
}

package ru.nsu.app;

import ru.nsu.multher.ExtendsMultiple;

@ExtendsMultiple({ ChildA.class, BottomChild.class, RealRoot.class })
public class DiamondChild extends ITestRoot {
    @Override
    public void say() {
        System.out.println("Multi");
        super.nextSay();
    }

    @Override
    public int getPlus1(int num) {
        return super.nextGetPlus1(num) + 1;
    }
}

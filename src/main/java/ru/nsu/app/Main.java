package ru.nsu.app;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BottomChild bottomChild = new BottomChild();
        basicTestPipeline(bottomChild);
        basicTestPipeline(bottomChild);
        System.out.println();

        ChildA childA = new ChildA();
        basicTestPipeline(childA);
        System.out.println();

        DiamondChild diamondChild = new DiamondChild();
        basicTestPipeline(diamondChild);
        System.out.println();

        BrokenChild brokenChild = new BrokenChild();
        basicTestPipeline(brokenChild);
        System.out.println();

        StateChild stateChild = new StateChild(4);
        basicTestPipeline(stateChild);
        System.out.println();

        StateBottomChild stateBottomChild  = new StateBottomChild(2);
        basicTestPipeline(stateBottomChild);
        System.out.println();
    }

    private static void basicTestPipeline(ITestRoot inherited) {
        System.out.println(
            inherited.getClass().getName() + ": " + inherited.getAncestorsClasses()
        );
        inherited.say();
        System.out.println(inherited.getPlus1(0));
    }
}
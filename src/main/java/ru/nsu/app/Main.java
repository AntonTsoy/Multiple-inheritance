package ru.nsu.app;

public class Main {
    public static void main(String[] args) {
        BottomChild bottomChild = new BottomChild();
        basicTestPipeline(bottomChild);
        basicTestPipeline(bottomChild);
        System.out.println();

        ChildA childA = new ChildA();
        basicTestPipeline(childA);
        System.out.println();

        MultiChild multiChild = new MultiChild();
        basicTestPipeline(multiChild);
        System.out.println();

        StateChild stateChild = new StateChild(4);
        basicTestPipeline(stateChild);
        System.out.println();

        StateBottomChild stateBottomChild  = new StateBottomChild(2);
        basicTestPipeline(stateBottomChild);
        System.out.println();
    }

    private static void basicTestPipeline(ITestRoot inherited) {
        inherited.say();
        System.out.println(inherited.getPlus1(0));
    }
}
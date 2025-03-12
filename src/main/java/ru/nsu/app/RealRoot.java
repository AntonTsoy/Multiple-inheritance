package ru.nsu.app;

public class RealRoot extends ITestRoot {
    @Override
    public void say() {
        System.out.println("Root");
//        super.nextSay(); // Throws error (it's good)
    }

    @Override
    public int getPlus1(int num) {
        return num + 1;
    }
}

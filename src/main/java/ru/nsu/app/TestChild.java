package ru.nsu.app;

import java.util.ArrayList;

// TODO: Добавить аннотацию ExtendsMultiple как только она появится
public class TestChild extends ITestRoot {
    @Override
    public void voidFn(int num) {
//        super.nextVoidFn();
        System.out.println("Test " + num);
    }

    @Override
    public int getInt() {
        return 0;
    }

    @Override
    public ArrayList<Integer> aboba() {
        return null;
    }
}

package ru.nsu.app;

import java.util.ArrayList;

// TODO: Добавить аннотацию ExtendsMultiple как только она появится
public class TestChild extends ITestRoot {
    @Override
    public void voidFn() {
//        super.nextVoidFn();
        System.out.println("Test");
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

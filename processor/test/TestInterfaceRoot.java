package test;

import java.lang.Class;
import java.util.ArrayList;

public abstract class TestInterfaceRoot implements TestInterface {
  private ArrayList<TestInterface> ancestors;

  private ArrayList<TestInterface> possibleNextInsts;

  protected TestInterfaceRoot() {
    this.ancestors = new ArrayList<>();
    this.possibleNextInsts = new ArrayList<>();
    var classes = ru.nsu.multher.AncestorsTopSorter.getTopSortedAncestorsClasses(this.getClass());
    for (Class<?> ancestorClass : classes) {
      try {
        this.ancestors.add((TestInterface) ancestorClass.getDeclaredConstructor().newInstance());
      } catch (Exception e) {
        throw new RuntimeException("Failed to instantiate ancestor " + ancestorClass.getName(), e);
      }
    }
  }

  public ArrayList<Class<?>> getAncestorsClasses() {
    var ancestorsClasses = new ArrayList<Class<?>>();
    for (var ancestor : ancestors) {
      ancestorsClasses.add(ancestor.getClass());
    }
    return ancestorsClasses;
  }

  public abstract void doSomething();

  protected void nextDoSomething() {
    if (ancestors.isEmpty()) throw new java.lang.IllegalAccessError("Only types with ancestors specified by @ExtendsMultiple allowed to call next methods");
    possibleNextInsts.addAll(ancestors);
    TestInterfaceRoot nextInstance = (TestInterfaceRoot) possibleNextInsts.remove(0);
    nextInstance.possibleNextInsts.addAll(possibleNextInsts);
    possibleNextInsts.clear();
    nextInstance.doSomething();
    nextInstance.possibleNextInsts.clear();
  }
}

package mycontroller;

public interface MoveStrategy {
    void move(MyAutoController carController);
    // To check whether the strategy has completed or not
    boolean completed();
    // To force the strategy to be completed
    void forceCompleted();
    // To check how much health needed to do the strategy
    int getHealthNeeded();
}

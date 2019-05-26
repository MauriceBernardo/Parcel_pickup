package mycontroller;

public interface MoveStrategy {
    void move(MyAutoController carController);
    // To check whether the strategy has completed or not
    boolean completed();
}

package mycontroller;

import java.util.ArrayList;

public abstract class ExploringMove implements MoveStrategy {
    private boolean completed = false;
    private ArrayList<String> wallTrapTypes;

    public ExploringMove(ArrayList<String> wallTrapTypes) {
        this.wallTrapTypes = wallTrapTypes;
    }

    @Override
    public boolean completed() {
        return this.completed;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public ArrayList<String> getWallTrapTypes() {
        return wallTrapTypes;
    }
}

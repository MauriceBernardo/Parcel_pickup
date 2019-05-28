package mycontroller;

import world.WorldSpatial;

import java.util.LinkedList;

public class MultiPointStrategy implements PointToPointMove {
    private LinkedList<PointToPointMove> pointToPointMoves;
    private LinkedList<PointToPointMove> backtrackMoves;
    private boolean completed = false;

    public MultiPointStrategy() {
        this.pointToPointMoves = new LinkedList<>();
        this.backtrackMoves = new LinkedList<>();
    }

    public void addStrategy(PointToPointMove pointToPointMove) {
        pointToPointMoves.add(pointToPointMove);

        if(pointToPointMoves.peekFirst().completed()){
            setCompleted();
        }
    }

    public void addBacktrackStrategy(PointToPointMove pointToPointMove) {
        backtrackMoves.push(pointToPointMove);

        if(pointToPointMoves.peekFirst().completed()){
            setCompleted();
        }
    }

    @Override
    public int getHealthNeeded() {
        int healthNeeded = 0;
        for (PointToPointMove strategy : pointToPointMoves){
            healthNeeded += strategy.getHealthNeeded();
        }
        return healthNeeded;
    }

    @Override
    public WorldSpatial.Direction getEndOrientation() {
        return pointToPointMoves.peekLast().getEndOrientation();
    }

    @Override
    public void move(MyAutoController carController) {
        if(!pointToPointMoves.isEmpty()) {
            pointToPointMoves.peekFirst().move(carController);

            if (pointToPointMoves.peekFirst().completed()) {
                pointToPointMoves.removeFirst();
            }
        } else if(!backtrackMoves.isEmpty()) {
            backtrackMoves.peekFirst().move(carController);

            if (backtrackMoves.peekFirst().completed()) {
                backtrackMoves.removeFirst();
            }
        } else {
            setCompleted();
        }
    }

    @Override
    public boolean completed() {
        return completed;
    }

    @Override
    public void forceCompleted() {
        setCompleted();
    }


    private void setCompleted() {
        completed = true;
    }
}

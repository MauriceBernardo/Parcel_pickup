package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;

import java.util.LinkedList;

public abstract class PointToPoint implements MoveStrategy{
    private boolean completed = false;
    private boolean backtrack;
    private Coordinate destination;
    private LinkedList<Coordinate> pathCoordinate = new LinkedList<>();
    private LinkedList<Command> moveCommand = new LinkedList<>();
    private LinkedList<Command> reverseCommand = new LinkedList<>();

    // Class for the point to point algorithm
    public class Point {
        int weight;
        Source source;
        Coordinate coordinate;

        public Point(int weight, Source source, Coordinate coordinate) {
            this.weight = weight;
            this.source = source;
            this.coordinate = coordinate;
        }
    }

    enum Source {
        DOWN, LEFT, ORIGIN, RIGHT, UP, IMPASSABLE
    }

    enum Command {
        FORWARD, LEFT, RIGHT, REVERSE, BRAKE, NO_COMMAND
    }

    public PointToPoint(Coordinate destination, boolean backtrack) {
        this.destination = destination;
        this.backtrack = backtrack;
    }

    public PointToPoint(int destX, int destY, boolean backtrack) {
        this.destination = new Coordinate(destX, destY);
        this.backtrack = backtrack;
    }

    public Coordinate getDestination() {
        return destination;
    }

    public LinkedList<Coordinate> getPathCoordinate() {
        return pathCoordinate;
    }

    public LinkedList<Command> getMoveCommand() {
        return moveCommand;
    }

    public void setCompleted() {
        this.completed = true;
    }

    @Override
    public boolean completed() {
        return this.completed;
    }

    // Translate the set of coordinates made into move command and the reverse move command
    public void translateToMoveCommand(WorldSpatial.Direction orientation) {
        LinkedList<Coordinate> clonePathCoordinate = new LinkedList<>(pathCoordinate);
        Coordinate sourceCoordinate = clonePathCoordinate.remove();

        // Assume using forward translation algorithm
        boolean forward = true;
        WorldSpatial.Direction lastDirection = orientation;

        // Decide whether using reverse translate or forward translate from the orientation
        if (!clonePathCoordinate.isEmpty()) {
            Coordinate nextCoordinate = clonePathCoordinate.peek();
            if (sourceCoordinate.getLeftCoordinate().equals(nextCoordinate.toString())) {
                if (orientation == WorldSpatial.Direction.EAST) {
                    forward = false;
                }
            } else if (sourceCoordinate.getRightCoordinate().equals(nextCoordinate.toString())) {
                if (orientation == WorldSpatial.Direction.WEST) {
                    forward = false;
                }
            } else if (sourceCoordinate.getUpCoordinate().equals(nextCoordinate.toString())) {
                if (orientation == WorldSpatial.Direction.SOUTH) {
                    forward = false;
                }
            } else if (sourceCoordinate.getDownCoordinate().equals(nextCoordinate.toString())) {
                if (orientation == WorldSpatial.Direction.NORTH) {
                    forward = false;
                }
            }
        }

        // Decide the move command and the reverse command
        reverseCommand.push(Command.BRAKE);
        while (!clonePathCoordinate.isEmpty()) {
            Coordinate destCoordinate = clonePathCoordinate.remove();

            if (checkForward(sourceCoordinate, destCoordinate, lastDirection)) {
                moveCommand.add(Command.FORWARD);
                reverseCommand.push(Command.REVERSE);
            } else if (checkReverse(sourceCoordinate, destCoordinate, lastDirection)) {
                moveCommand.add(Command.REVERSE);
                reverseCommand.push(Command.FORWARD);
            } else if (checkLeft(sourceCoordinate, destCoordinate, lastDirection)) {
                if (forward) {
                    lastDirection = WorldSpatial.changeDirection(lastDirection, WorldSpatial.RelativeDirection.LEFT);
                } else {
                    lastDirection = WorldSpatial.reverseDirection(WorldSpatial.changeDirection(lastDirection, WorldSpatial.RelativeDirection.LEFT));
                }
                moveCommand.add(Command.LEFT);
                reverseCommand.push(Command.LEFT);
            } else if (checkRight(sourceCoordinate, destCoordinate, lastDirection)) {
                if (forward) {
                    lastDirection = WorldSpatial.changeDirection(lastDirection, WorldSpatial.RelativeDirection.RIGHT);
                } else {
                    lastDirection = WorldSpatial.reverseDirection(WorldSpatial.changeDirection(lastDirection, WorldSpatial.RelativeDirection.RIGHT));
                }
                moveCommand.add(Command.RIGHT);
                reverseCommand.push(Command.RIGHT);
            }
            sourceCoordinate = destCoordinate;
        }

        moveCommand.add(Command.BRAKE);
    }

    // Apply the command from the set of move command
    public void applyCommand(MyAutoController carController){
        Command command;
        if(moveCommand.isEmpty() && !reverseCommand.isEmpty() && backtrack){
            command = reverseCommand.remove();
        } else if (!moveCommand.isEmpty()){
            command = moveCommand.remove();
        } else {
            command = Command.NO_COMMAND;
        }
        switch (command) {
            case FORWARD:
                carController.applyForwardAcceleration();
                break;
            case REVERSE:
                carController.applyReverseAcceleration();
                break;
            case RIGHT:
                carController.turnRight();
                break;
            case LEFT:
                carController.turnLeft();
                break;
            case BRAKE:
                carController.applyBrake();
                break;
            default:
                setCompleted();
                break;
        }
    }

    // Check whether the destCoordinate is in the right relative to the car orientation
    private boolean checkRight(Coordinate sourceCoordinate, Coordinate destCoordinate, WorldSpatial.Direction orientation) {
        switch (orientation) {
            case EAST:
                if (sourceCoordinate.getDownCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case WEST:
                if (sourceCoordinate.getUpCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case NORTH:
                if (sourceCoordinate.getRightCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case SOUTH:
                if (sourceCoordinate.getLeftCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
        }
        return false;
    }

    // Check whether the destCoordinate is in the Left relative to the car orientation
    private boolean checkLeft(Coordinate sourceCoordinate, Coordinate destCoordinate, WorldSpatial.Direction orientation) {
        switch (orientation) {
            case EAST:
                if (sourceCoordinate.getUpCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case WEST:
                if (sourceCoordinate.getDownCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case NORTH:
                if (sourceCoordinate.getLeftCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case SOUTH:
                if (sourceCoordinate.getRightCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
        }
        return false;
    }

    // Check whether the destCoordinate is in the back relative to the car orientation
    private boolean checkReverse(Coordinate sourceCoordinate, Coordinate destCoordinate, WorldSpatial.Direction orientation) {
        switch (orientation) {
            case EAST:
                if (sourceCoordinate.getLeftCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case WEST:
                if (sourceCoordinate.getRightCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case NORTH:
                if (sourceCoordinate.getDownCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case SOUTH:
                if (sourceCoordinate.getUpCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
        }
        return false;
    }

    // Check whether the destCoordinate is in the front relative to the car orientation
    private boolean checkForward(Coordinate sourceCoordinate, Coordinate destCoordinate, WorldSpatial.Direction orientation) {
        switch (orientation) {
            case EAST:
                if (sourceCoordinate.getRightCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case WEST:
                if (sourceCoordinate.getLeftCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case NORTH:
                if (sourceCoordinate.getUpCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case SOUTH:
                if (sourceCoordinate.getDownCoordinate().equals(destCoordinate.toString())) {
                    return true;
                }
                break;
        }
        return false;
    }

    // Apply the reverse command
    public void applyReverseCommand(){
        moveCommand = reverseCommand;
        completed = false;
    }
}

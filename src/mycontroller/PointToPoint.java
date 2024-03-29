package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;

import java.util.LinkedList;

/**
 * The utility class Point to point for PointToPointMove class.
 */
public class PointToPoint {
    private LinkedList<Command> moveCommand;
    private LinkedList<Command> reverseCommand;

    public PointToPoint() {
        this.moveCommand = new LinkedList<>();
        this.reverseCommand = new LinkedList<>();
    }

    public enum Command {
        FORWARD,
        LEFT,
        RIGHT,
        REVERSE,
        BRAKE,
        NO_COMMAND
    }

    // Check whether the destCoordinate is in the front relative to the car orientation
    private boolean checkForward(Coordinate sourceCoordinate, Coordinate destCoordinate, WorldSpatial.Direction orientation) {
        switch (orientation) {
            case EAST:
                if (getRightCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case WEST:
                if (getLeftCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case NORTH:
                if (getUpCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case SOUTH:
                if (getDownCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
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
                if (getLeftCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case WEST:
                if (getRightCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case NORTH:
                if (getDownCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case SOUTH:
                if (getUpCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
        }
        return false;
    }

    // Check whether the destCoordinate is in the right relative to the car orientation
    private boolean checkRight(Coordinate sourceCoordinate, Coordinate destCoordinate, WorldSpatial.Direction orientation) {
        switch (orientation) {
            case EAST:
                if (getDownCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case WEST:
                if (getUpCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case NORTH:
                if (getRightCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case SOUTH:
                if (getLeftCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
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
                if (getUpCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case WEST:
                if (getDownCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case NORTH:
                if (getLeftCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
            case SOUTH:
                if (getRightCoordinate(sourceCoordinate).equals(destCoordinate.toString())) {
                    return true;
                }
                break;
        }
        return false;
    }

    // See the first move command after initial Brake
    public Command peekFirstMoveCommand(){
        return moveCommand.get(1);
    }

    // Translate the set of coordinates made into move command
    public void translateToMoveCommand(WorldSpatial.Direction orientation, LinkedList<Coordinate> pathCoordinate) {
        LinkedList<Coordinate> clonePathCoordinate = new LinkedList<>(pathCoordinate);
        Coordinate sourceCoordinate = clonePathCoordinate.remove();

        // Assume using forward translation algorithm
        boolean forward = true;
        WorldSpatial.Direction lastDirection = orientation;

        // Decide whether using reverse translate or forward translate from the orientation
        if (!clonePathCoordinate.isEmpty()) {
            Coordinate nextCoordinate = clonePathCoordinate.peek();
            if (getLeftCoordinate(sourceCoordinate).equals(nextCoordinate.toString())) {
                if (orientation == WorldSpatial.Direction.EAST) {
                    forward = false;
                }
            } else if (getRightCoordinate(sourceCoordinate).equals(nextCoordinate.toString())) {
                if (orientation == WorldSpatial.Direction.WEST) {
                    forward = false;
                }
            } else if (getUpCoordinate(sourceCoordinate).equals(nextCoordinate.toString())) {
                if (orientation == WorldSpatial.Direction.SOUTH) {
                    forward = false;
                }
            } else if (getDownCoordinate(sourceCoordinate).equals(nextCoordinate.toString())) {
                if (orientation == WorldSpatial.Direction.NORTH) {
                    forward = false;
                }
            }
        }

        // Start move with brake
        moveCommand.add(Command.BRAKE);

        // Decide the move command and the reverse command
        while (!clonePathCoordinate.isEmpty()) {
            Coordinate destCoordinate = clonePathCoordinate.remove();

            if (checkForward(sourceCoordinate, destCoordinate, lastDirection)) {
                moveCommand.add(Command.FORWARD);
            } else if (checkReverse(sourceCoordinate, destCoordinate, lastDirection)) {
                moveCommand.add(Command.REVERSE);
            } else if (checkLeft(sourceCoordinate, destCoordinate, lastDirection)) {
                if (forward) {
                    lastDirection = WorldSpatial.changeDirection(lastDirection, WorldSpatial.RelativeDirection.LEFT);
                } else {
                    lastDirection = WorldSpatial.reverseDirection(WorldSpatial.changeDirection(lastDirection, WorldSpatial.RelativeDirection.LEFT));
                }
                moveCommand.add(Command.LEFT);
            } else if (checkRight(sourceCoordinate, destCoordinate, lastDirection)) {
                if (forward) {
                    lastDirection = WorldSpatial.changeDirection(lastDirection, WorldSpatial.RelativeDirection.RIGHT);
                } else {
                    lastDirection = WorldSpatial.reverseDirection(WorldSpatial.changeDirection(lastDirection, WorldSpatial.RelativeDirection.RIGHT));
                }
                moveCommand.add(Command.RIGHT);
            }
            sourceCoordinate = destCoordinate;
        }

        // End move with Brake
        if (moveCommand.peekLast() != Command.BRAKE) {
            moveCommand.add(Command.BRAKE);
        }
    }

    // Translate moveCommand into reverse command and consider it to be applied
    public void considerReverseCommand() {
        reverseCommand = new LinkedList<>();
        for (Command command : moveCommand) {
            if (command == Command.FORWARD) {
                reverseCommand.push(Command.REVERSE);
            } else if (command == Command.REVERSE) {
                reverseCommand.push(Command.FORWARD);
            } else {
                reverseCommand.push(command);
            }
        }

        if(reverseCommand.size() == 1){
            return;
        }

        // Remove the last and first brake
        reverseCommand.removeLast();
        reverseCommand.removeFirst();

        // Remove and get the last command
        Command lastCommand = reverseCommand.removeLast();

        // Put the last command to firstCommand
        reverseCommand.push(lastCommand);

        // Add Brake to first and Last
        reverseCommand.push(Command.BRAKE);
        reverseCommand.add(Command.BRAKE);
    }

    private void applyCommand(MyAutoController carController, LinkedList<Command> moveCommand) {
        Command command;
        if (!moveCommand.isEmpty()) {
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
                break;
        }
    }

    // Apply the command from the set of move command
    public boolean applyMoveCommand(MyAutoController carController) {
        applyCommand(carController, moveCommand);

        // Indicate whether there's more command to be executed or not (completed or not)
        if (moveCommand.isEmpty()) {
            return false;
        }
        return true;
    }

    // Apply the command from reverse move command
    public boolean applyReverseCommand(MyAutoController carController){
        applyCommand(carController, reverseCommand);

        // Indicate whether there's more command to be executed or not (completed or not)
        if (reverseCommand.isEmpty()) {
            return false;
        }
        return true;
    }

    // get the direct west coordinate of a Coordinate
    public String getLeftCoordinate(Coordinate coordinate){
        return (coordinate.x-1)+","+coordinate.y;
    }

    // get the direct east coordinate of a Coordinate
    public String getRightCoordinate(Coordinate coordinate){
        return (coordinate.x+1)+","+coordinate.y;
    }

    // get the direct north coordinate of a Coordinate
    public String getUpCoordinate(Coordinate coordinate){
        return coordinate.x+","+(coordinate.y+1);
    }

    // get the direct south coordinate of a Coordinate
    public String getDownCoordinate(Coordinate coordinate){
        return coordinate.x+","+(coordinate.y-1);
    }
}

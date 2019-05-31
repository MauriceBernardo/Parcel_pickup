package mycontroller;

import tiles.*;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Latching strategy.
 */
public class LatchingStrategy implements ExploringMove {
    private int oneStep = 1;
    private Coordinate initialCoordinate = null;
    private boolean findWayOutReversing = false;
    private boolean completed = false;
    private ArrayList<String> wallTrapTypes;
    private boolean foundRoadFrontRight = false;
    private boolean foundWallBottomRight = false;

    /**
     * Instantiates a new Latching strategy.
     *
     * @param wallTrapTypes the wall trap types
     */
    public LatchingStrategy(ArrayList<String> wallTrapTypes) {
        this.wallTrapTypes = wallTrapTypes;
    }

    @Override
    public boolean completed() {
        return this.completed;
    }

    @Override
    public void forceCompleted() {
        setCompleted();
    }

    @Override
    public int getHealthNeeded() {
        // No need to consider health for latching strategy
        return 0;
    }

    @Override
    public void move(MyAutoController carController) {
        WorldSpatial.Direction orientation = carController.getOrientation();
        HashMap<Coordinate, MapTile> currentView = carController.getView();
        String currPos = carController.getPosition();
        Coordinate currentPosition = new Coordinate(currPos);

        if (!isCompleted()) {
            // move forward if the car is reversing while searching for path out
            if (!findWayOutReversing) {
                carController.applyForwardAcceleration();
            }

            // search for wall to attach
            if (!checkFollowingWall(orientation, currentView, currPos)) {
                if (this.initialCoordinate == null) {
                    findInitialWallToAttach(carController);
                } else {
                    findWallToAttach(carController);
                }

            }
            // find path out of the corner
            else if (checkWallAhead(orientation, currentView, currPos) || this.findWayOutReversing) {
                findWayOut(carController);

            }

            // remove the type of wall (health / water / lava) depends on their priority level when a round is done
            if (currentPosition.equals(this.initialCoordinate)) {
                if (wallTrapTypes.size() != 0) {
                    int priorityRemoveIndex = getPriorityRemoveIndex();

                    wallTrapTypes.remove(priorityRemoveIndex);

                } else {
                    this.setCompleted();
                    carController.applyBrake();

                }

            } else if (this.initialCoordinate == null && checkFollowingWall(orientation, currentView, currPos)) {
                this.initialCoordinate = new Coordinate(currentPosition.x, currentPosition.y);
            }
        }
    }

    private void setCompleted() {
        this.completed = true;
    }

    private boolean isCompleted() {
        return completed;
    }

    /**
     * Find initial wall to attach.
     *
     * @param carController the car controller
     */
    private void findInitialWallToAttach(MyAutoController carController) {
        WorldSpatial.Direction orientation = carController.getOrientation();
        HashMap<Coordinate, MapTile> currentView = carController.getView();
        String currPos = carController.getPosition();

        switch(orientation) {
            case EAST:
                if (!checkWallAhead(orientation, currentView, currPos)) {
                    carController.turnRight();
                }
                break;
            case NORTH: case SOUTH: case WEST:
                if (checkWallAhead(orientation, currentView, currPos)) {
                    carController.turnLeft();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Find wall to attach.
     *
     * @param carController the car controller
     */
    private void findWallToAttach(MyAutoController carController) {
        WorldSpatial.Direction orientation = carController.getOrientation();
        HashMap<Coordinate, MapTile> currentView = carController.getView();
        String currPos = carController.getPosition();

        switch(orientation) {
            case EAST:
                if (!checkEastSouth(currentView, currPos) && !findWayOutReversing) {
                    this.foundRoadFrontRight = true;
                }

                // check if there is a path on the left hand side of the car when it is finding way out
                if (findWayOutReversing) {
                    if (!checkNorth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        findWayOutReversing = false;
                    }
                    // when the car hits a wall
                    else if (checkWest(currentView, currPos) && !checkSouth(currentView, currPos)) {
                        carController.turnRight();
                    }

                }

                // one step away from wall (initially attaching wall)
                // condition for reverse and brake :
                // 1. back and front right of the car => not wall
                // 2. back right of the car => wall
                // reverse, brake, forward and turn right so that the car will attach to another wall closer to it
                else if (!checkWest(currentView, currPos) && checkSouthWest(currentView, currPos)
                        && !checkEastSouth(currentView, currPos) && carController.getSpeed() == 0
                        && !this.foundWallBottomRight) {
                    carController.applyReverseAcceleration();
                    carController.applyBrake();
                    this.foundWallBottomRight = true;

                } else if (this.foundWallBottomRight) {
                    carController.turnRight();
                    carController.applyBrake();
                    this.foundWallBottomRight = false;

                }
                else if (checkWallAhead(orientation, currentView, currPos)) {
                    if (this.foundRoadFrontRight) {
                        carController.turnRight();

                        this.foundRoadFrontRight = false;

                    } else if (carController.getSpeed() == 0) {
                        carController.applyReverseAcceleration();
                        carController.applyBrake();

                    } else {
                        carController.turnLeft();
                    }
                }
                // turn right if nothing blocking on the right side of the car
                else if (!checkSouth(currentView, currPos)) {
                    if (findWayOutReversing) {
                        findWayOutReversing = false;
                    }
                    carController.turnRight();
                }
                break;

            case NORTH:
                if (checkNorthEast(currentView, currPos) && !findWayOutReversing) {
                    this.foundRoadFrontRight = true;
                }

                // check if there is a path on the left hand side of the car when it is finding way out
                if (findWayOutReversing) {
                    if (!checkWest(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        findWayOutReversing = false;
                    }
                    // when the car hits a wall
                    else if (checkSouth(currentView, currPos) && !checkEast(currentView, currPos)) {
                        carController.turnRight();
                    }
                }

                // one step away from wall (initially attaching wall)
                // condition for reverse and brake :
                // 1. back and front right of the car => not wall
                // 2. back right of the car => wall
                // reverse, brake, forward and turn right so that the car will attach to another wall closer to it
                else if (!checkSouth(currentView, currPos) && checkEastSouth(currentView, currPos)
                        && !checkNorthEast(currentView, currPos) && carController.getSpeed() == 0
                        && !this.foundWallBottomRight) {
                    carController.applyReverseAcceleration();
                    carController.applyBrake();
                    this.foundWallBottomRight = true;

                } else if (this.foundWallBottomRight) {
                    carController.turnRight();
                    carController.applyBrake();

                    this.foundWallBottomRight = false;

                }

                else if (checkWallAhead(orientation, currentView, currPos)) {
                    if (this.foundRoadFrontRight) {
                        this.foundRoadFrontRight = false;
                        carController.turnRight();

                    } else if (carController.getSpeed() != 0) {
                        carController.turnLeft();

                    } else {
                        carController.applyReverseAcceleration();
                        carController.applyBrake();
                    }
                }

                // turn right if nothing blocking on the right side of the car
                else if (!checkEast(currentView, currPos)) {
                    if (findWayOutReversing) {
                        findWayOutReversing = false;
                    }
                    carController.turnRight();
                }
                break;
            case SOUTH:
                if (!checkSouthWest(currentView, currPos) && !findWayOutReversing) {
                    this.foundRoadFrontRight = true;
                }

                // check if there is a path on the left hand side of the car when it is finding way out
                if (findWayOutReversing) {
                    if (!checkEast(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        findWayOutReversing = false;
                    }
                    // when the car hits a wall
                    else if (checkNorth(currentView, currPos) && !checkWest(currentView, currPos)) {
                        carController.turnRight();
                    }
                }

                // one step away from wall (initially attaching wall)
                // condition for reverse and brake :
                // 1. back and front right of the car => not wall
                // 2. back right of the car => wall
                // reverse, brake, forward and turn right so that the car will attach to another wall closer to it
                else if (!checkNorth(currentView, currPos) && checkWestNorth(currentView, currPos)
                        && !checkSouthWest(currentView, currPos) && carController.getSpeed() == 0
                        && !this.foundWallBottomRight) {
                    carController.applyReverseAcceleration();
                    carController.applyBrake();
                    this.foundWallBottomRight = true;

                } else if (this.foundWallBottomRight) {
                    carController.turnRight();
                    carController.applyBrake();
                    this.foundWallBottomRight = false;
                }

                else if (checkWallAhead(orientation, currentView, currPos)) {
                    if (foundRoadFrontRight) {

                        carController.turnRight();

                        this.foundRoadFrontRight = false;

                    } else if (carController.getSpeed() != 0) {
                        carController.turnLeft();
                    } else {
                        carController.applyReverseAcceleration();
                        carController.applyBrake();
                    }
                }

                // turn right if nothing blocking on the right side of the car
                else if (!checkWest(currentView, currPos)) {
                    if (findWayOutReversing) {
                        findWayOutReversing = false;
                    }
                    carController.turnRight();
                }
                break;


            case WEST:
                if (!checkWestNorth(currentView, currPos) && !findWayOutReversing) {
                    this.foundRoadFrontRight = true;
                }

                // check if there is a path on the left hand side of the car when it is finding way out
                if (findWayOutReversing) {
                    if (!checkSouth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        findWayOutReversing = false;
                    }
                    // when the car hits a wall
                    else if (checkEast(currentView, currPos) && !checkNorth(currentView, currPos)) {
                        carController.turnRight();
                    }
                }

                // one step away from wall (initially attaching wall)
                // condition for reverse and brake :
                // 1. back and front right of the car => not wall
                // 2. back right of the car => wall
                // reverse, brake, forward and turn right so that the car will attach to another wall closer to it
                else if (!checkEast(currentView, currPos) && checkNorthEast(currentView, currPos)
                        && !checkWestNorth(currentView, currPos) && carController.getSpeed() == 0
                        && !this.foundWallBottomRight) {
                    carController.applyReverseAcceleration();
                    carController.applyBrake();
                    this.foundWallBottomRight = true;

                } else if (this.foundWallBottomRight) {
                    carController.turnRight();
                    carController.applyBrake();


                    this.foundWallBottomRight = false;

                } else if (checkWallAhead(orientation, currentView, currPos)) {
                    if (this.foundRoadFrontRight) {
                        carController.turnRight();
                        this.foundRoadFrontRight = false;

                    } else if (carController.getSpeed() != 0) {
                        carController.turnLeft();
                    } else {
                        carController.applyReverseAcceleration();
                        carController.applyBrake();
                    }
                }
                // turn right if nothing blocking on the right side of the car
                else if (!checkNorth(currentView, currPos)) {
                    if (findWayOutReversing) {
                        findWayOutReversing = false;
                    }
                    carController.turnRight();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Find way out when stuck in the corner.
     *
     * @param carController the car controller
     */
    private void findWayOut(MyAutoController carController) {
        WorldSpatial.Direction orientation = carController.getOrientation();
        HashMap<Coordinate, MapTile> currentView = carController.getView();
        String currPos = carController.getPosition();

        switch(orientation) {
            case EAST:

                // turn to desired direction when the car is moving forward
                if (!checkSouth(currentView, currPos) && carController.getSpeed() > 0 && !this.findWayOutReversing) {
                    carController.turnRight();
                } else if (!checkNorth(currentView, currPos) && carController.getSpeed() > 0 && !this.findWayOutReversing) {
                    carController.turnLeft();
                }

                // stuck in the corner
                else {

                    carController.applyReverseAcceleration();

                    if (!checkNorth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        this.findWayOutReversing = false;
                        break;
                    }
                    // reverse and hit the wall
                    else if (checkWest(currentView, currPos)) {
                        carController.applyBrake();
                        this.findWayOutReversing = false;
                        break;
                    }

                    this.findWayOutReversing = true;
                }
                break;

            case NORTH:

                // turn to desired direction when the car is moving forward
                if (!checkEast(currentView, currPos) && carController.getSpeed() > 0 && !this.findWayOutReversing) {
                    carController.turnRight();
                } else if (!checkWest(currentView, currPos) && carController.getSpeed() > 0 && !this.findWayOutReversing) {
                    carController.turnLeft();
                }

                // stuck in the corner
                else {

                    carController.applyReverseAcceleration();

                    if (!checkWest(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        this.findWayOutReversing = false;
                        break;
                    }
                    // reverse and hit the wall
                    else if (checkSouth(currentView, currPos)) {
                        carController.applyBrake();
                        this.findWayOutReversing = false;
                        break;
                    }

                    this.findWayOutReversing = true;
                }
                break;

            case SOUTH:

                // turn to desired direction when the car is moving forward
                if (!checkWest(currentView, currPos) && carController.getSpeed() > 0 && !this.findWayOutReversing) {
                    carController.turnRight();
                } else if (!checkEast(currentView, currPos) && carController.getSpeed() > 0 && !this.findWayOutReversing) {
                    carController.turnLeft();
                }

                // stuck in the corner
                else {
                    carController.applyReverseAcceleration();

                    if (!checkEast(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        this.findWayOutReversing = false;
                        break;
                    }
                    // reverse and hit the wall
                    else if (checkNorth(currentView, currPos)) {
                        carController.applyBrake();
                        this.findWayOutReversing = false;
                        break;
                    }

                    this.findWayOutReversing = true;
                }

                break;

            case WEST:

                // turn to desired direction when the car is moving forward
                if (!checkNorth(currentView, currPos) && carController.getSpeed() > 0 && !this.findWayOutReversing) {
                    carController.turnRight();
                } else if (!checkSouth(currentView, currPos) && carController.getSpeed() > 0 && !this.findWayOutReversing) {
                    carController.turnLeft();
                }

                // stuck in the corner
                else {
                    carController.applyReverseAcceleration();

                    if (!checkSouth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        this.findWayOutReversing = false;
                        break;
                    }
                    // reverse and hit the wall
                    else if (checkEast(currentView, currPos)) {
                        carController.applyBrake();
                        this.findWayOutReversing = false;
                        break;
                    }

                    this.findWayOutReversing = true;
                }

                break;

            default:
                break;
        }
    }


    /**
     * Check if you have a wall in front of you!
     * @param orientation the orientation we are in based on WorldSpatial
     * @param currentView what the car can currently see
     * @param currPos
     * @return
     */
    private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
                                   String currPos){
        switch(orientation){
            case EAST:
                return checkEast(currentView, currPos);
            case NORTH:
                return checkNorth(currentView, currPos);
            case SOUTH:
                return checkSouth(currentView, currPos);
            case WEST:
                return checkWest(currentView, currPos);
            default:
                return false;
        }
    }

    /**
     * Check if the wall is on your right hand side given your orientation
     * @param orientation
     * @param currentView
     * @param currPos
     * @return boolean
     */
    private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView,
                                       String currPos) {

        switch(orientation){
            case EAST:
                return checkSouth(currentView, currPos);
            case NORTH:
                return checkEast(currentView, currPos);
            case SOUTH:
                return checkWest(currentView, currPos);
            case WEST:
                return checkNorth(currentView, currPos);
            default:
                return false;
        }
    }

    /**
     * Method below are used to check whether the particular tile is blocking the way of the car.
     * checkEast will check up the tile to the right.
     * checkEastSouth will check up the tile on the bottom right.
     * checkWest will check up the tile to the left.
     * checkWestNorth will check up the tile on top left.
     * checkNorth will check up the tile to the top.
     * checkNorthEast will check up tile on the top right.
     * checkSouth will check up tile below.
     * checkSouthWest will check tile on the bottom left.
     */
    private boolean checkEast(HashMap<Coordinate, MapTile> currentView, String currPos){
        // Check tiles to my right
        Coordinate currentPosition = new Coordinate(currPos);
        MapTile tile = currentView.get(new Coordinate(currentPosition.x+ oneStep, currentPosition.y));
        if (isBlocked(tile)) {
            return true;
        }

        return false;
    }

    private boolean checkEastSouth(HashMap<Coordinate, MapTile> currentView, String currPos) {
        // Check tiles to my bottom right
        Coordinate currentPosition = new Coordinate(currPos);
        MapTile tile = currentView.get(new Coordinate(currentPosition.x+ oneStep, currentPosition.y- oneStep));

        if (isBlocked(tile)) {
            return true;
        }

        return false;
    }

    private boolean checkWest(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles to my left
        Coordinate currentPosition = new Coordinate(currPos);
        MapTile tile = currentView.get(new Coordinate(currentPosition.x- oneStep, currentPosition.y));

        if (isBlocked(tile)) {
            return true;
        }

        return false;
    }

    private boolean checkWestNorth(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles to my top left
        Coordinate currentPosition = new Coordinate(currPos);
        MapTile tile = currentView.get(new Coordinate(currentPosition.x- oneStep, currentPosition.y+ oneStep));

        if (isBlocked(tile)) {
            return true;
        }

        return false;
    }

    private boolean checkNorth(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles to towards the top
        Coordinate currentPosition = new Coordinate(currPos);
        MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+ oneStep));
        if (isBlocked(tile)) {
            return true;
        }

        return false;
    }

    private boolean checkNorthEast(HashMap<Coordinate, MapTile> currentView, String currPos) {
        // Check tiles to towards the top right
        Coordinate currentPosition = new Coordinate(currPos);
        MapTile tile = currentView.get(new Coordinate(currentPosition.x+ oneStep, currentPosition.y+ oneStep));
        if (isBlocked(tile)) {
            return true;
        }

        return false;
    }

    private boolean checkSouth(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles towards the bottom
        Coordinate currentPosition = new Coordinate(currPos);
        MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y- oneStep));
        if (isBlocked(tile)) {
            return true;
        }

        return false;
    }

    private boolean checkSouthWest(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles towards the bottom left
        Coordinate currentPosition = new Coordinate(currPos);
        MapTile tile = currentView.get(new Coordinate(currentPosition.x- oneStep, currentPosition.y- oneStep));
        if (isBlocked(tile)) {
            return true;
        }

        return false;
    }

    private boolean isBlocked(MapTile tile) {
        String trapType = "";

        if(tile.isType(MapTile.Type.WALL)){
            return true;
        } else if (tile.isType(MapTile.Type.TRAP)) {
            trapType = getTrapType(tile);
        }

        for (String latchingTrapType: wallTrapTypes) {
            if (trapType.equals(latchingTrapType)) {
                return true;
            }
        }
        return false;
    }

    private String getTrapType(MapTile tile) {
        if (tile instanceof LavaTrap) {
            TrapTile lavaTrap = new LavaTrap();
            return lavaTrap.getTrap();
        } else if (tile instanceof WaterTrap) {
            TrapTile waterTrap = new WaterTrap();
            return waterTrap.getTrap();
        } else if (tile instanceof HealthTrap) {
            TrapTile healthTrap = new HealthTrap();
            return healthTrap.getTrap();
        }
        return "other";
    }

    /**
     * Gets priority remove index depends on their priority level
     * LAVA > HEALTH > WATER
     *
     * @return the priority remove index
     */
    private int getPriorityRemoveIndex() {
        final int WATER_REMOVE_PRIORITY_LEVEL = 1;
        final int HEALTH_REMOVE_PRIORITY_LEVEL = 2;
        final int LAVA_REMOVE_PRIORITY_LEVEL = 3;

        int currPos = -1;
        int currPriorityLevel = -1;

        for (int i = 0; i<wallTrapTypes.size(); i++) {
            if (wallTrapTypes.get(i).equals("lava")) {
                currPos = i;
                currPriorityLevel = LAVA_REMOVE_PRIORITY_LEVEL;
            } else if (wallTrapTypes.get(i).equals("health") && (currPriorityLevel < WATER_REMOVE_PRIORITY_LEVEL)) {
                currPos = i;
                currPriorityLevel = HEALTH_REMOVE_PRIORITY_LEVEL;
            } else if (wallTrapTypes.get(i).equals("water") && (currPriorityLevel < LAVA_REMOVE_PRIORITY_LEVEL)) {
                currPos = i;
                currPriorityLevel = WATER_REMOVE_PRIORITY_LEVEL;
            }
        }

        return currPos;
    }
}


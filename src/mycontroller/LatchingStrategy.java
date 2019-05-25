package mycontroller;

import tiles.*;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;

public class LatchingStrategy implements MoveStrategy {
    //TODO: complete function & starting direction arg
    private int wallSensitivity = 1;

    private ArrayList<String> latchingTrapTypes;

    public LatchingStrategy() {
        latchingTrapTypes = new ArrayList<>();
    }

    @Override
    public void move(MyAutoController carController) {
        WorldSpatial.Direction orientation = carController.getOrientation();
        HashMap<Coordinate, MapTile> currentView = carController.getView();
        String currPos = carController.getPosition();

        if (!checkFollowingWall(orientation, currentView, currPos)) {
            findWallToAttach(carController);
        }
        else {
            if (checkWallAhead(orientation, currentView, currPos)) {
                findWayOut(carController);
            }
        }
    }

    public void addTrap(String trapType) {
        latchingTrapTypes.add(trapType);
    }

    private void findWallToAttach(MyAutoController carController) {
        WorldSpatial.Direction orientation = carController.getOrientation();
        HashMap<Coordinate, MapTile> currentView = carController.getView();
        String currPos = carController.getPosition();

        switch(orientation) {
            case EAST:
                if (!checkWallAhead(orientation, currentView, currPos)) {
                    carController.turnRight();
                } else {
                    carController.turnLeft();
                }
                break;
            case NORTH:
                if (checkWallAhead(orientation, currentView, currPos)) {
                    carController.turnLeft();
                } else if (!checkEast(currentView, currPos)) {
                    carController.turnRight();
                }
                break;
            case SOUTH:
                if (checkWallAhead(orientation, currentView, currPos)) {
                    carController.turnLeft();
                } else if (!checkWest(currentView, currPos)) {
                    carController.turnRight();
                }
                break;
            case WEST:
                if (checkWallAhead(orientation, currentView, currPos)) {
                    carController.turnLeft();
                } else if (!checkNorth(currentView, currPos)) {
                    carController.turnRight();
                }
                break;
            default:
                break;
        }
    }

    // find path to get out when a wall is ahead
    private void findWayOut(MyAutoController carController) {
        WorldSpatial.Direction orientation = carController.getOrientation();
        HashMap<Coordinate, MapTile> currentView = carController.getView();
        String currPos = carController.getPosition();

        switch(orientation) {
            case EAST:
                if (!checkNorth(currentView, currPos)) {
                    carController.turnLeft();
                } else if (!checkSouth(currentView, currPos)) {
                    carController.turnRight();
                } else {
                    // stuck in the edge
                    carController.applyReverseAcceleration();
                    if (!checkSouth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        carController.applyForwardAcceleration();
                        carController.turnLeft();
                    } else if (!checkNorth(currentView, currPos)) {
                        carController.turnLeft();
                        carController.applyBrake();
                        carController.applyForwardAcceleration();
                        carController.turnRight();
                    }
                }
                break;

            case NORTH:
                if (!checkWest(currentView, currPos)) {
                    carController.turnLeft();
                } else if (!checkEast(currentView, currPos)) {
                    carController.turnRight();
                } else {
                    // stuck in the edge
                    carController.applyReverseAcceleration();
                    if (!checkWest(currentView, currPos)) {
                        carController.turnLeft();
                        carController.applyBrake();
                        carController.applyForwardAcceleration();
                        carController.turnRight();
                    } else if (!checkEast(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        carController.applyForwardAcceleration();
                        carController.turnLeft();
                    }
                }
                break;

            case SOUTH:
                if (!checkEast(currentView, currPos)) {
                    carController.turnLeft();
                } else if (!checkWest(currentView, currPos)) {
                    carController.turnRight();
                } else {
                    carController.applyReverseAcceleration();
                    if (!checkWest(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        carController.applyForwardAcceleration();
                        carController.turnLeft();
                    } else if (!checkEast(currentView, currPos)) {
                        carController.turnLeft();
                        carController.applyBrake();
                        carController.applyForwardAcceleration();
                        carController.turnRight();
                    }
                }
                break;

            case WEST:
                if (!checkSouth(currentView, currPos)) {
                    carController.turnLeft();
                } else if (!checkNorth(currentView, currPos)) {
                    carController.turnRight();
                } else {
                    carController.applyReverseAcceleration();
                    if (!checkSouth(currentView, currPos)) {
                        carController.turnLeft();
                        carController.applyBrake();
                        carController.applyForwardAcceleration();
                        carController.turnRight();
                    } else if (!checkNorth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        carController.applyForwardAcceleration();
                        carController.turnLeft();
                    }
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
     * @return
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
     * Method below just iterates through the list and check in the correct coordinates.
     * i.e. Given your current position is 10,10
     * checkEast will check up to wallSensitivity amount of tiles to the right.
     * checkWest will check up to wallSensitivity amount of tiles to the left.
     * checkNorth will check up to wallSensitivity amount of tiles to the top.
     * checkSouth will check up to wallSensitivity amount of tiles below.
     */
    private boolean checkEast(HashMap<Coordinate, MapTile> currentView, String currPos){
        // Check tiles to my right
        Coordinate currentPosition = new Coordinate(currPos);
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));

            if (isBlocked(tile)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkWest(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles to my left
        Coordinate currentPosition = new Coordinate(currPos);
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));

            if (isBlocked(tile)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNorth(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles to towards the top
        Coordinate currentPosition = new Coordinate(currPos);
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
            if (isBlocked(tile)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSouth(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles towards the bottom
        Coordinate currentPosition = new Coordinate(currPos);
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
            if (isBlocked(tile)) {
                return true;
            }
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

        for (String latchingTrapType: latchingTrapTypes) {
            if (trapType.equals(latchingTrapType)) {
                return true;
            }
        }
        return false;
    }

    private String getTrapType(MapTile tile) {
        if (tile instanceof LavaTrap) {
            return "lava";
        } else if (tile instanceof WaterTrap) {
            return "water";
        } else if (tile instanceof HealthTrap) {
            return "ice";
        }
        return "other";
    }
}

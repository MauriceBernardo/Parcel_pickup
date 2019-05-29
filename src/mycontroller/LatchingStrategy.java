package mycontroller;

import tiles.*;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;

public class LatchingStrategy implements ExploringMove {
    private int wallSensitivity = 1;
    private Coordinate initialCoordinate = null;
    private boolean reversing = false;
    private boolean completed = false;
    private ArrayList<String> wallTrapTypes;
    private boolean foundRoadFrontRight = false;
    private boolean foundWallBottomRight = false;

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
            if (!reversing) {
                carController.applyForwardAcceleration();
            }

            if (!checkFollowingWall(orientation, currentView, currPos)) {
                if (this.initialCoordinate == null) {
                    findInitialWallToAttach(carController);
                } else {
                    findWallToAttach(carController);
                }

            }
            else {
                if (checkWallAhead(orientation, currentView, currPos) || this.reversing) {
                    findWayOut(carController);
                }
            }

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

    private void findWallToAttach(MyAutoController carController) {
        WorldSpatial.Direction orientation = carController.getOrientation();
        HashMap<Coordinate, MapTile> currentView = carController.getView();
        String currPos = carController.getPosition();

        switch(orientation) {
            case EAST:
                if (!checkEastSouth(currentView, currPos) && !reversing) {
                    this.foundRoadFrontRight = true;
                }

                if (reversing) {
                     if (!checkNorth(currentView, currPos)) {
                         carController.turnRight();
                         carController.applyBrake();
                         reversing = false;
                     }

                } else if (!checkWest(currentView, currPos) && checkSouthWest(currentView, currPos)
                        && carController.getSpeed() == 0 && !this.foundWallBottomRight) {
                    carController.applyReverseAcceleration();
                    carController.applyBrake();
                    this.foundWallBottomRight = true;

                } else if (this.foundWallBottomRight) {
                    carController.turnRight();
                    this.foundWallBottomRight = false;

                } else if (checkWallAhead(orientation, currentView, currPos)) {

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
                else if (!checkSouth(currentView, currPos)) {
                    if (reversing) {
                        reversing = false;
                    }
                    carController.turnRight();
                }
                break;

            case NORTH:
                if (!checkNorthEast(currentView, currPos) && !reversing) {
                    this.foundRoadFrontRight = true;
                }

                if (reversing) {
                    if (!checkWest(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        reversing = false;
                    }

                } else if (!checkSouth(currentView, currPos) && checkEastSouth(currentView, currPos)
                        && carController.getSpeed() == 0 && !this.foundWallBottomRight) {
                    carController.applyReverseAcceleration();
                    carController.applyBrake();
                    this.foundWallBottomRight = true;

                } else if (this.foundWallBottomRight) {

                    carController.turnRight();

                    this.foundWallBottomRight = false;

                } else if (checkWallAhead(orientation, currentView, currPos)) {
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
                else if (!checkEast(currentView, currPos)) {
                    if (reversing) {
                        reversing = false;
                    }
                    carController.turnRight();
                }
                break;
            case SOUTH:
                if (!checkSouthWest(currentView, currPos) && !reversing) {
                    this.foundRoadFrontRight = true;
                }

                if (reversing) {
                    if (!checkEast(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        reversing = false;
                    }

                } else if (!checkNorth(currentView, currPos) && checkWestNorth(currentView, currPos)
                        && carController.getSpeed() == 0 && !this.foundWallBottomRight) {
                    carController.applyReverseAcceleration();
                    carController.applyBrake();
                    this.foundWallBottomRight = true;

                } else if (this.foundWallBottomRight) {
                    carController.turnRight();
                    this.foundWallBottomRight = false;

                } else if (checkWallAhead(orientation, currentView, currPos)) {
                    if (foundRoadFrontRight) {

                        carController.turnRight();

                        this.foundRoadFrontRight = false;

                    } else if (carController.getSpeed() != 0) {
                        carController.turnLeft();
                    } else {
                        carController.applyReverseAcceleration();
                        carController.applyBrake();
                    }
                } else if (!checkWest(currentView, currPos)) {
                    if (reversing) {
                        reversing = false;
                    }
                    carController.turnRight();
                }
                break;
            case WEST:
                if (!checkWestNorth(currentView, currPos) && !reversing) {
                    this.foundRoadFrontRight = true;
                }

                if (reversing) {
                    if (!checkSouth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        reversing = false;
                    }

                } else if (!checkEast(currentView, currPos) && checkNorthEast(currentView, currPos)
                        && carController.getSpeed() == 0 && !this.foundWallBottomRight) {
                    carController.applyReverseAcceleration();
                    carController.applyBrake();
                    this.foundWallBottomRight = true;

                } else if (this.foundWallBottomRight) {
                    carController.turnRight();

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
                } else if (!checkNorth(currentView, currPos)) {
                    if (reversing) {
                        reversing = false;
                    }
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
                if (!checkNorth(currentView, currPos) && carController.getSpeed() > 0 && !this.reversing) {
                    carController.turnLeft();
                } else if (!checkSouth(currentView, currPos) && carController.getSpeed() > 0 && !this.reversing) {
                    carController.turnRight();
                } else {
                    // stuck in the edge
                    carController.applyReverseAcceleration();
                    this.reversing = true;

                    if (!checkNorth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        this.reversing = false;

                    }
                }
                break;

            case NORTH:
                if (!checkWest(currentView, currPos) && carController.getSpeed() > 0 && !this.reversing) {
                    carController.turnLeft();
                } else if (!checkEast(currentView, currPos) && carController.getSpeed() > 0 && !this.reversing) {
                    carController.turnRight();
                } else {
                    // stuck in the edge
                    carController.applyReverseAcceleration();
                    this.reversing = true;

                    if (!checkWest(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        this.reversing = false;

                    }
                }
                break;

            case SOUTH:
                if (!checkEast(currentView, currPos) && carController.getSpeed() > 0 && !this.reversing) {
                    carController.turnLeft();
                } else if (!checkWest(currentView, currPos) && carController.getSpeed() > 0 && !this.reversing) {
                    carController.turnRight();
                } else {
                    carController.applyReverseAcceleration();
                    this.reversing = true;

                    if (!checkEast(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        this.reversing = false;
                    }
                }
                break;

            case WEST:
                if (!checkSouth(currentView, currPos) && carController.getSpeed() > 0 && !this.reversing) {
                    carController.turnLeft();
                } else if (!checkNorth(currentView, currPos) && carController.getSpeed() > 0 && !this.reversing) {
                    carController.turnRight();
                } else {
                    carController.applyReverseAcceleration();
                    this.reversing = true;

                    if (!checkSouth(currentView, currPos)) {
                        carController.turnRight();
                        carController.applyBrake();
                        this.reversing = false;

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



    private boolean checkEastSouth(HashMap<Coordinate, MapTile> currentView, String currPos) {
        // Check tiles to my right
        Coordinate currentPosition = new Coordinate(currPos);
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y-i));

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

    private boolean checkWestNorth(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles to my left
        Coordinate currentPosition = new Coordinate(currPos);
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y+i));

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

    private boolean checkNorthEast(HashMap<Coordinate, MapTile> currentView, String currPos) {
        // Check tiles to towards the top
        Coordinate currentPosition = new Coordinate(currPos);
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y+i));
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

    private boolean checkSouthWest(HashMap<Coordinate,MapTile> currentView, String currPos){
        // Check tiles towards the bottom
        Coordinate currentPosition = new Coordinate(currPos);
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y-i));
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

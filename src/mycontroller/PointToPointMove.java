package mycontroller;


import world.WorldSpatial;

public interface PointToPointMove extends MoveStrategy {
    int getHealthNeeded();
    WorldSpatial.Direction getEndOrientation();
}

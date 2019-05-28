package mycontroller;


import world.WorldSpatial;

public interface PointToPointMove extends MoveStrategy {
    // To get the orientation after executing the strategy
    WorldSpatial.Direction getEndOrientation();
}

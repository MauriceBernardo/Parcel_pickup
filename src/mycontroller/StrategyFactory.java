package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class StrategyFactory {
    private MoveStrategy pointToPointMove = null;
    private MoveStrategy exploringMove = null;

    private static StrategyFactory instance = null;

    public static StrategyFactory getInstance() {
        if (instance == null) {
            instance = new StrategyFactory();
        }
        return instance;
    }

    public enum ExploringMoveType {
        LATCHING
    }

    public enum PointToPointMoveType {
        WEIGHTED,
        WEIGHTED_MULTI_POINT,
        WEIGHTED_MULTI_POINT_BACKTRACK,
    }

    public MoveStrategy getExploringMove(ExploringMoveType type, ArrayList<String> wallTrapTypes) {
        if (exploringMove == null || exploringMove.completed()) {
            switch (type) {
                case LATCHING:
                    exploringMove = new LatchingStrategy(wallTrapTypes);
                    break;
                default:
                    ArrayList<String> empty = new ArrayList<>();
                    exploringMove = new LatchingStrategy(empty);
                    break;
            }
        }
        return exploringMove;
    }

    public MoveStrategy getPointToPointMove(PointToPointMoveType type, Coordinate source, LinkedList<Coordinate> destination,
                                            WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> localMap,
                                            HashMap<String, Integer> tileWeight) {
        if (pointToPointMove == null || pointToPointMove.completed()) {
            MultiPointStrategy multiPointStrategy = new MultiPointStrategy();
            Coordinate src = source;
            WorldSpatial.Direction direction = orientation;

            switch (type) {
                case WEIGHTED:
                    pointToPointMove = new WeightedRouteStrategy(source, destination.get(0), orientation, localMap, tileWeight, false);
                    break;
                case WEIGHTED_MULTI_POINT:
                    // Init the composite strategy
                    for(Coordinate coordinate : destination){
                        WeightedRouteStrategy next = new WeightedRouteStrategy(src, coordinate, direction, localMap, tileWeight, false);
                        multiPointStrategy.addStrategy(next);
                        src = coordinate;
                        direction = next.getEndOrientation();
                    }

                    pointToPointMove = multiPointStrategy;
                    break;
                case WEIGHTED_MULTI_POINT_BACKTRACK:
                    // Init the composite strategy
                    for(Coordinate coordinate : destination){
                        WeightedRouteStrategy next = new WeightedRouteStrategy(src, coordinate, direction, localMap, tileWeight, false);
                        WeightedRouteStrategy backtrack = new WeightedRouteStrategy(src, coordinate, direction, localMap, tileWeight, true);
                        multiPointStrategy.addStrategy(next);
                        multiPointStrategy.addBacktrackStrategy(backtrack);
                        src = coordinate;
                        direction = next.getEndOrientation();
                    }

                    pointToPointMove = multiPointStrategy;
                    break;
                default:
                    pointToPointMove = new WeightedRouteStrategy(source, destination.get(0), orientation, localMap, null, false);
                    break;
            }
        }
        return pointToPointMove;
    }
}

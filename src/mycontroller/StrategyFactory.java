package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class StrategyFactory {
    private static StrategyFactory instance = null;

    public static StrategyFactory getInstance() {
        if (instance == null) {
            instance = new StrategyFactory();
        }
        return instance;
    }

    public enum ExploringMoveType {
        LATCHING,
        DEFAULT
    }

    public enum PointToPointMoveType {
        WEIGHTED,
        WEIGHTED_MULTI_POINT,
        WEIGHTED_MULTI_POINT_BACKTRACK,
        DEFAULT
    }

    public MoveStrategy getExploringMove(ExploringMoveType type, ArrayList<String> wallTrapTypes) {
        switch (type) {
            case LATCHING:
                return new LatchingStrategy(wallTrapTypes);
            default:
                ArrayList<String> empty = new ArrayList<>();
                return new LatchingStrategy(empty);
        }

    }

    public MoveStrategy getPointToPointMove(PointToPointMoveType type, Coordinate source, LinkedList<Coordinate> destination,
                                            WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> localMap,
                                            HashMap<String, Integer> tileWeight) {
        MultiPointStrategy multiPointStrategy = new MultiPointStrategy();
        Coordinate src = source;
        WorldSpatial.Direction direction = orientation;

        switch (type) {
            case WEIGHTED:
                return new WeightedRouteStrategy(source, destination.get(0), orientation, localMap, tileWeight, false);

            case WEIGHTED_MULTI_POINT:
                // Init the composite strategy
                for (Coordinate coordinate : destination) {
                    WeightedRouteStrategy next = new WeightedRouteStrategy(src, coordinate, direction, localMap, tileWeight, false);
                    multiPointStrategy.addStrategy(next);
                    src = coordinate;
                    direction = next.getEndOrientation();
                }

                return multiPointStrategy;

            case WEIGHTED_MULTI_POINT_BACKTRACK:
                // Init the composite strategy
                for (Coordinate coordinate : destination) {
                    WeightedRouteStrategy next = new WeightedRouteStrategy(src, coordinate, direction, localMap, tileWeight, false);
                    WeightedRouteStrategy backtrack = new WeightedRouteStrategy(src, coordinate, direction, localMap, tileWeight, true);
                    multiPointStrategy.addStrategy(next);
                    multiPointStrategy.addBacktrackStrategy(backtrack);
                    src = coordinate;
                    direction = next.getEndOrientation();
                }

                return multiPointStrategy;

            default:
                return new WeightedRouteStrategy(source, destination.get(0), orientation, localMap, null, false);
        }
    }
}

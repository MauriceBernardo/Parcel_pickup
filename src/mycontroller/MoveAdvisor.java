package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public abstract class MoveAdvisor {
    private MoveStrategy moveStrategy;
    private ArrayList<String> wallTrapTypes = new ArrayList<>();
    private HashMap<String, Integer> tileWeight = new HashMap<>();

    public void update(MyAutoController carController) {
        moveStrategy.move(carController);
    }

    public MoveStrategy getMoveStrategy() {
        return moveStrategy;
    }

    public void setMoveStrategy(MoveStrategy moveStrategy) {
        this.moveStrategy = moveStrategy;
    }


    public ArrayList<String> getWallTrapTypes() {
        return wallTrapTypes;
    }

    public HashMap<String, Integer> getTileWeight() {
        return tileWeight;
    }

    // Tile to be considered as wall for exploringMove Strategy
    public void addWallTrapTypes(String tileType) {
        wallTrapTypes.add(tileType);
    }

    // Remove tile to be considered as wall for exploringMove Strategy
    public void removeWallTrapTypes(String tileType) {
        wallTrapTypes.remove(tileType);
    }

    // Tile weight to be considered for pointToPointMove Strategy
    public void addTileWeight(String key, Integer weight) {
        tileWeight.put(key, weight);
    }

    // Remove tile weight to be considered
    public void updateTileWeight(String key, Integer weight) {
        tileWeight.replace(key, weight);
    }

    public LinkedList<Coordinate> makeIceHealingDestination(MyAutoController carController, int amountToHeal) {
        // variable needed to make a new strategy
        StrategyFactory strategyFactory = StrategyFactory.getInstance();
        Coordinate currentLocation = new Coordinate(carController.getPosition());
        WorldSpatial.Direction orientation = carController.getOrientation();
        LinkedList<Coordinate> iceLocation = carController.getIceCoordinates();

        int minimumHealthNeeded = 100000;
        LinkedList<Coordinate> tempCoordinates = new LinkedList<>();
        LinkedList<Coordinate> bestCoordinates = new LinkedList<>();

        for (Coordinate coordinate : iceLocation) {
            tempCoordinates.clear();
            tempCoordinates.add(coordinate);

            MoveStrategy moveStrategy = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                    currentLocation, tempCoordinates, orientation, carController.getLocalMap(), getTileWeight());

            // No need to be considered if can't even go to the location
            if (!moveStrategy.completed()) {
                if (moveStrategy.getHealthNeeded() < minimumHealthNeeded) {
                    bestCoordinates.clear();
                    minimumHealthNeeded = moveStrategy.getHealthNeeded();
                    bestCoordinates.add(coordinate);
                }
            }
        }

        // add point to the ice point (brake) to specify how much to heal
        if (!bestCoordinates.isEmpty()) {
            Coordinate bestIceCoordinates = bestCoordinates.getFirst();
            for (int i = 0; i < Math.ceil(amountToHeal/2) + minimumHealthNeeded - 3; i++) {
                bestCoordinates.push(new Coordinate(bestIceCoordinates.toString()));
            }
        }

        return bestCoordinates;
    }

    public LinkedList<Coordinate> makeWaterHealingDestination(MyAutoController carController, int amountToHeal) {
        // variable needed to make a new strategy
        StrategyFactory strategyFactory = StrategyFactory.getInstance();
        Coordinate currentLocation = new Coordinate(carController.getPosition());
        WorldSpatial.Direction orientation = carController.getOrientation();
        LinkedList<Coordinate> waterLocation = carController.getWaterCoordinates();

        int numberOfCoordinateNeeded = (int) Math.ceil(amountToHeal / 5);
        int count = 0;
        LinkedList<Coordinate> bestCoordinates = new LinkedList<>();
        LinkedList<Coordinate> tempCoordinates = new LinkedList<>();

        while (bestCoordinates.size() != numberOfCoordinateNeeded) {
            if (waterLocation.size() <= count) {
                break;
            }

            tempCoordinates.add(waterLocation.get(count));
            MoveStrategy moveStrategy = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                    currentLocation, tempCoordinates, orientation, carController.getLocalMap(), getTileWeight());

            // No need to be considered if can't even go to the location
            if (!moveStrategy.completed()) {
                bestCoordinates.add(tempCoordinates.peekFirst());
            }
            tempCoordinates.clear();
            count++;
        }


        // Check whether there's gain on going to fetch the water
        MoveStrategy moveStrategy = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                currentLocation, bestCoordinates, orientation, carController.getLocalMap(), getTileWeight());

        // If there's no gain, no need to heal
        if (moveStrategy.getHealthNeeded() - amountToHeal >= 0) {
            bestCoordinates.clear();
        }

        return bestCoordinates;
    }

    public abstract void decideHealingStrategy(MyAutoController carController, int amountToHeal);
}

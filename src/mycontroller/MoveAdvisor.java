package mycontroller;

import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

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

    public abstract ArrayList<Coordinate> makeIceHealingDestination(MyAutoController carController, int amountToHeal);
    public abstract ArrayList<Coordinate> makeWaterHealingDestination(MyAutoController carController, int amountToHeal);
    public abstract void decideHealingStrategy();
}

package mycontroller;

import swen30006.driving.Simulation;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Advisor {
    private Simulation.StrategyMode optimization;
    private ArrayList<String> wallTrapTypes = new ArrayList<>();
    private HashMap<String, Integer> tileWeight = new HashMap<>();


    public void update(MyAutoController carController) {
    }

    public Simulation.StrategyMode getOptimization() {
        return optimization;
    }

    public ArrayList<String> getWallTrapTypes() {
        return wallTrapTypes;
    }

    public HashMap<String, Integer> getTileWeight() {
        return tileWeight;
    }

    public void setOptimization(Simulation.StrategyMode optimization) {
        this.optimization = optimization;
    }

    // Tile to be considered as wall for exploringMove Strategy
    public void addWallTrapTypes(String tileType) {
        wallTrapTypes.add(tileType);
    }

    public void removeWallTrapTypes(String tileType) {
        wallTrapTypes.remove(tileType);
    }

    // Tile weight to be considered for pointToPointMove Strategy
    public void addTileWeight(String key, Integer weight) {
        tileWeight.put(key, weight);
    }


}

package mycontroller;
import utilities.Coordinate;
import java.util.ArrayList;
import java.util.HashMap;

public class StrategyFactory {
    private static StrategyFactory instance = null;
    public static StrategyFactory getInstance(){
        if(instance == null){
            instance = new StrategyFactory();
        }
        return instance;
    }

    public enum ExploringMoveType{
        LATCHING
    }

    public enum PointToPointMoveType{
        WEIGHTED
    }

    public MoveStrategy getExploringMove(ExploringMoveType type, ArrayList<String> wallTrapTypes) {
        switch (type){
            case LATCHING:
                return new LatchingStrategy(wallTrapTypes);
            default:
                // Return wall only latching
                ArrayList<String> empty = new ArrayList<>();
                return new LatchingStrategy(empty);
        }
    }

    public MoveStrategy getPointToPointMove(PointToPointMoveType type, Coordinate destination, boolean backtrack, HashMap<String,Integer> tileWeight, MyAutoController carController){
        switch (type){
            case WEIGHTED:
                return new WeightedRouteStrategy(destination, backtrack, carController, tileWeight);
            default:
                return new WeightedRouteStrategy(destination, false, carController, null);
        }
    }
}

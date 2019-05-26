package mycontroller;

import java.util.ArrayList;

public class StrategyFactory {
    public static StrategyFactory instance = null;
    public static StrategyFactory getInstance(){
        if(instance == null){
            instance = new StrategyFactory();
        }
        return instance;
    }

    public MoveStrategy getMoveStrategy() {
        ArrayList<String> wallTrapTypes = new ArrayList<>();
        wallTrapTypes.add("lava");
        wallTrapTypes.add("health");
        wallTrapTypes.add("water");

        return new LatchingStrategy(wallTrapTypes);
    }
}

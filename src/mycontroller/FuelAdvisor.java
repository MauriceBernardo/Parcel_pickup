package mycontroller;

import swen30006.driving.Simulation;

public class FuelAdvisor extends Advisor {

    public FuelAdvisor() {
        setOptimization(Simulation.StrategyMode.FUEL);

        // set the tile weight for fuel optimization
        addTileWeight("water", 1);
        addTileWeight("health", 1);
        addTileWeight("lava", 1);
        addTileWeight("parcel", 2);
        addTileWeight("road", 1);

        // set the tile as wall for fuel optimization
        addWallTrapTypes("lava");
        addWallTrapTypes("health");
        addWallTrapTypes("water");
    }

    @Override
    public void update(MyAutoController carController) {

    }
}
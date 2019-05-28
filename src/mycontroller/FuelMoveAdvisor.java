package mycontroller;

import swen30006.driving.Simulation;
import utilities.Coordinate;

import java.util.ArrayList;

public class FuelMoveAdvisor extends MoveAdvisor {

    public FuelMoveAdvisor() {
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

    @Override
    public ArrayList<Coordinate> makeIceHealingDestination(MyAutoController carController, int amountToHeal) {
        return null;
    }

    @Override
    public ArrayList<Coordinate> makeWaterHealingDestination(MyAutoController carController, int amountToHeal) {
        return null;
    }

    @Override
    public void decideHealingStrategy() {

    }
}
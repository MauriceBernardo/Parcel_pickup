package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.LinkedList;

public class HealthMoveAdvisor extends MoveAdvisor {
    private Coordinate initialCoordinate;
    private boolean retrievingParcel = false;
    private boolean exitingMaze = false;
    private boolean changed = false;

    public HealthMoveAdvisor() {
        // set the tile weight for health optimization
        addTileWeight("water", 1000);
        addTileWeight("health", 10);
        addTileWeight("lava", 3);
        addTileWeight("parcel", 0);
        addTileWeight("road", 0);

        // set the tile as wall for health optimization
//        addWallTrapTypes("lava");
        addWallTrapTypes("health");
        addWallTrapTypes("water");

        StrategyFactory strategyFactory = StrategyFactory.getInstance();
        setMoveStrategy(strategyFactory.getExploringMove(StrategyFactory.ExploringMoveType.LATCHING, getWallTrapTypes()));
    }

    @Override
    public void update(MyAutoController carController) {
        Coordinate currentLocation = new Coordinate(carController.getPosition());
        WorldSpatial.Direction orientation = carController.getOrientation();
        LinkedList<Coordinate> finishCoordinates = carController.getFinishCoordinates();
        LinkedList<Coordinate> parcelCoordinates = carController.getParcelCoordinates();
        StrategyFactory strategyFactory = StrategyFactory.getInstance();

        // Check exit and number of parcel, if enough go to exit
        if (!finishCoordinates.isEmpty() && carController.numParcelsFound() >= carController.numParcels()) {
            if (retrievingParcel) {
                retrievingParcel = false;
                getMoveStrategy().forceCompleted();
            }
            exitingMaze = true;
            setMoveStrategy(strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED,
                    currentLocation, finishCoordinates, orientation, carController.getLocalMap(), getTileWeight()));
        }

        // Check parcel, take it if it's possible to take
        if (!parcelCoordinates.isEmpty() && !exitingMaze ){
            retrievingParcel = true;
            LinkedList<Coordinate> pathCoordinates = new LinkedList<>();

            // Set moveStrategy to move to first reachable parcel coordinate
            for (Coordinate coordinate : parcelCoordinates){
                pathCoordinates.clear();
                pathCoordinates.add(coordinate);
                setMoveStrategy(strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                        currentLocation, pathCoordinates, orientation, carController.getLocalMap(), getTileWeight()));
            }
        }

        // Check needed health for exit or retrieving parcel strategy
//        if (getMoveStrategy().getHealthNeeded() >= carController.getHealth()){
//            exitingMaze = false;
//            retrievingParcel = false;
//            getMoveStrategy().forceCompleted();
//
//        }

        // Check whether the health is in critical condition when doing exploring move
//        if (checkNeedHealing(carController) && getMoveStrategy() instanceof ExploringMove){
//
//        }

        // If any special move has done, move using exploring move again
        if(getMoveStrategy().completed()){
            setMoveStrategy(strategyFactory.getExploringMove(StrategyFactory.ExploringMoveType.LATCHING, getWallTrapTypes()));
        }

        if(getMoveStrategy() instanceof ExploringMove){
            System.out.println("Yeae");
        }

        // Move using the current strategy
        super.update(carController);
    }

    private boolean checkNeedHealing(MyAutoController carController) {
        return false;
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

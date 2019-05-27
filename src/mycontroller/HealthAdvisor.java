package mycontroller;

import swen30006.driving.Simulation;
import utilities.Coordinate;

import java.util.LinkedList;

public class HealthAdvisor extends Advisor {
    private MoveStrategy exploringMove;
    private MoveStrategy pointToPointMove;
    private boolean retrievingParcel = false;
    private boolean exitingMaze = false;
    private boolean changed = false;

    public HealthAdvisor() {
        setOptimization(Simulation.StrategyMode.HEALTH);

        // set the tile weight for health optimization
        addTileWeight("water", 1);
        addTileWeight("health", 1);
        addTileWeight("lava", 5);
        addTileWeight("parcel", 2);
        addTileWeight("road", 2);

        // set the tile as wall for health optimization
        addWallTrapTypes("lava");
        addWallTrapTypes("health");
        addWallTrapTypes("water");

        StrategyFactory strategyFactory = StrategyFactory.getInstance();
        this.exploringMove = strategyFactory.getExploringMove(StrategyFactory.ExploringMoveType.LATCHING, getWallTrapTypes());
    }

    @Override
    public void update(MyAutoController carController) {
        LinkedList<Coordinate> parcelCoordinates = carController.getParcelCoordinates();
        LinkedList<Coordinate> finishCoordinates = carController.getFinishCoordinates();

        if (exploringMove.completed()){
            StrategyFactory strategyFactory = StrategyFactory.getInstance();
            this.exploringMove = strategyFactory.getExploringMove(StrategyFactory.ExploringMoveType.LATCHING, getWallTrapTypes());
        }

        if (carController.numParcelsFound() >= carController.numParcels() && !finishCoordinates.isEmpty() && !exitingMaze){
            Coordinate finishCoordinate = finishCoordinates.remove();
            StrategyFactory strategyFactory = StrategyFactory.getInstance();
            this.pointToPointMove = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED,
                    finishCoordinate, false, getTileWeight(), carController);

            if(!pointToPointMove.completed()) {
                changed = true;
                exitingMaze = true;
                retrievingParcel = false;
            }
        }

        if (!parcelCoordinates.isEmpty() && !retrievingParcel && !exitingMaze) {
            Coordinate parcelCoordinate = parcelCoordinates.remove();
            StrategyFactory strategyFactory = StrategyFactory.getInstance();
            this.pointToPointMove = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED,
                    parcelCoordinate, true, getTileWeight(), carController);

            if(!pointToPointMove.completed()){
                changed = true;
                retrievingParcel = true;
            }
        }

        if(changed){
            changed = false;
            carController.applyBrake();
            return;
        }

        if (!retrievingParcel && !exitingMaze) {
            exploringMove.move(carController);
        } else {
            if(!pointToPointMove.completed()){
                pointToPointMove.move(carController);
            } else {
                changed = true;
                retrievingParcel = false;
            }
        }


    }
}

package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;
import java.util.LinkedList;


public class FuelMoveAdvisor extends MoveAdvisor {
    private Integer initialHealth = null;
    private boolean retrievingParcel = false;
    private boolean healed = false;
    private MoveStrategy exploringMove;
    private MoveStrategy pointToPointMove = null;

    public FuelMoveAdvisor() {
        // set the tile weight for health optimization
        addTileWeight("water", 0);
        addTileWeight("health", 0);
        addTileWeight("lava", 5);
        addTileWeight("parcel", 0);
        addTileWeight("road", 0);

        // no tile as wall for fuel optimization

        // Initialize moveStrategy with Latching Exploring Move
        StrategyFactory strategyFactory = StrategyFactory.getInstance();
        exploringMove = strategyFactory.getExploringMove(StrategyFactory.ExploringMoveType.LATCHING, getWallTrapTypes());
        setMoveStrategy(exploringMove);
    }

    @Override
    public void update(MyAutoController carController) {
        // Initialize all the information needed to make decision
        Coordinate currentLocation = new Coordinate(carController.getPosition());
        WorldSpatial.Direction orientation = carController.getOrientation();
        LinkedList<Coordinate> finishCoordinates = carController.getFinishCoordinates();
        LinkedList<Coordinate> parcelCoordinates = carController.getParcelCoordinates();
        StrategyFactory strategyFactory = StrategyFactory.getInstance();

        // Only ran when the first update happen, initialize the initial health and pointToPointMove to completed move
        if (initialHealth == null) {
            initialHealth = (int) carController.getHealth();

            // Set pointToPointMove to a completed move
            LinkedList<Coordinate> dummy = new LinkedList<>();
            dummy.add(currentLocation);
            pointToPointMove = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.DEFAULT,
                    currentLocation, dummy, orientation, carController.getLocalMap(), getTileWeight());
            pointToPointMove.forceCompleted();
        }



        // Check exit and number of parcel, if enough go to exit
        // Assumption there's no unreachable exit and all exit tile is grouped
        if (!finishCoordinates.isEmpty() && (carController.numParcelsFound() >= carController.numParcels())) {
            MoveStrategy exitStrategy = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED,
                    currentLocation, finishCoordinates, orientation, carController.getLocalMap(), getTileWeight());

            // If it's retrieving extra parcel and way out is seen, go to exit
            if (retrievingParcel && !exitStrategy.completed()) {
                retrievingParcel = false;
                getMoveStrategy().forceCompleted();
            }

            if(pointToPointMove.completed()){
                pointToPointMove = exitStrategy;
            }

            setMoveStrategy(pointToPointMove);
        }

        // Check parcel, take it the parcel needed is not enough
        if (!parcelCoordinates.isEmpty() &&  (carController.numParcelsFound() < carController.numParcels())) {
            LinkedList<Coordinate> pathCoordinates = new LinkedList<>();

            // Set moveStrategy to move to first reachable parcel coordinate
            for (Coordinate coordinate : parcelCoordinates) {
                pathCoordinates.clear();
                pathCoordinates.add(coordinate);

                if(pointToPointMove.completed()){
                    pointToPointMove = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                            currentLocation, pathCoordinates, orientation, carController.getLocalMap(), getTileWeight());
                    retrievingParcel = true;
                }
            }
            setMoveStrategy(pointToPointMove);
        }

        // if there's ice heal enormous amount of health and won't need to heal anymore
        if(!healed && !makeIceHealingDestination(carController,2000).isEmpty()){
            decideHealingStrategy(carController, 2000);
            healed = true;
        }


        // If any special move has done, move using exploring move again
        if (getMoveStrategy().completed()) {

            // Update all special move check to false
            retrievingParcel = false;

            // Make new exploring move if the exploring move has completed
            if(exploringMove.completed()){
                exploringMove = strategyFactory.getExploringMove(StrategyFactory.ExploringMoveType.LATCHING, getWallTrapTypes());
            }
            setMoveStrategy(exploringMove);
        }

        // Move using the current strategy
        super.update(carController);
    }

    @Override
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

    @Override
    public LinkedList<Coordinate> makeWaterHealingDestination(MyAutoController carController, int amountToHeal) {
        // no need to consider water healing for fuel optimization
        return new LinkedList<>();
    }

    @Override
    public void decideHealingStrategy(MyAutoController carController, int amountToHeal) {
        // Variable needed to be able to make a move strategy
        StrategyFactory strategyFactory = StrategyFactory.getInstance();
        LinkedList<Coordinate> iceCoordinate = makeIceHealingDestination(carController, amountToHeal);
        Coordinate currentLocation = new Coordinate(carController.getPosition());
        WorldSpatial.Direction orientation = carController.getOrientation();

        // heal using ice only
        pointToPointMove = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                currentLocation, iceCoordinate, orientation, carController.getLocalMap(), getTileWeight());


        // let the car continue its path if nothing can heal
        setMoveStrategy(pointToPointMove);
    }
}

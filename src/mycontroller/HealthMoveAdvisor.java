package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;
import java.util.LinkedList;

public class HealthMoveAdvisor extends MoveAdvisor {
    private Integer initialHealth = null;
    private boolean retrievingParcel = false;
    private boolean healing = false;
    private MoveStrategy exploringMove;
    private MoveStrategy pointToPointMove = null;

    private int healthNeeded = 0;
    private int healthChecked;

    public HealthMoveAdvisor() {
        // Healing move can be seen when the health is 250 for the hard map
        // more than that it will die since the algorithm doesn't really support advanced logic
        // With some healing this algorithm can survive in health 460, fuel 350 condition to collect 4 parcel

        // set the tile weight for health optimization
        addTileWeight("water", 996);
        addTileWeight("health", 10);
        addTileWeight("lava", 3);
        addTileWeight("parcel", 0);
        addTileWeight("road", 0);

        // set the tile as wall for health optimization
        addWallTrapTypes("health");
        addWallTrapTypes("water");

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
            healthChecked = initialHealth;

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
                healthNeeded = pointToPointMove.getHealthNeeded();
                healthChecked = (int) Math.floor(carController.getHealth());
            }

            setMoveStrategy(pointToPointMove);
        }

        // Check parcel, take it if it's possible to take
        if (!parcelCoordinates.isEmpty()) {
            LinkedList<Coordinate> pathCoordinates = new LinkedList<>();

            // Set moveStrategy to move to first reachable parcel coordinate
            for (Coordinate coordinate : parcelCoordinates) {
                pathCoordinates.clear();
                pathCoordinates.add(coordinate);

                if(pointToPointMove.completed()){
                    pointToPointMove = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                            currentLocation, pathCoordinates, orientation, carController.getLocalMap(), getTileWeight());
                    retrievingParcel = true;
                    healthNeeded = pointToPointMove.getHealthNeeded();
                    healthChecked = (int) Math.floor(carController.getHealth());
                }
            }
            setMoveStrategy(pointToPointMove);
        }

        // Check needed health for exit or retrieving parcel strategy (pointToPoint)
        if (healthNeeded >= healthChecked) {
            retrievingParcel = false;
            healing = true;

            // Heal 2 times more than the health needed to go to certain point
            int amountToHeal = (healthNeeded - healthChecked + 5) * 2;

            // Decide to do water or ice healing
            decideHealingStrategy(carController, amountToHeal);

            // reset the health needed
            healthNeeded = 0;
        }

        // If any special move has done, move using exploring move again
        if (getMoveStrategy().completed()) {

            // Update all special move check to false
            retrievingParcel = false;
            healing = false;

            // Make new exploring move if the exploring move has completed
            if(exploringMove.completed()){
                exploringMove = strategyFactory.getExploringMove(StrategyFactory.ExploringMoveType.LATCHING, getWallTrapTypes());
            }
            setMoveStrategy(exploringMove);
        }

        // Check whether the health is in critical condition when doing exploring move
        if (getMoveStrategy() instanceof ExploringMove && !healing) {
            if(checkNeedHealing(carController, initialHealth)) {
                healing = true;
                // heal until initial health
                int amountToHeal = (int) Math.floor(initialHealth - carController.getHealth());
                decideHealingStrategy(carController, amountToHeal);
            }
        }

        // Move using the current strategy
        super.update(carController);
    }

    // Check whether health is in critical condition based on health optimization (only consider ice)
    private boolean checkNeedHealing(MyAutoController carController, int initialHealth) {
        int amountToHeal = (int) Math.floor(initialHealth - carController.getHealth());

        // only consider when it reach below threshold
        if(carController.getHealth() > initialHealth*0.1){
            return false;
        }

        StrategyFactory strategyFactory = StrategyFactory.getInstance();
        Coordinate currentLocation = new Coordinate(carController.getPosition());
        WorldSpatial.Direction orientation = carController.getOrientation();
        LinkedList<Coordinate> iceCoordinate = makeIceHealingDestination(carController, amountToHeal);

        // Check whether the car can go to the ice or not
        if(!iceCoordinate.isEmpty()){
            MoveStrategy move = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED,
                    currentLocation, iceCoordinate, orientation, carController.getLocalMap(), getTileWeight());
            // Heal if it make the car health to be 5 - 10 when it needs heal
            return (int) Math.floor(carController.getHealth()) - move.getHealthNeeded() >= 5
                    && (int) Math.floor(carController.getHealth()) - move.getHealthNeeded() <= 10;
        }

        return false;
    }

    @Override
    public void decideHealingStrategy(MyAutoController carController, int amountToHeal) {
        // Allow access through water when deciding on healing move
        updateTileWeight("water", 3);

        // Variable needed to be able to make a move strategy
        StrategyFactory strategyFactory = StrategyFactory.getInstance();
        LinkedList<Coordinate> iceCoordinate = makeIceHealingDestination(carController, amountToHeal);
        LinkedList<Coordinate> waterCoordinate = makeWaterHealingDestination(carController, amountToHeal);
        Coordinate currentLocation = new Coordinate(carController.getPosition());
        WorldSpatial.Direction orientation = carController.getOrientation();

        // heal using the possible healing,
        if (iceCoordinate.isEmpty()) {
            pointToPointMove = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                    currentLocation, waterCoordinate, orientation, carController.getLocalMap(), getTileWeight());

        } else if (waterCoordinate.isEmpty()) {
            pointToPointMove = strategyFactory.getPointToPointMove(StrategyFactory.PointToPointMoveType.WEIGHTED_MULTI_POINT_BACKTRACK,
                    currentLocation, iceCoordinate, orientation, carController.getLocalMap(), getTileWeight());
        }

        // let the car continue its path if nothing can heal
        setMoveStrategy(pointToPointMove);

        // Limited access through water when healing strategy has been made
        updateTileWeight("water", 996);
    }
}

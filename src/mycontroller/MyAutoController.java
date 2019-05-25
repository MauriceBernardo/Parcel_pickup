package mycontroller;

import controller.CarController;
import world.Car;
import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

public class MyAutoController extends CarController {
    // How many minimum units the wall is away from the player.

    private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.

    // Car Speed to move at
    private final int CAR_MAX_SPEED = 1;

    public MyAutoController(Car car) {
        super(car);
    }

    // Coordinate initialGuess;
    // boolean notSouth = true;
    @Override
    public void update() {
        // Gets what the car can see
        HashMap<Coordinate, MapTile> currentView = getView();

        // checkStateChange();
        if (getSpeed() < CAR_MAX_SPEED) {       // Need speed to turn and progress toward the exit
            applyForwardAcceleration();   // Tough luck if there's a wall in the way
        }

        MoveStrategy moveStrategy = StrategyFactory.getInstance().getMoveStrategy();

        moveStrategy.move(this);

    }

}

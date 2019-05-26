package mycontroller;

import controller.CarController;
import world.Car;
import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

public class MyAutoController extends CarController {
    public MyAutoController(Car car) {
        super(car);
    }

    // TODO: delete later
    private MoveStrategy moveStrategy = StrategyFactory.getInstance().getMoveStrategy();

    @Override
    public void update() {

        moveStrategy.move(this);

    }


}

package mycontroller;

import controller.CarController;
import swen30006.driving.Simulation;
import tiles.TrapTile;
import world.Car;
import java.util.HashMap;
import tiles.MapTile;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

public class MyAutoController extends CarController {
    // Car explored map
    private HashMap<Coordinate, MapTile> localMap = new HashMap<>();

    // TODO: delete later
    private MoveStrategy moveStrategy = StrategyFactory.getInstance().getMoveStrategy();
  
    public MyAutoController(Car car) {
        super(car);
        if (Simulation.toConserve() == Simulation.StrategyMode.FUEL) {
        }

        // Initialisation of localMap
        int maxWidth = mapWidth();
        int maxHeight = mapHeight();
        for (int y = 0; y <= maxHeight; y++) {
            for (int x = 0; x <= maxWidth; x++) {
                Coordinate coordinate = new Coordinate(x, y);
                MapTile unexploredTile = new MapTile(MapTile.Type.EMPTY);
                localMap.put(coordinate, unexploredTile);
            }
        }

    }

    @Override
    public void update() {
        // Gets what the car can see
        HashMap<Coordinate, MapTile> currentView = getView();
        // Update the local map based on what the car can see
        updateLocalMap(currentView);

        // For logging purposes
        Coordinate carPosition = new Coordinate(getPosition());
        System.out.println(carPosition);
        for (int y = carPosition.y + 4; y >= carPosition.y - 4; y--) {
            for (int x = carPosition.x - 4; x <= carPosition.x + 4; x++) {
                if (x == carPosition.x && y == carPosition.y) {
                    System.out.printf("%6s", "here");
                    System.out.print("|");
                    continue;
                }

                if (currentView.get(new Coordinate(x, y)).getType() == MapTile.Type.TRAP) {
                    TrapTile test = (TrapTile) currentView.get(new Coordinate(x, y));
                    System.out.printf("%6s", test.getTrap());
                } else {
                    System.out.printf("%6s", currentView.get(new Coordinate(x, y)).getType());
                }
                System.out.print("|");
            }
            System.out.println();
        }
        // alvin's (to delete later)
        moveStrategy.move(this);
    }

    private void updateLocalMap(HashMap<Coordinate, MapTile> currentView) {
        Coordinate carPosition = new Coordinate(getPosition());
        for (int y = carPosition.y + 4; y >= carPosition.y - 4; y--) {
            for (int x = carPosition.x - 4; x <= carPosition.x + 4; x++) {
                Coordinate viewCoordinate = new Coordinate(x, y);
                this.localMap.put(viewCoordinate, currentView.get(viewCoordinate));
            }
        }
    }

    public HashMap<Coordinate, MapTile> getLocalMap() {
        return localMap;
    }
}

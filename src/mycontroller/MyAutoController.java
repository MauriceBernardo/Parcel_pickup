package mycontroller;

import controller.CarController;
import swen30006.driving.Simulation;
import tiles.*;
import world.Car;

import java.util.HashMap;
import java.util.LinkedList;

import utilities.Coordinate;

public class MyAutoController extends CarController {
    // Car explored map
    private HashMap<Coordinate, MapTile> localMap = new HashMap<>();

    // MoveAdvisor to move depending on the strategy
    private MoveAdvisor moveAdvisor;

    public MyAutoController(Car car) {
        super(car);
        if (Simulation.toConserve() == Simulation.StrategyMode.FUEL) {
            this.moveAdvisor = new FuelMoveAdvisor();
        } else if (Simulation.toConserve() == Simulation.StrategyMode.HEALTH) {
            this.moveAdvisor = new HealthMoveAdvisor();
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

        // Move depending on the optimization
        moveAdvisor.update(this);
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

    public LinkedList<Coordinate> getParcelCoordinates() {
        LinkedList<Coordinate> parcelCoordinates = new LinkedList<>();
        for (Coordinate coordinate : localMap.keySet()) {
            if (localMap.get(coordinate) instanceof ParcelTrap) {
                parcelCoordinates.add(coordinate);
            }
        }

        return parcelCoordinates;
    }

    public LinkedList<Coordinate> getFinishCoordinates() {
        LinkedList<Coordinate> finishCoordinates = new LinkedList<>();
        for (Coordinate coordinate : localMap.keySet()) {
            if (localMap.get(coordinate).getType() == MapTile.Type.FINISH) {
                finishCoordinates.add(coordinate);
            }
        }

        return finishCoordinates;
    }

    public LinkedList<Coordinate> getIceCoordinates() {
        LinkedList<Coordinate> iceCoordinates = new LinkedList<>();
        for (Coordinate coordinate : localMap.keySet()) {
            if (localMap.get(coordinate) instanceof HealthTrap) {
                iceCoordinates.add(coordinate);
            }
        }

        return iceCoordinates;
    }

    public LinkedList<Coordinate> getWaterCoordinates() {
        LinkedList<Coordinate> waterCoordinates = new LinkedList<>();
        for (Coordinate coordinate : localMap.keySet()) {
            if (localMap.get(coordinate) instanceof WaterTrap) {
                waterCoordinates.add(coordinate);
            }
        }

        return waterCoordinates;
    }

    public HashMap<Coordinate, MapTile> getLocalMap() {
        return localMap;
    }
}

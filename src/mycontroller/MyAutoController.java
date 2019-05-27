package mycontroller;

import controller.CarController;
import swen30006.driving.Simulation;
import tiles.TrapTile;
import world.Car;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import tiles.MapTile;
import utilities.Coordinate;

public class MyAutoController extends CarController {
    // Car explored map
    private HashMap<Coordinate, MapTile> localMap = new HashMap<>();

    // Advisor to move depending on the strategy
	private Advisor moveAdvisor;

    public MyAutoController(Car car) {
        super(car);
        if (Simulation.toConserve() == Simulation.StrategyMode.FUEL) {
        	this.moveAdvisor = new FuelAdvisor();
        } else if (Simulation.toConserve() == Simulation.StrategyMode.HEALTH){
        	this.moveAdvisor = new HealthAdvisor();
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

    public LinkedList<Coordinate> getParcelCoordinates(){
        LinkedList<Coordinate> parcelCoordinates = new LinkedList<>();
        for (Coordinate coordinate : localMap.keySet()){
            if(localMap.get(coordinate).getType() == MapTile.Type.TRAP){
                TrapTile trapTile = (TrapTile) localMap.get(coordinate);
                if(trapTile.getTrap().equals("parcel")){
                    parcelCoordinates.add(coordinate);
                }
            }
        }

        return parcelCoordinates;
    }

    public LinkedList<Coordinate> getFinishCoordinates(){
        LinkedList<Coordinate> finishCoordinates = new LinkedList<>();
        for (Coordinate coordinate : localMap.keySet()){
            if(localMap.get(coordinate).getType() == MapTile.Type.FINISH){
                finishCoordinates.add(coordinate);
            }
        }

        return finishCoordinates;
    }

    public HashMap<Coordinate, MapTile> getLocalMap() {
        return localMap;
    }
}

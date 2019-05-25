package mycontroller;

import com.badlogic.gdx.Input;
import controller.CarController;
import swen30006.driving.Simulation;
import tiles.TrapTile;
import world.Car;
import java.util.HashMap;
import java.util.Set;

import tiles.MapTile;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

public class MyAutoController extends CarController{		
		// How many minimum units the wall is away from the player.
		private int wallSensitivity = 1;

		// This is set to true when the car starts sticking to a wall.
		private boolean isFollowingWall = false;
		
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
			System.out.println(getPosition());
			Coordinate carPosition = new Coordinate(getPosition());

			for(int y = carPosition.y + 4; y >= carPosition.y-4; y--){
				for(int x = carPosition.x - 4; x <= carPosition.x+4; x++){
					if(x == carPosition.x && y == carPosition.y){
						System.out.printf("%6s","here");
						System.out.print("|");
						continue;
					}

					if(currentView.get(new Coordinate(x,y)).getType() == MapTile.Type.TRAP){
						TrapTile test = (TrapTile) currentView.get(new Coordinate(x,y));
						System.out.printf("%6s",test.getTrap());
					} else {
						System.out.printf("%6s", currentView.get(new Coordinate(x, y)).getType());
					}
					System.out.print("|");
				}
				System.out.println();
			}

			Set<Integer> parcels = Simulation.getParcels();
			Simulation.resetParcels();
			for (int k : parcels){
				switch (k){
					case Input.Keys.B:
						applyBrake();
						break;
					case Input.Keys.UP:
						applyForwardAcceleration();
						break;
					case Input.Keys.DOWN:
						applyReverseAcceleration();
						break;
					case Input.Keys.LEFT:
						turnLeft();
						break;
					case Input.Keys.RIGHT:
						turnRight();
						break;
					default:
				}
			}


		}
	}

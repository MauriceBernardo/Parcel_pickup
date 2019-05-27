package mycontroller;

import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.*;

public class WeightedRouteStrategy extends PointToPointMove {
    private static final int UNREACHABLE = 1000;

    private boolean initialized = false;

    private HashMap<Coordinate, Point> weightMap = new HashMap<>();
    private HashMap<String, Integer> tileWeight;

    public WeightedRouteStrategy(Coordinate destination, boolean backtrack, MyAutoController carController, HashMap<String, Integer> tileWeight) {
        super(destination, backtrack, carController);
        if(tileWeight != null){
            this.tileWeight = tileWeight;
        } else {
            // Put default weight for the algorithm
            this.tileWeight = new HashMap<>();
            this.tileWeight.put("lava", 5);
            this.tileWeight.put("health", 1);
            this.tileWeight.put("water", 1);
            this.tileWeight.put("road", 2);
        }

        calculateLocalMapWeight(getLocalMap(), getSource(), getInitialOrientation());
        if (getShortestPathCoordinates()) {
            translateToMoveCommand(carController.getOrientation());
            initialized = true;
        } else {
            setCompleted();
        }

    }

    @Override
    public void move(MyAutoController carController) {
        if (initialized) {
            applyCommand(carController);
        }
    }

    // Get the shortest path coordinates base on the Dijkstra
    private boolean getShortestPathCoordinates() {
        Point nextPoint = weightMap.get(this.getDestination());
        while (nextPoint.source != Point.Source.ORIGIN) {
            this.getPathCoordinate().push(nextPoint.coordinate);
            switch (nextPoint.source) {
                case UP:
                    nextPoint = weightMap.get(new Coordinate(nextPoint.coordinate.getUpCoordinate()));
                    break;
                case DOWN:
                    nextPoint = weightMap.get(new Coordinate(nextPoint.coordinate.getDownCoordinate()));
                    break;
                case LEFT:
                    nextPoint = weightMap.get(new Coordinate(nextPoint.coordinate.getLeftCoordinate()));
                    break;
                case RIGHT:
                    nextPoint = weightMap.get(new Coordinate(nextPoint.coordinate.getRightCoordinate()));
                    break;
                case IMPASSABLE:
                    return false;
            }
        }

        this.getPathCoordinate().push(nextPoint.coordinate);
        return true;
    }

    // Dijkstra's algorithm
    private void calculateLocalMapWeight(HashMap<Coordinate, MapTile> localMap, Coordinate initialCoordinate, WorldSpatial.Direction carOrientation) {
        Point originPoint = new Point(0, Point.Source.ORIGIN, initialCoordinate);
        ArrayList<Point> queue = new ArrayList<>();
        queue.add(originPoint);

        // Initialize all point as unreachable
        for (Coordinate coordinate : localMap.keySet()) {
            Point otherPoint = new Point(UNREACHABLE, Point.Source.IMPASSABLE, coordinate);
            if (!originPoint.coordinate.equals(otherPoint.coordinate)) {
                queue.add(otherPoint);
            }
        }

        // Sort based on weight
        queue.sort(Comparator.comparingInt(o -> o.weight));

        while (weightMap.keySet().size() != localMap.keySet().size()) {
            Point nextPoint = queue.remove(0);
            weightMap.put(nextPoint.coordinate, nextPoint);

            // get 4 direction of a point
            Coordinate eastCoordinate = new Coordinate(nextPoint.coordinate.getRightCoordinate());
            Coordinate westCoordinate = new Coordinate(nextPoint.coordinate.getLeftCoordinate());
            Coordinate northCoordinate = new Coordinate(nextPoint.coordinate.getUpCoordinate());
            Coordinate southCoordinate = new Coordinate(nextPoint.coordinate.getDownCoordinate());

            // initialize all distance as unreachable
            int northDist = UNREACHABLE;
            int southDist = UNREACHABLE;
            int eastDist = UNREACHABLE;
            int westDist = UNREACHABLE;


            // Calculate the distance of each coordinates
            if (nextPoint.source == Point.Source.ORIGIN) {
                // Check depend on orientation

                // Horizontal checking
                if (carOrientation == WorldSpatial.Direction.EAST || carOrientation == WorldSpatial.Direction.WEST) {
                    // East checking
                    eastDist = calculateDist(localMap.get(eastCoordinate));

                    // West checking
                    westDist = calculateDist(localMap.get(westCoordinate));
                }
                // Vertical checking
                else {
                    // North checking
                    northDist = calculateDist(localMap.get(northCoordinate));

                    // South checking
                    southDist = calculateDist(localMap.get(southCoordinate));
                }
            } else {
                // East checking
                eastDist = calculateDist(localMap.get(eastCoordinate));

                // West checking
                westDist = calculateDist(localMap.get(westCoordinate));

                // North checking
                northDist = calculateDist(localMap.get(northCoordinate));

                // South checking
                southDist = calculateDist(localMap.get(southCoordinate));
            }

            // update weight and source
            for (Point target : queue) {
                if (target.coordinate.equals(eastCoordinate)) {
                    if (target.weight > nextPoint.weight + eastDist) {
                        target.weight = nextPoint.weight + eastDist;
                        target.source = Point.Source.LEFT;
                    }
                } else if (target.coordinate.equals(westCoordinate)) {
                    if (target.weight > nextPoint.weight + westDist) {
                        target.weight = nextPoint.weight + westDist;
                        target.source = Point.Source.RIGHT;
                    }
                } else if (target.coordinate.equals(northCoordinate)) {
                    if (target.weight > nextPoint.weight + northDist) {
                        target.weight = nextPoint.weight + northDist;
                        target.source = Point.Source.DOWN;
                    }
                } else if (target.coordinate.equals(southCoordinate)) {
                    if (target.weight > nextPoint.weight + southDist) {
                        target.weight = nextPoint.weight + southDist;
                        target.source = Point.Source.UP;
                    }
                }
            }

            // Sort based on weight
            queue.sort(Comparator.comparingInt(o -> o.weight));
        }
    }

    private int calculateDist(MapTile destination) {
        if (destination == null) {
            return UNREACHABLE;
        }

        switch (destination.getType()) {
            case EMPTY:
            case WALL:
                return UNREACHABLE;
            case TRAP:
                return tileWeight.get(((TrapTile) destination).getTrap());
            default:
                return tileWeight.get("road");
        }
    }

}

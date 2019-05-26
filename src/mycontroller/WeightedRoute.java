package mycontroller;

import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.WorldSpatial;
import java.util.*;

public class WeightedRoute extends PointToPoint {
    private static final int UNREACHABLE = 1000;

    private boolean initialized = false;

    private HashMap<Coordinate, Point> weightMap = new HashMap<>();

    public WeightedRoute(Coordinate destination, boolean backtrack) {
        super(destination, backtrack);
    }

    public WeightedRoute(int destX, int destY, boolean backtrack) {
        super(destX, destY, backtrack);
    }

    @Override
    public void move(MyAutoController carController) {
        if (!initialized) {
            calculateLocalMapWeight(carController.getLocalMap(), carController.getPosition(), carController.getOrientation());
            if (getShortestPathCoordinates()) {
                translateToMoveCommand(carController.getOrientation());
                initialized = true;
            } else {
                setCompleted();
                System.out.println("YO MAN CAN'T MOVE");
            }
        }

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
    private void calculateLocalMapWeight(HashMap<Coordinate, MapTile> localMap, String carPosition, WorldSpatial.Direction carOrientation) {
        Coordinate initialCoordinate = new Coordinate(carPosition);
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

    // TODO: can change depending optimization
    private int calculateDist(MapTile destination) {
        if (destination == null) {
            return UNREACHABLE;
        }

        switch (destination.getType()) {
            case WALL:
                return UNREACHABLE;
            case TRAP:
                if (((TrapTile) destination).getTrap().equals("lava")) {
                    return 5;
                } else if (((TrapTile) destination).getTrap().equals("water")) {
                    return 1;
                }
            case EMPTY:
                return UNREACHABLE;
            default:
                return 2;
        }
    }

}

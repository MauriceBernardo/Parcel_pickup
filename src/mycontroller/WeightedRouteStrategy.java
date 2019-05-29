package mycontroller;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import utilities.PointToPoint;
import world.WorldSpatial;

import java.util.*;

public class WeightedRouteStrategy implements PointToPointMove {
    private static final int UNREACHABLE = 1000;

    // Variable to see whether it's completed or not
    private boolean completed = false;

    // Guard for calling move although it can't move
    private boolean initialized = false;

    // Variable needed to run the algorithm
    private Coordinate destination;
    private HashMap<Coordinate, Point> weightMap = new HashMap<>();
    private HashMap<String, Integer> tileWeight;
    private LinkedList<Coordinate> pathCoordinate = new LinkedList<>();
    private PointToPoint pointToPointAdvisor;
    private HashMap<Coordinate, MapTile> localMap;
    private WorldSpatial.Direction initialOrientation;
    private boolean backtrack;

    private static class Point {
        public int weight;
        public Source source;
        public Coordinate coordinate;

        public enum Source {
            DOWN,
            LEFT,
            ORIGIN,
            RIGHT,
            UP,
            IMPASSABLE
        }

        public Point(int weight, Source source, Coordinate coordinate) {
            this.weight = weight;
            this.source = source;
            this.coordinate = coordinate;
        }
    }

    public WeightedRouteStrategy(Coordinate source, Coordinate destination, WorldSpatial.Direction orientation,
                                 HashMap<Coordinate, MapTile> localMap, HashMap<String, Integer> tileWeight, boolean backtrack) {
        if (tileWeight != null) {
            this.tileWeight = tileWeight;
        } else {
            // Put default weight for the algorithm
            this.tileWeight = new HashMap<>();
            this.tileWeight.put("lava", 5);
            this.tileWeight.put("health", 0);
            this.tileWeight.put("water", 0);
            this.tileWeight.put("road", 0);
        }

        this.pointToPointAdvisor = new PointToPoint();
        this.initialOrientation = orientation;
        this.destination = destination;
        this.localMap = localMap;
        this.backtrack = backtrack;

        calculateLocalMapWeight(localMap, source, orientation);
        if (getShortestPathCoordinates()) {
            this.pointToPointAdvisor.translateToMoveCommand(orientation, pathCoordinate);
            if(backtrack){
                this.pointToPointAdvisor.considerReverseCommand();
            }
            initialized = true;
        } else {
            setCompleted();
        }

    }

    @Override
    public void move(MyAutoController carController) {
        if (initialized) {
            if(backtrack){
                if(!pointToPointAdvisor.applyReverseCommand(carController)){
                    setCompleted();
                }
            } else {
                if (!pointToPointAdvisor.applyMoveCommand(carController)) {
                    setCompleted();
                }
            }
        }
    }

    @Override
    public boolean completed() {
        return completed;
    }

    @Override
    public void forceCompleted() {
        setCompleted();
    }

    @Override
    public int getHealthNeeded() {
        // No health needed if can't even go to the point
        if(!initialized){
            return 0;
        }

        int healthNeeded = 0;

        MapTile endTile = localMap.get(pathCoordinate.peekLast());
        if (endTile instanceof LavaTrap) {
            healthNeeded += 5;
        }

        for (Coordinate coordinate : pathCoordinate) {
            MapTile passedTile = localMap.get(coordinate);
            if (passedTile instanceof LavaTrap) {
                healthNeeded += 5;
            }
        }
        return healthNeeded;
    }

    @Override
    public WorldSpatial.Direction getEndOrientation() {
        if (!initialized || pathCoordinate.size() == 1) {
            return initialOrientation;
        }

        WorldSpatial.Direction orientation = WorldSpatial.Direction.EAST;
        Coordinate lastCoordinate = this.pathCoordinate.peekLast();

        // Check whether it's reversing to go to the point or forward
        boolean forward = true;
        PointToPoint.Command command = pointToPointAdvisor.peekFirstMoveCommand();
        if (command == PointToPoint.Command.REVERSE) {
            forward = false;
        }

        Point lastPoint = weightMap.get(lastCoordinate);


        switch (lastPoint.source) {
            case LEFT:
                orientation = WorldSpatial.Direction.EAST;
                break;
            case RIGHT:
                orientation = WorldSpatial.Direction.WEST;
                break;
            case DOWN:
                orientation = WorldSpatial.Direction.NORTH;
                break;
            case UP:
                orientation = WorldSpatial.Direction.SOUTH;
                break;
        }

        if (!forward) {
            return WorldSpatial.reverseDirection(orientation);
        }
        return orientation;
    }

    // Get the shortest path coordinates base on the Dijkstra
    private boolean getShortestPathCoordinates() {
        Point nextPoint = weightMap.get(destination);
        while (nextPoint.source != Point.Source.ORIGIN) {
            pathCoordinate.push(nextPoint.coordinate);
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

        pathCoordinate.push(nextPoint.coordinate);
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

    // Calculate the distance depending on the tile weight
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


    private void setCompleted() {
        this.completed = true;
    }
}

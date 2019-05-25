package mycontroller;

public class StrategyFactory {
    public static StrategyFactory instance = null;
    public static StrategyFactory getInstance(){
        if(instance == null){
            instance = new StrategyFactory();
        }
        return instance;
    }

    public MoveStrategy getMoveStrategy() {
        return new LatchingStrategy();
    }
}

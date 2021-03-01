package tw.momocraft.entityplus.handlers;

public class UtilsHandler {

    private static DependHandler dependence;

    public static void setup() {
        dependence = new DependHandler();
    }

    public static DependHandler getDepend() {
        return dependence;
    }

}

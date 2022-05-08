package tw.momocraft.entityplus.handlers;

public class UtilsHandler {

    private static DependHandler dependence;
    private static ScheduleHandler schedule;

    public static void setUpFirst(boolean reload) {
        dependence = new DependHandler();
        dependence.setup(reload);
    }

    public static void setUpLast(boolean reload) {
        schedule = new ScheduleHandler();
    }

    public static DependHandler getDepend() {
        return dependence;
    }

    public static ScheduleHandler getSchedule() {
        return schedule;
    }
}

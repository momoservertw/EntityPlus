package tw.momocraft.entityplus.handlers;

public class UtilsHandler {

    private static DependHandler dependence;
    private static ScheduleHandler schedule;

    public static void setUpFirst() {
        dependence = new DependHandler();
    }

    public static void setUpLast() {
        schedule = new ScheduleHandler();
    }

    public static DependHandler getDepend() {
        return dependence;
    }

    public static ScheduleHandler getSchedule() {
        return schedule;
    }
}

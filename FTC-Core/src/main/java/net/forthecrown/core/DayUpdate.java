package net.forthecrown.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class DayUpdate {
    private final List<Runnable> listeners = new ArrayList<>();

    //Means this thing thinks the day changed right before the server's restart.
    private final TimeZone updateTimeZone = TimeZone.getTimeZone("GMT+06:00"); //TimeZone of central Kazakhstan lmao

    private byte day;

    DayUpdate(byte day) {
        this.day = day;
        Crown.logger().info("DayUpdate loaded");
    }

    public void checkDay(){
        Calendar calendar = Calendar.getInstance(updateTimeZone);
        if(calendar.get(Calendar.DAY_OF_WEEK) != getDay()) update();
    }

    public void update(){
        Crown.logger().info("Updating date");
        setDay((byte) Calendar.getInstance(updateTimeZone).get(Calendar.DAY_OF_WEEK));

        listeners.forEach(r -> {
            try {
                r.run();
            } catch (Exception e){
                Crown.logger().severe("Could not update date of " + r.getClass().getSimpleName());
                e.printStackTrace();
            }
        });
    }

    public void addListener(Runnable runnable){
        listeners.add(runnable);
    }

    public byte getDay() {
        return day;
    }

    public TimeZone getUpdateTimeZone() {
        return updateTimeZone;
    }

    public void setDay(byte day) {
        this.day = day;
        Crown.config().set("Day", day);
    }

    public List<Runnable> getListeners() {
        return listeners;
    }
}

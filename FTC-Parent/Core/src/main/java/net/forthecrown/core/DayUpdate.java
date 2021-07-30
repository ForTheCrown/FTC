package net.forthecrown.core;

import net.forthecrown.utils.FtcUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DayUpdate {
    private final List<Runnable> listeners = new ArrayList<>();
    private byte day;

    DayUpdate(byte day) {
        this.day = day;
        ForTheCrown.logger().info("DayUpdate loaded");
    }

    public void checkDay(){
        Calendar calendar = Calendar.getInstance(FtcUtils.SERVER_TIME_ZONE);
        if(calendar.get(Calendar.DAY_OF_WEEK) != getDay()) update();
    }

    public void update(){
        ForTheCrown.logger().info("Updating date");
        setDay((byte) Calendar.getInstance(FtcUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK));

        listeners.forEach(r -> {
            try {
                r.run();
            } catch (Exception e){
                ForTheCrown.logger().severe("Could not update date of " + r.getClass().getSimpleName());
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

    public void setDay(byte day) {
        this.day = day;
        ForTheCrown.config().set("Day", day);
    }

    public List<Runnable> getListeners() {
        return listeners;
    }
}

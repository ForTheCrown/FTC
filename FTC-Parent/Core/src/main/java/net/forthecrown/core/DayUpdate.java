package net.forthecrown.core;

import net.forthecrown.utils.CrownUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DayUpdate {
    private final List<Runnable> listeners = new ArrayList<>();
    private byte day;

    DayUpdate(byte day) {
        this.day = day;
        CrownCore.logger().info("DayUpdate loaded");
    }

    public void checkDay(){
        Calendar calendar = Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE);
        if(calendar.get(Calendar.DAY_OF_WEEK) != getDay()) update();
    }

    public void update(){
        CrownCore.logger().info("Updating date");

        listeners.forEach(r -> {
            try {
                r.run();
            } catch (Exception e){
                CrownCore.logger().severe("Could not update date of " + r.getClass().getSimpleName());
                e.printStackTrace();
            }
        });

        setDay((byte) Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK));
    }

    public void addListener(Runnable runnable){
        listeners.add(runnable);
    }

    public byte getDay() {
        return day;
    }

    public void setDay(byte day) {
        this.day = day;
    }

    public List<Runnable> getListeners() {
        return listeners;
    }
}

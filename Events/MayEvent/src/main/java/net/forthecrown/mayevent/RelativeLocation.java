package net.forthecrown.mayevent;

import org.bukkit.Location;

public class RelativeLocation implements Cloneable{

    public Location relativeTo;
    public final double xOffset;
    public final double yOffset;
    public final double zOffset;

    public RelativeLocation(Location relativeTo, double xOffset, double yOffset, double zOffset) {
        this.relativeTo = relativeTo.clone();
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public RelativeLocation(double xOffset, double yOffset, double zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public Location getLocation(){
        return relativeTo.clone().add(xOffset, yOffset, zOffset);
    }

    public RelativeLocation setRelativeTo(Location relativeTo) {
        this.relativeTo = relativeTo.clone();
        return this;
    }

    public RelativeLocation clone(){
        return new RelativeLocation(relativeTo, xOffset, yOffset, zOffset);
    }
}
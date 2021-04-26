package net.forthecrown.mayevent.guns;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public interface GunHolder {
    HitScanWeapon getHeldGun();
    void setGun(HitScanWeapon gun);

    default boolean fireGun(){
        HitScanWeapon gun = getHeldGun();
        if(gun == null) return false;

        return gun.attemptUse(this);
    }

    default Vector getAimingDirection(){
        return getLocation().getDirection();
    }

    LivingEntity getHoldingEntity();

    Location getLocation();

    int getWave();

    default boolean ignoreAmmo(){
        return true;
    }
}

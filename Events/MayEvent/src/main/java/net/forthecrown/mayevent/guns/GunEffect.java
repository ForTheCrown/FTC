package net.forthecrown.mayevent.guns;

@FunctionalInterface
public interface GunEffect {
    void effect(HitScanWeapon.HitScanShot shot, HitScanWeapon weapon);
}

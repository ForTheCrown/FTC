package net.forthecrown.mayevent.guns;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.mayevent.MayUtils;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public enum WeaponEffect {

    BULLET_HIT_WALL ((shot, gun) -> {
        new ParticleBuilder(Particle.ASH).extra(25).count(2).location(shot.endLoc).allPlayers().spawn();
    }),
    BULLET_HIT_MOB ((shot, gun) -> {
        new ParticleBuilder(Particle.REDSTONE).extra(25).color(Color.RED).count(5).location(shot.endLoc).allPlayers().spawn();
    }),

    EXPLOSION ((shot, gun) -> {
        MayUtils.attemptDestruction(shot.endLoc, (int) gun.damage() + shot.wave);
        MayUtils.damageInRadius(shot.endLoc, (int) gun.damage()/2, gun.damage() + shot.wave);

        new ParticleBuilder(Particle.EXPLOSION_HUGE).count(5).extra(5).location(shot.endLoc).allPlayers().spawn();
        shot.endLoc.getWorld().playSound(shot.endLoc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
    }),

    GUNSHOT((shot, gun) -> {
        new ParticleBuilder(Particle.REDSTONE).color(Color.GRAY).count(5).extra(2).location(shot.endLoc).spawn();
        shot.eyeLoc.getWorld().playSound(shot.eyeLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 0.1f);
    }),

    BLOCK_BREAK((shot, gun) -> {
        new ParticleBuilder(Particle.ASH).count(10).extra(10).location(shot.endLoc).allPlayers().spawn();
        shot.endLoc.getWorld().playSound(shot.endLoc, Sound.BLOCK_METAL_BREAK, 1f ,1f);
    });

    private final GunEffect effect;
    WeaponEffect(GunEffect effect){
        this.effect = effect;
    }

    public void play(HitScanWeapon.HitScanShot shot, HitScanWeapon weapon){
        effect.effect(shot, weapon);
    }
}

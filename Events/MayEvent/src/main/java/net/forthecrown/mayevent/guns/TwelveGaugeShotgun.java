package net.forthecrown.mayevent.guns;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.core.utils.CrownRandom;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.forthecrown.mayevent.BlockDamageTracker;
import net.forthecrown.mayevent.MayUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class TwelveGaugeShotgun extends HitScanWeapon {
    public TwelveGaugeShotgun() {
        super(32, 5, 16, "Shotgun",
                (remaining, max) -> new ChatComponentText("Twelve Gauge Shotgun")
                        .a(EnumChatFormat.GREEN, EnumChatFormat.BOLD)
                        .addSibling(new ChatComponentText(": " + remaining + "/" + max).a(EnumChatFormat.WHITE)),

                () -> new ItemStackBuilder(Material.WOODEN_HOE, 1)
                        .setFlags(ItemFlag.HIDE_UNBREAKABLE)
                        .setUnbreakable(true)
                        .setName(Component.text("Shotgun").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .addLore(Component.text("Reliably deadly lol").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)),

                () -> new ItemStackBuilder(Material.BROWN_GLAZED_TERRACOTTA, 12)
                        .setName(Component.text("12 pack of Shotgun ammo")
                                .color(NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        )
        );
    }

    @Override
    public boolean attemptUse(Location eyeLoc, Vector direction, boolean ignoreAmmo, LivingEntity entity, int wave) {
        testCooldownAndAdd(entity);

        if(remainingAmmo <= 0 && !ignoreAmmo) return false;
        remainingAmmo -= ignoreAmmo ? 0 : 1;

        CrownRandom random = new CrownRandom();

        for (int i = 0; i < 5; i++){
            Location eLoc = eyeLoc.clone();
            eLoc.setYaw(eyeLoc.getYaw() + random.intInRange(-5, 5));
            eLoc.setPitch(eyeLoc.getPitch() + random.intInRange(-5, 5));

            Vector dir = eLoc.getDirection();

            RayTraceResult hitScan = eyeLoc.getWorld().rayTraceBlocks(eLoc, dir, 100);

            RayTraceResult entHitScan = eyeLoc.getWorld().rayTraceEntities(eLoc, dir, 100, cutie -> !(cutie instanceof Player));
            Entity target = entHitScan == null ? null : entHitScan.getHitEntity();

            onUse(new HitScanShot(hitScan, eLoc, dir, entity, target, wave));
        }

        makeMessage();
        return true;
    }

    @Override
    protected boolean onUse(HitScanShot shot) {
        WeaponEffect.GUNSHOT.play(shot, this);
        if(shot.hasHitEntity) {
            if (!(shot.target instanceof LivingEntity)) return true;
            LivingEntity entity = (LivingEntity) shot.target;
            entity.damage(damage + shot.wave);
            WeaponEffect.BULLET_HIT_MOB.play(shot, this);
        } else {
            WeaponEffect.BULLET_HIT_WALL.play(shot, this);
            if(BlockDamageTracker.damage(shot.hitScan.getHitBlock(), damage) == 31) WeaponEffect.BLOCK_BREAK.play(shot, this);
        }

        MayUtils.drawLine(shot.eyeLoc, shot.endLoc, 2, new ParticleBuilder(Particle.REDSTONE).color(Color.GRAY).count(2));
        return true;
    }
}

package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.inventory.weapon.AltAttackContext;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.forthecrown.inventory.weapon.click.Click;
import net.forthecrown.utils.Bukkit2NMS;
import net.forthecrown.utils.TimeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.bukkit.Location;

public class ExplodeAbility extends WeaponAbility {
    public static final long COOLDOWN_MILLIS = TimeUtil.SECOND_IN_MILLIS * 5;

    ExplodeAbility() {
        super("Explode");
    }

    @Override
    public void onWeaponUse(WeaponUseContext context) {
        if(!context.clickHistory.hasPattern(Click.LEFT, Click.RIGHT, Click.RIGHT)) return;
        if(isOnCooldown(context.sword.getExtraData())) return;

        Location loc = context.entity.getLocation().add(0, context.entity.getHeight() / 2, 0);
        Level level = Bukkit2NMS.getLevel(loc.getWorld());

        context.player.setInvulnerable(true);

        level.explode(
                Bukkit2NMS.getEntity(context.player),
                loc.getX(), loc.getY(), loc.getZ(), 2.4f,
                Explosion.BlockInteraction.NONE
        );

        context.player.setInvulnerable(false);
    }

    @Override
    public void onBlockAltAttack(AltAttackContext.c_Block context) {

    }

    @Override
    public void onEntityAltAttack(AltAttackContext.c_Entity context) {

    }

    boolean isOnCooldown(CompoundTag extraData) {
        long lastUse = extraData.getLong("last_use");
        return (lastUse + COOLDOWN_MILLIS) > System.currentTimeMillis();
    }
}

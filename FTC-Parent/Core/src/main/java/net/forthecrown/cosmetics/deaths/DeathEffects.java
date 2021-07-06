package net.forthecrown.cosmetics.deaths;

import net.forthecrown.core.CrownCore;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;

public class DeathEffects {

    private static BuiltInventory INVENTORY;

    public static SoulDeathEffect SOUL;
    public static TotemDeathEffect TOTEM;
    public static ExplosionDeathEffect EXPLOSION;
    public static EnderRingDeathEffect ENDER_RING;

    public static void init(){
        SOUL = register(new SoulDeathEffect());
        TOTEM = register(new TotemDeathEffect());
        EXPLOSION = register(new ExplosionDeathEffect());
        ENDER_RING = register(new EnderRingDeathEffect());

        INVENTORY = CosmeticConstants.baseInventory(36, Component.text("Death effects"), true)
                .addOptions(Registries.DEATH_EFFECTS)
                .addOption(CosmeticConstants.NO_DEATH)
                .build();

        Registries.DEATH_EFFECTS.close();
        CrownCore.logger().info("Death Effects registered");
    }

    private static <T extends AbstractDeathEffect> T register(T effect){
        return (T) Registries.DEATH_EFFECTS.register(effect.key(), effect);
    }

    public static BuiltInventory getInventory(){
        return INVENTORY;
    }


}

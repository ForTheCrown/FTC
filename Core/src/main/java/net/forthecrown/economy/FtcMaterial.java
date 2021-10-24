package net.forthecrown.economy;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.Translatable;
import net.minecraft.Util;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum FtcMaterial implements Translatable, ComponentLike, Keyed {
    ROTTEN_FLESH (MaterialGroup.DROPS),
    BONE (MaterialGroup.DROPS),
    ARROW (MaterialGroup.DROPS),
    STRING (MaterialGroup.DROPS),
    SPIDER_EYE (MaterialGroup.DROPS),
    LEATHER (MaterialGroup.DROPS),
    GUNPOWDER (MaterialGroup.DROPS),
    BLAZE_ROD (MaterialGroup.DROPS),
    SLIME_BALL (MaterialGroup.DROPS),
    COD (MaterialGroup.DROPS),
    INK_SAC (MaterialGroup.DROPS),
    GLOW_INK_SAC (MaterialGroup.DROPS),

    COAL (MaterialGroup.MINERALS),
    EMERALD (MaterialGroup.MINERALS),
    DIAMOND (MaterialGroup.MINERALS),
    LAPIS_LAZULI (MaterialGroup.MINERALS),
    REDSTONE (MaterialGroup.MINERALS),
    COPPER_INGOT (MaterialGroup.MINERALS),
    IRON_INGOT (MaterialGroup.MINERALS),
    GOLD_INGOT (MaterialGroup.MINERALS),
    NETHERITE_INGOT (MaterialGroup.MINERALS),
    AMETHYST_SHARD (MaterialGroup.MINERALS),

    DIRT (MaterialGroup.MINING),
    STONE (MaterialGroup.MINING),
    GRANITE (MaterialGroup.MINING),
    DIORITE (MaterialGroup.MINING),
    ANDESITE (MaterialGroup.MINING),
    COBBLESTONE (MaterialGroup.MINING),
    GRAVEL (MaterialGroup.MINING),
    SAND (MaterialGroup.MINING),
    CALCITE (MaterialGroup.MINING),
    DEEPSLATE (MaterialGroup.MINING),
    COBBLED_DEEPSLATE (MaterialGroup.MINING),
    SMOOTH_BASALT (MaterialGroup.MINING),
    BASALT (MaterialGroup.MINING),
    NETHERRACK (MaterialGroup.MINING),
    BLACKSTONE (MaterialGroup.MINING),
    END_STONE (MaterialGroup.MINING),

    AMETHYST_BLOCK (MaterialGroup.CRAFTABLE_BLOCKS),
    IRON_BLOCK (MaterialGroup.MINING),
    DIAMOND_BLOCK (MaterialGroup.MINING),
    GOLD_BLOCK (MaterialGroup.MINING),
    NETHERITE_BLOCK (MaterialGroup.MINING),
    SLIME_BLOCK (MaterialGroup.MINING),
    EMERALD_BLOCK (MaterialGroup.MINING),
    REDSTONE_BLOCK (MaterialGroup.MINING),
    LAPIS_BLOCK (MaterialGroup.MINING),
    COAL_BLOCK (MaterialGroup.MINING),

    BAMBOO (MaterialGroup.FARMING),
    KELP (MaterialGroup.FARMING),
    CACTUS (MaterialGroup.FARMING),
    MELON (MaterialGroup.FARMING),
    VINES (MaterialGroup.FARMING),
    SUGAR_CANE (MaterialGroup.FARMING),
    POTATO (MaterialGroup.FARMING),
    WHEAT (MaterialGroup.FARMING),
    CARROT (MaterialGroup.FARMING),
    PUMPKIN (MaterialGroup.FARMING),
    BEETROOT_SEEDS (MaterialGroup.FARMING),
    BEETROOT (MaterialGroup.FARMING),
    CHORUS_FRUIT (MaterialGroup.FARMING),
    SWEET_BERRIES (MaterialGroup.FARMING),
    WHEAT_SEEDS (MaterialGroup.FARMING);

    private static final Map<Material, FtcMaterial> BUKKIT_TO_FTC = Util.make(() -> {
        FtcMaterial[] values = FtcMaterial.values();
        Object2ObjectOpenHashMap<Material, FtcMaterial> result = new Object2ObjectOpenHashMap<>(values.length);

       for (FtcMaterial m: values) {
           result.put(m.bukkitMaterial, m);
       }

       return Object2ObjectMaps.unmodifiable(result);
    });

    private final MaterialGroup group;
    private final Material bukkitMaterial;

    FtcMaterial(MaterialGroup group) {
        this.group = group;
        this.bukkitMaterial = Material.getMaterial(name());
    }

    FtcMaterial(MaterialGroup group, Material bukkitMaterial) {
        this.group = group;
        this.bukkitMaterial = bukkitMaterial;
    }

    public static FtcMaterial of(Material material) {
        return BUKKIT_TO_FTC.get(material);
    }

    public static FtcMaterial fromString(String string) {
        return of(Material.matchMaterial(string));
    }

    public Material getBukkitMaterial() {
        return bukkitMaterial;
    }

    public MaterialGroup getGroup() {
        return group;
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.translatable(translationKey());
    }

    @Override
    public @NotNull String translationKey() {
        return getBukkitMaterial().getTranslationKey();
    }

    @Override
    public @NotNull Key key() {
        return getBukkitMaterial().getKey();
    }
}

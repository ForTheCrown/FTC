package net.forthecrown.pirates.grappling;

import net.forthecrown.core.chat.FtcFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

public enum GhBiome {
    FLOATING_ISLANDS (NamedTextColor.AQUA, Material.GRASS_BLOCK, Material.GREEN_TERRACOTTA),
    RED_DESERT (NamedTextColor.GOLD, Material.RED_SANDSTONE, Material.ORANGE_TERRACOTTA),
    TETRIS (NamedTextColor.GREEN, Material.PURPLE_STAINED_GLASS, Material.PURPLE_TERRACOTTA),
    PARKOUR (NamedTextColor.LIGHT_PURPLE, Material.OAK_PLANKS, Material.TERRACOTTA),
    TEMPLE (NamedTextColor.GRAY, Material.BLACK_STAINED_GLASS, Material.BLACK_TERRACOTTA);

    public final TextColor color;
    private final Material selectorMaterial;
    private final Material completedMaterial;

    GhBiome(TextColor color, Material selectorMat, Material completedMat) {
        this.color = color;

        this.selectorMaterial = selectorMat;
        this.completedMaterial = completedMat;
    }

    public Material completedMat() {
        return completedMaterial;
    }

    public Material selectorMat() {
        return selectorMaterial;
    }

    public Component displayName(){
        return Component.text(FtcFormatter.normalEnum(this));
    }
}

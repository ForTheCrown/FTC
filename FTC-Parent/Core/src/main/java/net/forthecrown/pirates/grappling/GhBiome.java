package net.forthecrown.pirates.grappling;

import net.forthecrown.core.chat.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum GhBiome {
    FLOATING_ISLANDS (NamedTextColor.AQUA),
    RED_DESERT (NamedTextColor.GOLD),
    TETRIS (NamedTextColor.GREEN),
    PARKOUR (NamedTextColor.LIGHT_PURPLE),
    TEMPLE (NamedTextColor.GRAY);

    public final TextColor color;
    GhBiome(TextColor color) {
        this.color = color;
    }

    public Component displayName(){
        return Component.text(ChatFormatter.capitalizeWords(name().toLowerCase().replaceAll("_", " ")));
    }
}

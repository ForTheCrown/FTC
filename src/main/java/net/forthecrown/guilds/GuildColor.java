package net.forthecrown.guilds;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum GuildColor {
    WHITE       (NamedTextColor.WHITE),
    ORANGE      (NamedTextColor.GOLD),
    MAGENTA     (NamedTextColor.LIGHT_PURPLE),
    LIGHT_BLUE  (NamedTextColor.AQUA),
    YELLOW      (NamedTextColor.YELLOW),
    LIME        (NamedTextColor.GREEN),
    PINK        (TextColor.fromHexString("#fc95b6")),
    GRAY        (NamedTextColor.DARK_GRAY),
    LIGHT_GRAY  (NamedTextColor.GRAY),
    CYAN        (NamedTextColor.DARK_AQUA),
    PURPLE      (NamedTextColor.DARK_PURPLE),
    BLUE        (NamedTextColor.BLUE),
    BROWN       (TextColor.fromHexString("#724728")),
    GREEN       (NamedTextColor.DARK_GREEN),
    RED         (NamedTextColor.RED),
    BLACK       (TextColor.fromHexString("#333333")),
    ;

    private final TextColor textColor;

    public Material toWool() {
        return Material.matchMaterial(this.name() + "_wool");
    }

    public Material toGlassPane() {
        return Material.matchMaterial(name() + "_stained_glass_pane");
    }

    public String toText() {
        return Text.prettyEnumName(this);
    }
}
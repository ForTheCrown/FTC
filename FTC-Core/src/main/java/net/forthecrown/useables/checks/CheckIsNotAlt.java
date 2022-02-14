package net.forthecrown.useables.checks;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckIsNotAlt implements UsageCheckInstance {
    public static final Key KEY = Keys.forthecrown("not_alt");

    @Override
    public String asString() {
        return typeKey().asString();
    }

    @Override
    public Component failMessage(Player player) {
        return Component.text("Alt accounts cannot use this")
                .color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull Key typeKey() {
        return KEY;
    }

    @Override
    public boolean test(Player player) {
        return !Crown.getUserManager().isAlt(player.getUniqueId());
    }
}
package net.forthecrown.useables.checks;

import net.forthecrown.core.Keys;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CheckNoRiders implements UsageCheckInstance {
    public static final Key KEY = Keys.ftc("no_riders");

    @Override
    public @Nullable Component failMessage(Player player) {
        return Component.text("You cannot be ridden to use this")
                .color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        return ListUtils.isNullOrEmpty(player.getPassengers());
    }

    @Override
    public String asString() {
        return typeKey().asString();
    }

    @Override
    public Key typeKey() {
        return KEY;
    }
}

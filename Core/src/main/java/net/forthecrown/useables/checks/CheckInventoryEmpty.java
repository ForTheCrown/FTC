package net.forthecrown.useables.checks;

import net.forthecrown.core.Keys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckInventoryEmpty implements UsageCheckInstance {
    public static final Key KEY = Keys.ftccore("inventory_empty");

    @Override
    public String asString() {
        return typeKey().asString();
    }

    @Override
    public Component failMessage(Player player) {
        return Component.text("You must have an empty inventory").color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull Key typeKey() {
        return KEY;
    }

    @Override
    public boolean test(Player player) {
        return player.getInventory().isEmpty();
    }
}

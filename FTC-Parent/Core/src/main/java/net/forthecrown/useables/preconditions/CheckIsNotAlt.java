package net.forthecrown.useables.preconditions;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckIsNotAlt implements UsageCheckInstance {
    public static final Key KEY = Key.key(ForTheCrown.inst(), "not_alt");

    @Override
    public String asString() {
        return typeKey().asString();
    }

    @Override
    public Component failMessage() {
        return Component.text("Alt accounts cannot use this")
                .color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull Key typeKey() {
        return KEY;
    }

    @Override
    public boolean test(Player player) {
        return !UserManager.inst().isAlt(player.getUniqueId());
    }
}

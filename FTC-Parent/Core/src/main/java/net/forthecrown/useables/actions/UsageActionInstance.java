package net.forthecrown.useables.actions;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface UsageActionInstance {
    void onInteract(Player player);
    String asString();

    @NotNull Key typeKey();
}

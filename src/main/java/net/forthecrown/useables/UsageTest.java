package net.forthecrown.useables;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public abstract class UsageTest extends UsageInstance {
    public UsageTest(UsageType type) {
        super(type);
    }

    public abstract boolean test(Player player, CheckHolder holder);

    @Nullable
    public abstract Component getFailMessage(Player player, CheckHolder holder);

    public void postTests(Player player, CheckHolder holder) {

    }
}
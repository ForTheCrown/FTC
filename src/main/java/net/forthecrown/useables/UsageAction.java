package net.forthecrown.useables;

import org.bukkit.entity.Player;

public abstract class UsageAction extends UsageInstance {
    public UsageAction(UsageType type) {
        super(type);
    }

    public abstract void onUse(Player player, ActionHolder holder);
}
package net.forthecrown.easteregghunt;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public interface IUserTracker {
    Map<UUID, Byte> tracked();

    void clear();

    boolean entryAllowed(Player player);

    void put(Player player, Byte amount);

    void increment(Player player);

    byte get(Player player);
}

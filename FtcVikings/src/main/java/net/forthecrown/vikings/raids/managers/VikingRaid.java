package net.forthecrown.vikings.raids.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public interface VikingRaid {
    void raidInit(Player player, RaidDifficulty difficulty, PluginManager manager) ;

    void onRaidEnd();
    void onRaidLoad();
    void onRaidComplete();

    Location getRaidLocation();

    void setUsingPlayer(Player player);
    Player getUsingPlayer();

    void setDifficulty(RaidDifficulty difficulty);
    RaidDifficulty getDifficulty();

    String getName();
}

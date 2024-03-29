package net.forthecrown.dungeons.boss;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.forthecrown.Loggers;
import net.forthecrown.registry.FtcKeyed;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

public interface KeyedBoss extends DungeonBoss, FtcKeyed {

  default JsonElement serialize() {
    return new JsonPrimitive(getKey());
  }

  /**
   * Gets the key to this boss' advancement
   *
   * @return The boss' advancement key
   */
  default NamespacedKey advancementKey() {
    return NamespacedKey.fromString("forthecrown:dungeons/" + getKey());
  }

  /**
   * Awards this boss' advancement to the given player
   *
   * @param player The player to award
   */
  default void awardAdvancement(Player player) {
    Advancement advancement = Bukkit.getAdvancement(advancementKey());

    if (advancement == null) {
      Loggers.getLogger().warn("{} boss has no advancement", getKey());
      return;
    }

    AdvancementProgress progress = player.getAdvancementProgress(advancement);
    for (String s : progress.getRemainingCriteria()) {
      progress.awardCriteria(s);
    }
  }
}
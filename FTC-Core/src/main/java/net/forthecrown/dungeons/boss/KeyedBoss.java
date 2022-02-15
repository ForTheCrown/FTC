package net.forthecrown.dungeons.boss;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Keyed;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

public interface KeyedBoss extends DungeonBoss, Keyed, JsonSerializable {
    @Override
    default JsonElement serialize() {
        return JsonUtils.writeKey(key());
    }

    /**
     * Gets the key to this boss' advancement
     * @return The boss' advancement key
     */
    default NamespacedKey advancementKey() {
        return Keys.key(key().namespace(), "dungeons/" + key().value());
    }

    /**
     * Awards this boss' advancement to the given player
     * @param player The player to award
     */
    default void awardAdvancement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(advancementKey());

        if(advancement == null) {
            Crown.logger().warn("{} boss has no advancement", key().asString());
            return;
        }

        FtcUtils.grantAdvancement(advancement, player);
    }
}

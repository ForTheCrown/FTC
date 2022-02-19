package net.forthecrown.useables.warps;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.BaseRegistry;
import net.forthecrown.serializer.CrownSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Represents a registry of warps
 */
public interface WarpManager extends CrownSerializer, SuggestionProvider<CommandSource>, BaseRegistry<Warp, Location> {

    /**
     * Gets all the warps the given player can use
     * @param player The player to get the warps of
     * @return The warps the player can use
     */
    List<Warp> getUsableFor(Player player);
}

package net.forthecrown.registry;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.useables.warps.Warp;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Represents a registry of warps
 */
public interface WarpRegistry extends CrownSerializer, SuggestionProvider<CommandSource>, CrownRegistry<Warp, Location> {

    /**
     * Gets all the warps the given player can use
     * @param player The player to get the warps of
     * @return The warps the player can use
     */
    List<Warp> getUsableFor(Player player);
}

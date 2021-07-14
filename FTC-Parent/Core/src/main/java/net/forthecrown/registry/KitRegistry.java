package net.forthecrown.registry;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.useables.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Holds and registers all the kits there are.
 */
public interface KitRegistry extends CrownSerializer, CrownRegistry<Kit, List<ItemStack>>, SuggestionProvider<CommandSource> {

    /**
     * Gets all the kits a given player can use
     * @param player The player to get the kits for
     * @return All the kits the player can use
     */
    List<Kit> getUsableFor(Player player);
}

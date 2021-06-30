package net.forthecrown.registry;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.useables.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface KitRegistry extends CrownSerializer, CrownRegistry<Kit, List<ItemStack>>, SuggestionProvider<CommandSource> {
    List<Kit> getUseableFor(Player player);
}

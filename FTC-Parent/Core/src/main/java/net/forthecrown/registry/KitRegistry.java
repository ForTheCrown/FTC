package net.forthecrown.core.registry;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.core.CrownCore;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.useables.kits.Kit;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface KitRegistry extends CrownSerializer<CrownCore>, CrownRegistry<Kit, List<ItemStack>>, SuggestionProvider<CommandSource> {
    List<Kit> getUseableFor(Player player);
}

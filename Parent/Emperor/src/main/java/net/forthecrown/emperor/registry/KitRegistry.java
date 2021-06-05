package net.forthecrown.emperor.registry;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.serializer.CrownSerializer;
import net.forthecrown.emperor.useables.kits.Kit;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface KitRegistry extends CrownSerializer<CrownCore>, CrownRegistry<Kit, List<ItemStack>>, SuggestionProvider<CommandSource> {
    List<Kit> getUseableFor(Player player);
}

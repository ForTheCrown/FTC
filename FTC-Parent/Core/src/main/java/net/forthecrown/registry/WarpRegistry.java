package net.forthecrown.registry;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.useables.warps.Warp;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface WarpRegistry extends CrownSerializer, SuggestionProvider<CommandSource>, CrownRegistry<Warp, Location> {
    List<Warp> getUseableWarpsFor(Player player);
}

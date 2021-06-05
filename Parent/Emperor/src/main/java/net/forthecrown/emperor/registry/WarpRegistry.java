package net.forthecrown.emperor.registry;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.serializer.CrownSerializer;
import net.forthecrown.emperor.useables.warps.Warp;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface WarpRegistry extends CrownSerializer<CrownCore>, SuggestionProvider<CommandSource>, CrownRegistry<Warp, Location> {
    List<Warp> getUseableWarpsFor(Player player);
}

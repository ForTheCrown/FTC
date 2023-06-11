package net.forthecrown.dungeons.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.forthecrown.dungeons.listeners.PunchingBags;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.text.Text;
import org.bukkit.Location;

@CommandData("file = commands/punching_bag.gcn")
public class CommandPunchingBag {

  void createDummy(
      CommandSource source,
      @Argument(value = "location", optional = true) Location location
  ) throws CommandSyntaxException {
    location = Objects.requireNonNullElseGet(location, source::getLocation);
    PunchingBags.spawnDummy(location);

    source.sendSuccess(
        Text.format("Summoned dummy at {0, location}", location)
    );
  }
}
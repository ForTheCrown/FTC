package net.forthecrown.packet;

import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;

public interface SignRenderer {

  boolean test(Player player, WorldVec3i pos, Sign sign);

  void render(Player player, WorldVec3i pos, Sign sign);
}

package net.forthecrown.packet;

import lombok.Getter;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

@Getter
class PacketRendererImpl implements PacketRenderingService {

  private final Registry<SignRenderer> signRenderers = Registries.newRegistry();
  private final Registry<EntityRenderer> entityRenderers = Registries.newRegistry();

  public boolean renderSign(Sign sign, WorldVec3i pos, Player player) {
    if (signRenderers.isEmpty()) {
      return false;
    }

    for (SignRenderer r: signRenderers) {
      if (!r.test(player, pos, sign)) {
        continue;
      }

      r.render(player, pos, sign);
      return true;
    }

    return false;
  }
}

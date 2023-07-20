package net.forthecrown.core.listeners;

import net.forthecrown.packet.SignRenderer;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SignRenderTest implements SignRenderer {

  @Override
  public boolean test(Player player, WorldVec3i pos, Sign sign) {
    return sign.getLine(0).contains("TEST_SIGN");
  }

  @Override
  public void render(Player player, WorldVec3i pos, Sign sign) {
    sign.line(0, player.displayName());
    sign.line(1, player.displayName());
    sign.line(2, player.displayName());
    sign.line(3, player.displayName());
  }
}

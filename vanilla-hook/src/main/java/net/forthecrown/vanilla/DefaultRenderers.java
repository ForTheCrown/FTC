package net.forthecrown.vanilla;

import java.util.Map;
import net.forthecrown.packet.SignRenderer;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.vanilla.packet.ListenersImpl;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;

public class DefaultRenderers {

  static final NamespacedKey SIGN_KEY = new NamespacedKey("forthecrown", "render_placeholders");

  static final SignRenderer SIGN_PLACEHOLDERS = new SignRenderer() {
    @Override
    public boolean test(Player player, WorldVec3i pos, Sign sign) {
      return sign.getPersistentDataContainer().has(SIGN_KEY);
    }

    @Override
    public void render(Player player, WorldVec3i pos, Sign sign) {
      PlaceholderRenderer renderer = Placeholders.newRenderer();
      Map<String, Object> ctx = Map.of("pos", pos);

      renderSide(sign.getSide(Side.FRONT), renderer, player, ctx);
      renderSide(sign.getSide(Side.BACK), renderer, player, ctx);
    }

    void renderSide(
        SignSide side,
        PlaceholderRenderer renderer,
        Player player,
        Map<String, Object> ctx
    ) {
      for (int i = 0; i < 4; i++) {
        side.line(i, renderer.render(side.line(i), player, ctx));
      }
    }
  };

  static void registerAll(ListenersImpl listeners) {
    listeners.getSignRenderers().register("placeholders", SIGN_PLACEHOLDERS);
  }
}

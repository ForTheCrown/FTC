package net.forthecrown.events;

import java.util.Objects;
import java.util.UUID;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Permissions;
import net.minecraft.core.UUIDUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SignOwnershipListener implements Listener {

  public static final NamespacedKey SIGN_OWNER = Keys.forthecrown("sign_owner");

  public static final boolean ADMIN_BYPASS = !FTC.inDebugMode();

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onSignChange(SignChangeEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    Sign sign = (Sign) event.getBlock().getState();
    setOwner(sign, playerId);
    sign.update();
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onPlayerInteract(PlayerInteractEvent event) {
    BlockState state = event.getClickedBlock().getState();

    if (!(state instanceof Sign sign)) {
      return;
    }

    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    UUID owner = getOwner(sign);

    if ((player.hasPermission(Permissions.ADMIN) && ADMIN_BYPASS)
        || Objects.equals(playerId, owner)
    ) {
      return;
    }

    event.setCancelled(true);
  }

  UUID getOwner(Sign sign) {
    PersistentDataContainer container = sign.getPersistentDataContainer();
    int[] intList = container.get(SIGN_OWNER, PersistentDataType.INTEGER_ARRAY);

    if (intList == null || intList.length < 1) {
      return null;
    }

    return UUIDUtil.uuidFromIntArray(intList);
  }

  void setOwner(Sign sign, UUID owner) {
    PersistentDataContainer container = sign.getPersistentDataContainer();

    if (owner == null) {
      container.remove(SIGN_OWNER);
      return;
    }

    int[] savedUuid = UUIDUtil.uuidToIntArray(owner);
    container.set(SIGN_OWNER, PersistentDataType.INTEGER_ARRAY, savedUuid);
  }
}

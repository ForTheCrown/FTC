package net.forthecrown.events;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.logging.Loggers;
import net.minecraft.core.UUIDUtil;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SignOwnershipListener implements Listener {

  public static final Logger LOGGER = Loggers.getLogger();

  public static final NamespacedKey SIGN_OWNER = Keys.forthecrown("sign_owner");

  private final Set<UUID> justClicked = new HashSet<>();

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onSignChange(SignChangeEvent event) {
    Sign sign = (Sign) event.getBlock().getState();

    if (!canEdit(sign, event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    var block = event.getClickedBlock();
    if (!(block.getState() instanceof Sign)) {
      return;
    }

    justClicked.add(event.getPlayer().getUniqueId());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onPlayerOpenSign(PlayerOpenSignEvent event) {
    var id = event.getPlayer().getUniqueId();
    var sign = event.getSign();

    // Doesn't contain means they just placed the sign
    if (!justClicked.contains(id)) {
      setOwnerIfNoneExists(sign, id);
      sign.update();
      return;
    }

    justClicked.remove(id);
    Player player = event.getPlayer();

    if (canEdit(sign, player)) {
      return;
    }

    event.setCancelled(true);
  }

  boolean canEdit(Sign sign, Player player) {
    if (player.hasPermission(Permissions.ADMIN)) {
      return true;
    }

    UUID owner = getOwner(sign);
    return Objects.equals(owner, player.getUniqueId());
  }

  UUID getOwner(Sign sign) {
    PersistentDataContainer container = sign.getPersistentDataContainer();
    int[] intList = container.get(SIGN_OWNER, PersistentDataType.INTEGER_ARRAY);

    if (intList == null || intList.length < 1) {
      return null;
    }

    return UUIDUtil.uuidFromIntArray(intList);
  }

  void setOwnerIfNoneExists(Sign sign, UUID owner) {
    PersistentDataContainer container = sign.getPersistentDataContainer();

    if (owner == null) {
      container.remove(SIGN_OWNER);
      return;
    }

    if (container.has(SIGN_OWNER)) {
      return;
    }

    int[] savedUuid = UUIDUtil.uuidToIntArray(owner);
    container.set(SIGN_OWNER, PersistentDataType.INTEGER_ARRAY, savedUuid);
  }
}

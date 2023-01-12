package net.forthecrown.utils.inventory.menu;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

@Getter
public class ExternalClickContext {
  private final User user;
  private final Player player;

  private final Inventory clicked;
  private final MenuInventory menuInventory;

  private final int rawSlot;
  private final int slot;
  private final InventoryView view;

  private final ClickType type;

  @Setter
  @Accessors(fluent = true)
  private boolean cancelEvent;

  public ExternalClickContext(InventoryClickEvent event) {
    this.player = (Player) event.getWhoClicked();
    this.user = Users.get(player);
    this.view = event.getView();

    this.clicked = view.getBottomInventory();
    this.menuInventory = (MenuInventory) view.getTopInventory();

    this.slot = event.getSlot();
    this.rawSlot = event.getRawSlot();

    this.type = event.getClick();
    this.cancelEvent = event.isCancelled();
  }
}
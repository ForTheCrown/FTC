package net.forthecrown.menu;

import static net.forthecrown.Cooldowns.NO_END_COOLDOWN;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 * A context around an inventory click
 * <p>
 * Also allows you to set whether the inventory should be reset with
 * {@link ClickContext#shouldReloadMenu(boolean)} (boolean)}
 * <p>
 * As well as modifying the cooldown after a click with {@link ClickContext#setCooldownTime(int)}.
 * Set to 0 or less for no cooldown.
 */
@Getter
public class ClickContext {

  /**
   * Default cooldown length, 10 ticks
   */
  public static final byte DEFAULT_COOLDOWN = 10;

  /**
   * The inventory being clicked
   */
  private final Inventory inventory;

  /** Menu holder object */
  private final MenuHolder holder;

  /**
   * The player clicking in the inventory
   */
  private final Player player;

  /**
   * The slot being clicked
   */
  private final int slot;

  /** Raw inventory slot, usable in {@link #getView()} */
  private final int rawSlot;

  /**
   * The item stack which is current on the cursor
   */
  private final ItemStack cursorItem;

  /**
   * The type of click
   */
  private final ClickType clickType;

  /**
   * Determines if the inventory should be reloaded after the click code is executed
   */
  @Accessors(fluent = true)
  @Setter
  private boolean shouldReloadMenu;

  /**
   * Determines if the vanilla click event should be cancelled
   */
  @Accessors(fluent = true)
  @Setter
  private boolean cancelEvent;

  /**
   * Determines if the inventory should be closed after this click code is ran
   */
  @Accessors(fluent = true)
  @Setter
  private boolean shouldClose;

  /**
   * The amount of ticks the user be in menu cooldown, 0 or less to disable
   */
  @Setter
  private int cooldownTime;

  /**
   * The {@link #getPlayer()}'s current inventory view
   */
  private final InventoryView view;

  /** The node being clicked */
  MenuNode node;

  public ClickContext(Inventory inventory, InventoryClickEvent event) {
    this.inventory = inventory;
    this.holder = (MenuHolder) inventory.getHolder();
    this.player = (Player) event.getWhoClicked();
    this.slot = event.getSlot();
    this.rawSlot = event.getRawSlot();
    this.cursorItem = event.getCursor();
    this.clickType = event.getClick();
    this.view = event.getView();

    this.cancelEvent = true;
    this.cooldownTime = DEFAULT_COOLDOWN;
  }

  /**
   * Tests if this context should place the player in cooldown
   *
   * @return True, if {@link #getCooldownTime()} is larger than 0, false otherwise
   */
  public boolean shouldCooldown() {
    return cooldownTime > 0 || cooldownTime == NO_END_COOLDOWN;
  }
}
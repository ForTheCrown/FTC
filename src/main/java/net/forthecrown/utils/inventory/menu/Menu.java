package net.forthecrown.utils.inventory.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

@Getter
public class Menu implements InventoryHolder, MenuCloseConsumer {

  /**
   * The cooldown category menus use
   */
  public static final String COOLDOWN_CATEGORY = Menu.class.getName();

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * The menu's title
   */
  private final Component title;

  /**
   * The menu's size
   */
  private final int size;

  /**
   * True, if items can be removed from the inventory and added it to it by users
   */
  private final boolean itemMovingAllowed;

  /**
   * Inventory index 2 menu node map. Immutable
   */
  private final Int2ObjectMap<MenuNode> nodes;

  /**
   * Callback for when this menu is opened, called before the inventory options are placed into the
   * inventory
   */
  private final MenuOpenConsumer openCallback;

  /**
   * Callback for when this menu is closed
   */
  private final MenuCloseConsumer closeCallback;

  /**
   * Border node placed on all empty border slots
   */
  private final MenuNode border;

  /* ----------------------------- CONSTRUCTOR ------------------------------ */

  Menu(MenuBuilder builder) {
    this.title = builder.title;
    this.size = builder.size;

    this.itemMovingAllowed = builder.itemMovingAllowed;

    this.nodes = Int2ObjectMaps.unmodifiable(builder.nodes);

    this.openCallback = builder.openCallback;
    this.closeCallback = builder.closeCallback;
    this.border = builder.border;
  }

  /* ----------------------------- FUNCTIONS ------------------------------ */

  public void open(User user) {
    open(user, Context.EMPTY);
  }

  public void open(User user, Context context) {
    if (openCallback != null) {
      openCallback.onOpen(user, context);
    }

    var inventory = createInventory(user, context);
    user.getPlayer().openInventory(inventory);
  }

  public MenuInventory createInventory(User user, Context context) {
    var inv = new MenuInventory(this, size, title, context);

    if (border != null) {
      var item = border.createItem(user, context);

      if (ItemStacks.notEmpty(item)) {
        Menus.placeBorder(inv, item);
      }
    }

    nodes.int2ObjectEntrySet()
        .forEach(entry -> {
          Slot slot = Slot.of(entry.getIntKey());

          if (slot.getIndex() >= inv.getSize()) {
            throw new IllegalStateException(
                "Slot " + slot + " is too big for inventory size: " +
                    inv.getSize()
            );
          }

          var item = entry.getValue().createItem(user, context);

          if (ItemStacks.isEmpty(item)) {
            return;
          }

          inv.setItem(slot, item);
        });

    return inv;
  }

  public void onMenuClick(InventoryClickEvent event) {
    ClickContext click = new ClickContext(
        (MenuInventory) event.getClickedInventory(),
        (Player) event.getWhoClicked(),
        event.getSlot(),
        event.getCursor(),
        event.getClick()
    );

    Context context = click.getInventory().getContext();
    User user = Users.get(click.getPlayer());

    if (itemMovingAllowed) {
      event.setCancelled(false);
      click.cancelEvent(false);
    } else {
      event.setCancelled(true);
      click.cancelEvent(true);
    }

    if (Cooldown.contains(user, COOLDOWN_CATEGORY)) {
      return;
    }

    try {
      var node = nodes.get(click.getSlot());

      if (node == null) {
        if (border != null
            && Menus.isBorderSlot(Slot.of(click.getSlot()), size)
        ) {
          node = border;
        } else {
          return;
        }
      }

      node.onClick(user, context, click);

      if (click.shouldCooldown()) {
        Cooldown.add(user, COOLDOWN_CATEGORY, click.getCooldownTime());
      }

      if (click.cancelEvent()) {
        event.setCancelled(true);
      }

      if (click.shouldClose()) {
        click.getPlayer().closeInventory();
      } else if (click.shouldReloadMenu()) {
        open(user, context);
      }
    } catch (CommandSyntaxException exc) {
      Exceptions.handleSyntaxException(user, exc);
    } catch (Throwable t) {
      FTC.getLogger().error("Error running menu click!", t);
    }
  }

  public void onMenuClose(InventoryCloseEvent event) {
    MenuInventory inv = (MenuInventory) event.getInventory();
    onClose(inv, Users.get(event.getPlayer().getUniqueId()), event.getReason());
  }

  /* ----------------------------- OVERRIDDEN METHODS ------------------------------ */

  @Override
  public void onClose(FtcInventory inventory, User user, InventoryCloseEvent.Reason reason) {
    if (closeCallback == null) {
      return;
    }

    closeCallback.onClose(inventory, user, reason);
  }

  @Override
  public @NotNull Inventory getInventory() {
    throw new UnsupportedOperationException();
  }
}
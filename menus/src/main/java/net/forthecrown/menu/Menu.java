package net.forthecrown.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.EnumSet;
import lombok.Getter;
import net.forthecrown.Cooldowns;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.slf4j.Logger;

@Getter
public class Menu implements MenuCloseConsumer {

  private static final Logger LOGGER = Loggers.getLogger();

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
   * Inventory index 2 menu node map. Immutable
   */
  private final MenuNode[] nodes;

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

  private final ExternalClickConsumer externalClickCallback;

  private final EnumSet<MenuFlag> flags;

  /* ----------------------------- CONSTRUCTOR ------------------------------ */

  Menu(MenuBuilder builder) {
    this.title = builder.title;
    this.size = builder.size;
    this.externalClickCallback = builder.externalClickCallback;

    this.flags = builder.flags;

    var nodeMap = Int2ObjectMaps.unmodifiable(builder.nodes);
    this.nodes = new MenuNode[size];

    nodeMap.forEach((slot, menuNode) -> {
      if (slot < 0 || slot >= size) {
        throw new IndexOutOfBoundsException(
            "Slot %s out of bounds for inventory size %s. Title = %s"
                .formatted(slot, size, title == null ? null : Text.plain(title))
        );
      }

      nodes[slot] = menuNode;
    });

    this.openCallback = builder.openCallback;
    this.closeCallback = builder.closeCallback;
    this.border = builder.border;
  }

  /* ----------------------------- FUNCTIONS ------------------------------ */

  public boolean hasFlag(MenuFlag flag) {
    return flags.contains(flag);
  }

  public void open(User user) {
    open(user, Context.EMPTY);
  }

  public void open(User user, Context context) {
    var inventory = createInventory(user, context);

    if (openCallback != null) {
      openCallback.onOpen(user, context, inventory);
    }

    user.getPlayer().openInventory(inventory);
  }

  private MenuHolder createMenuHolder(Context ctx) {
    return new MenuHolder(this, ctx);
  }

  public Inventory createInventory(User user, Context context) {
    MenuHolder holder = createMenuHolder(context);
    Inventory inv = holder.getInventory();
    fillInventory(user, context, inv);
    return inv;
  }

  public void fillInventory(User user, Context context, Inventory inv) {
    inv.clear();

    if (border != null) {
      var item = border.createItem(user, context);

      if (ItemStacks.notEmpty(item)) {
        Menus.placeBorder(inv, item);
      }
    }

    for (int i = 0; i < nodes.length; i++) {
      Slot slot = Slot.of(i);
      MenuNode node = nodes[i];

      if (node == null) {
        continue;
      }

      if (slot.getIndex() >= inv.getSize()) {
        throw new IllegalStateException(
            "Slot " + slot + " is too big for inventory size: " +
                inv.getSize()
        );
      }

      var item = node.createItem(user, context);

      if (item == null) {
        continue;
      }

      if (ItemStacks.notEmpty(item) && hasFlag(MenuFlag.PREVENT_ITEM_STACKING)) {
        Menus.makeUnstackable(item);
      }

      inv.setItem(i, item);
    }
  }

  public void onExternalClick(InventoryClickEvent event) {
    ExternalClickContext context = new ExternalClickContext(event);

    if (event.isShiftClick() && !hasFlag(MenuFlag.ALLOW_SHIFT_CLICKING)) {
      context.cancelEvent(true);
    }

    if (externalClickCallback != null) {
      externalClickCallback.onExternalClick(
          context,
          context.getHolder().getContext()
      );
    }

    event.setCancelled(context.cancelEvent());
  }

  public void onMenuClick(InventoryClickEvent event) {
    ClickContext click = new ClickContext(
        event.getClickedInventory(),
        event
    );

    Context context = click.getHolder().getContext();
    User user = Users.get(click.getPlayer());

    boolean cancel = !hasFlag(MenuFlag.ALLOW_ITEM_MOVING);
    event.setCancelled(cancel);
    click.cancelEvent(cancel);

    LOGGER.debug("onMenuClick: cancelled={}, flags={}", cancel, flags);

    Cooldowns cd = Cooldowns.cooldowns();

    if (cd.onCooldown(user.getUniqueId(), COOLDOWN_CATEGORY)) {
      // On cooldown, node can't allow or prevent item moving, thus just
      // flat out disable it here
      event.setCancelled(true);

      LOGGER.debug("onCooldown");
      return;
    }

    try {
      var node = nodes[click.getSlot()];

      if (node == null) {
        if (border != null && Menus.isBorderSlot(Slot.of(click.getSlot()), size)) {
          node = border;
          LOGGER.debug("node = border");
        } else {
          return;
        }
        LOGGER.debug("node = nodes[slot]");
      }

      click.node = node;
      node.onClick(user, context, click);

      LOGGER.debug("postClick: cancelled={}", click.cancelEvent());
    } catch (CommandSyntaxException exc) {
      Exceptions.handleSyntaxException(user, exc);
    } catch (Throwable t) {
      Loggers.getLogger().error("Error running menu click!", t);
    } finally {
      LOGGER.debug("Finally block");

      if (click.cancelEvent()) {
        event.setCancelled(true);
      }

      if (click.shouldCooldown()) {
        cd.cooldown(user.getUniqueId(), COOLDOWN_CATEGORY, click.getCooldownTime());
      }

      if (click.shouldClose()) {
        var player = click.getPlayer();
        var scheduler = click.getPlayer().getScheduler();

        scheduler.runDelayed(
            PluginUtil.getPlugin(),
            scheduledTask -> player.closeInventory(),
            null,
            1
        );

      } else if (click.shouldReloadMenu()) {
        var inventory = click.getInventory();
        fillInventory(user, context, inventory);
      }
    }
  }

  public void onMenuClose(InventoryCloseEvent event) {
    MenuHolder holder = (MenuHolder) event.getInventory().getHolder();
    assert holder != null : "Null holder";

    User user = Users.get((Player) event.getPlayer());

    onClose(event.getInventory(), holder.getContext(), user, event.getReason());
  }

  /* ----------------------------- OVERRIDDEN METHODS ------------------------------ */

  @Override
  public void onClose(Inventory inventory, Context context, User user, Reason reason) {
    if (closeCallback == null) {
      return;
    }

    closeCallback.onClose(inventory, context, user, reason);
  }
}
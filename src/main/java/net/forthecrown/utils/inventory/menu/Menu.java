package net.forthecrown.utils.inventory.menu;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.EnumSet;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
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

  private final ExternalClickConsumer externalClickCallback;

  private final EnumSet<MenuFlag> flags;

  private final Timing clickTiming;
  private final Timing openTiming;
  private final Timing externalClickTiming;
  private final Timing nodeRunTiming;

  /* ----------------------------- CONSTRUCTOR ------------------------------ */

  Menu(MenuBuilder builder) {
    this.title = Objects.requireNonNull(builder.title);
    this.size = builder.size;
    this.externalClickCallback = builder.externalClickCallback;

    this.flags = builder.flags;

    this.nodes = Int2ObjectMaps.unmodifiable(builder.nodes);

    this.openCallback = builder.openCallback;
    this.closeCallback = builder.closeCallback;
    this.border = builder.border;

    String title = Text.plain(this.title);
    var p = FTC.getPlugin();

    this.clickTiming = Timings.of(p, title + " Click");
    this.nodeRunTiming = Timings.of(p, title + " Click.NodeRuntime", clickTiming);

    this.openTiming = Timings.of(p, title + " InvCreate");
    this.externalClickTiming = Timings.of(p, title + " ExternalClick");
  }

  /* ----------------------------- FUNCTIONS ------------------------------ */

  public boolean hasFlag(MenuFlag flag) {
    return flags.contains(flag);
  }

  public void open(User user) {
    open(user, Context.EMPTY);
  }

  public void open(User user, Context context) {
    getOpenTiming().startTiming();

    try {
      var inventory = createInventory(user, context);

      if (openCallback != null) {
        openCallback.onOpen(user, context, inventory);
      }

      user.getPlayer().openInventory(inventory);
    } finally {
      getOpenTiming().stopTiming();
    }
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

          if (hasFlag(MenuFlag.PREVENT_ITEM_STACKING)) {
            Menus.makeUnstackable(item);
          }

          inv.setItem(slot, item);
        });

    return inv;
  }

  public void onExternalClick(InventoryClickEvent event) {
    ExternalClickContext context = new ExternalClickContext(event);

    if (event.isShiftClick() && !hasFlag(MenuFlag.ALLOW_SHIFT_CLICKING)) {
      context.cancelEvent(true);
    }

    if (externalClickCallback != null) {
      externalClickCallback.onShiftClick(
          context,
          context.getMenuInventory().getContext()
      );
    }

    event.setCancelled(context.cancelEvent());
  }

  public void onMenuClick(InventoryClickEvent event) {
    ClickContext click = new ClickContext(
        (MenuInventory) event.getClickedInventory(),
        event
    );

    Context context = click.getInventory().getContext();
    User user = Users.get(click.getPlayer());

    boolean cancel = !hasFlag(MenuFlag.ALLOW_ITEM_MOVING);
    event.setCancelled(cancel);
    click.cancelEvent(cancel);

    if (Cooldown.contains(user, COOLDOWN_CATEGORY)) {
      // On cooldown, node can't allow or prevent item moving, thus just
      // flat out disable it here
      event.setCancelled(true);

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

      click.node = node;

      getNodeRunTiming().startTiming();
      node.onClick(user, context, click);
    } catch (CommandSyntaxException exc) {
      Exceptions.handleSyntaxException(user, exc);
    } catch (Throwable t) {
      Loggers.getLogger().error("Error running menu click!", t);
    } finally {
      getNodeRunTiming().stopTiming();

      if (click.cancelEvent()) {
        event.setCancelled(true);
      }

      if (click.shouldCooldown()) {
        Cooldown.add(user, COOLDOWN_CATEGORY, click.getCooldownTime());
      }

      if (click.shouldClose()) {
        click.getPlayer().closeInventory();
      } else if (click.shouldReloadMenu()) {
        open(user, context);
      }
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
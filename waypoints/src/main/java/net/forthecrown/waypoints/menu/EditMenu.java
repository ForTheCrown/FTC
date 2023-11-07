package net.forthecrown.waypoints.menu;

import static net.forthecrown.waypoints.menu.WaypointMenus.WAYPOINT;

import com.google.common.base.Strings;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult.PartialResult;
import net.forthecrown.command.Exceptions;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.Waypoints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditMenu extends MenuPage {

  static final Slot INFO   = Slot.of(2, 2);
  static final Slot NAME   = Slot.of(4, 2);
  static final Slot REMOVE = Slot.of(6, 2);
  static final Slot LIST   = Slot.of(4, 4);

  public EditMenu() {
    initMenu(Menus.builder(Menus.sizeFromRows(5), "Editing Menu"), false);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.BOOK)
        .setName("Waypoint editing")
        .build();
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    WaypointListPage list = new WaypointListPage(this);
    builder.add(LIST, list);

    builder.add(REMOVE, removeNode());
    builder.add(NAME, nameNode());
    builder.add(INFO, new SubEditMenu(this));
  }

  static void ensureValid(Context context) throws CommandSyntaxException {
    var waypoint = context.get(WAYPOINT);
    if (waypoint == null) {
      throw Exceptions.create("Internal error: No waypoint in GUI context");
    }

    ensureValid(waypoint);
  }

  static void ensureValid(Waypoint waypoint) throws CommandSyntaxException {
    if (waypoint.hasBeenAdded()) {
      return;
    }

    throw Exceptions.create("Waypoint has been removed");
  }

  private MenuNode nameNode() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          Waypoint waypoint = context.getOrThrow(WAYPOINT);

          var builder = ItemStacks.builder(Material.NETHER_STAR);
          builder.setName("&eUpgrade to Named region");

          boolean canUse = Strings.isNullOrEmpty(waypoint.getEffectiveName());

          if (canUse) {
            builder.addLore("&7Purchase a Region Ticket in the webshop!")
                .addLoreRaw(Component.empty())
                .addLore("&7Then click on this item with the renamed Region")
                .addLore("&7Ticket on your cursor to name the waypoint.");
          } else {
            builder
                .addEnchant(Enchantment.BINDING_CURSE, 1)
                .addFlags(ItemFlag.HIDE_ENCHANTS)
                .addLore("&cName already set or set by an external feature");
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          ensureValid(context);

          Waypoint waypoint = context.getOrThrow(WAYPOINT);

          ItemStack cursor = click.getCursorItem();
          if (ItemStacks.isEmpty(cursor)) {
            click.shouldClose(true);

            Component message = Placeholders.renderString(
                "&7Purchase a Region Ticket here: &e${webshoplink}",
                user
            );

            user.sendMessage(message);
            return;
          }

          String effectiveName = waypoint.getEffectiveName();
          if (!Strings.isNullOrEmpty(effectiveName)) {
            throw Exceptions.create("Waypoint already has a name");
          }

          ItemMeta meta = cursor.getItemMeta();
          if (!meta.hasDisplayName()) {
            throw Exceptions.create("Item has no custom name");
          }

          if (!ItemStacks.hasTagElement(meta, "region_ticket")) {
            throw Exceptions.create("Not a region naming ticket!");
          }

          String plainName = Text.plain(meta.displayName());
          validateName(plainName);

          waypoint.set(WaypointProperties.NAME, plainName);
          click.shouldReloadMenu(true);

          user.sendMessage(
              Text.format("Set waypoint name to '&f{0}&r'",
                  NamedTextColor.GRAY, waypoint.displayName()
              )
          );
        })

        .build();
  }

  private void validateName(String name) throws CommandSyntaxException {
    var nameResult = Waypoints.validateWaypointName(name);

    if (nameResult.error().isPresent()) {
      throw Exceptions.create(
          nameResult
              .mapError(s -> "Cannot set name to '" + name + "': " + s)
              .error()
              .map(PartialResult::message)
              .orElse(":)")
      );
    }
  }

  private MenuNode removeNode() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          Waypoint waypoint = context.getOrThrow(WAYPOINT);
          var builder = ItemStacks.builder(Material.BARRIER)
              .setName("&4Remove waypoint");

          if (!waypoint.hasBeenAdded()) {
            builder.addLore("&cWaypoint has already been deleted");
          } else {
            builder.addLore("&7Click to delete");
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          ensureValid(context);

          if (!click.getClickType().isShiftClick()) {
            throw Exceptions.create("Shift-Click to confirm deletion");
          }

          WaypointManager manager = WaypointManager.getInstance();
          manager.removeWaypoint(context.getOrThrow(WAYPOINT));

          click.shouldClose(true);
          click.cancelEvent(true);
        })

        .build();
  }
}

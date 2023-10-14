package net.forthecrown.waypoints.menu;

import static net.forthecrown.waypoints.menu.EditMenu.ensureValid;
import static net.forthecrown.waypoints.menu.WaypointMenus.WAYPOINT;
import static net.kyori.adventure.text.Component.empty;

import java.time.Duration;
import java.util.UUID;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.listeners.PlayerListener;
import net.forthecrown.waypoints.listeners.PlayerListener.MovingWaypoint;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubEditMenu extends MenuPage {

  static final Slot DESC   = Slot.of(2, 2);
  static final Slot MOVE   = Slot.of(4, 2);
  static final Slot PRIV   = Slot.of(6, 2);

  public SubEditMenu(MenuPage parent) {
    super(parent);
    initMenu(Menus.builder(Menus.sizeFromRows(5), "Waypoint Editing"), true);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    var waypoint = context.getOrThrow(WAYPOINT);
    var builder = waypoint.createDisplayItem(user);
    builder.addLoreRaw(empty()).addLore("&6Click to edit waypoint properties");
    return builder.build();
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    builder.add(DESC, descriptionNode());
    builder.add(MOVE, moveNode());
    builder.add(PRIV, privateToggleNode());
  }

  private MenuNode privateToggleNode() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          Waypoint waypoint = context.getOrThrow(WAYPOINT);
          boolean isPrivate
              = !waypoint.get(WaypointProperties.PUBLIC)
              && waypoint.get(WaypointProperties.HIDE_RESIDENTS);

          var builder = ItemStacks.builder(Material.IRON_HELMET);

          builder.addLore("&7Private waypoints require an invite")
              .addLore("&7even if the waypoint is named, and")
              .addLore("&7they don't show how many people live there");

          if (isPrivate) {
            builder
                .addEnchant(Enchantment.BINDING_CURSE, 1)
                .addFlags(ItemFlag.HIDE_ENCHANTS)
                .setName("&eWaypoint state: &6Private")
                .addLore("&7Click to set to public");
          } else {
            builder.setName("&eWaypoint state: &6Public")
                .addLore("&7Click to set to private");
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          ensureValid(context);

          Waypoint waypoint = context.getOrThrow(WAYPOINT);
          boolean isPrivate
              = !waypoint.get(WaypointProperties.PUBLIC)
              && waypoint.get(WaypointProperties.HIDE_RESIDENTS);

          if (isPrivate) {
            waypoint.set(WaypointProperties.PUBLIC, true);
            waypoint.set(WaypointProperties.HIDE_RESIDENTS, false);
          } else {
            waypoint.set(WaypointProperties.PUBLIC, false);
            waypoint.set(WaypointProperties.HIDE_RESIDENTS, true);
          }

          user.sendMessage(
              Text.format("Waypoint set to &e{0}",
                  NamedTextColor.GRAY,
                  isPrivate ? "Public" : "Private"
              )
          );
          click.shouldReloadMenu(true);
        })

        .build();
  }

  private MenuNode moveNode() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          return ItemStacks.builder(Material.IRON_BOOTS)
              .setName("&eMove waypoint")

              .addLore("&7When selected, the menu will be closed")
              .addLore("&7and the next time you use the waypoint creation tool")
              .addLore("&7instead of creating a new waypoint, this one")
              .addLore("&7will be moved instead.")

              .build();
        })

        .setRunnable((user, context, click) -> {
          ensureValid(context);

          Waypoint waypoint = context.getOrThrow(WAYPOINT);
          UUID playerId = user.getUniqueId();

          BukkitTask task = Tasks.runLater(
              () -> PlayerListener.copyingWaypoint.remove(playerId),
              Duration.ofMinutes(10)
          );

          PlayerListener.copyingWaypoint.put(
              user.getUniqueId(),
              new MovingWaypoint(waypoint.getId(), task)
          );

          user.sendMessage(
              Text.format("Use &e/kit waypoint&r to make a waypoint to move this one to",
                  NamedTextColor.GRAY
              )
          );
          click.shouldClose(true);
        })

        .build();
  }

  private MenuNode descriptionNode() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          Waypoint waypoint = context.getOrThrow(WAYPOINT);

          var builder = ItemStacks.builder(Material.WRITABLE_BOOK)
              .setName("&eSet Waypoint description");

          if (waypoint.getDescription() == null) {
            builder.addLore("&7No description set");
          } else {
            builder.addLore("&7Current description:")
                .addLore(waypoint.getDescription().create(user));
          }

          builder.addLore("")
              .addLore("&7Click to set a description");

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          ensureValid(context);

          Waypoint waypoint = context.getOrThrow(WAYPOINT);
          Player player = user.getPlayer();

          DescriptionPrompt prompt = new DescriptionPrompt(user, waypoint);
          Conversation conversation = new Conversation(PluginUtil.getPlugin(), player, prompt);

          prompt.task = Tasks.runLater(
              () -> {
                if (!player.isOnline()) {
                  return;
                }

                player.abandonConversation(conversation);
              },
              Duration.ofMinutes(5)
          );

          player.beginConversation(conversation);
          click.shouldClose(true);
        })

        .build();
  }

}

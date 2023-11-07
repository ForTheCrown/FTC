package net.forthecrown.waypoints.menu;

import static net.forthecrown.waypoints.menu.EditMenu.ensureValid;
import static net.forthecrown.waypoints.menu.WaypointMenus.WAYPOINT;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.forthecrown.command.Exceptions;
import net.forthecrown.menu.ClickContext;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.page.ListPage;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.name.DisplayIntent;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointProperties;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResidentsList extends ListPage<Entry<UUID>> {

  public ResidentsList(MenuPage parent) {
    super(parent, WaypointMenus.PAGE);
    initMenu(Menus.builder(Menus.sizeFromRows(5), "Residents List"), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    builder.add(Slot.ZERO,
        MenuNode.builder()
            .setItem((user, context) -> {
              Waypoint waypoint = context.getOrThrow(WAYPOINT);
              if (waypoint.canEdit(user)) {
                return WaypointMenus.EDIT_MENU.createItem(user, context);
              }

              return WaypointMenus.NO_PERMS.createItem(user, context);
            })
            .setRunnable((user, context, click) -> {
              ensureValid(context);
              Waypoint waypoint = context.getOrThrow(WAYPOINT);

              if (waypoint.canEdit(user)) {
                WaypointMenus.EDIT_MENU.onClick(user, context, click);
                return;
              }

              WaypointMenus.NO_PERMS.onClick(user, context, click);
            })
            .build()
    );

    super.createMenu(builder);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    Waypoint waypoint = context.get(WAYPOINT);

    if (waypoint == null) {
      return null;
    }

    var builder = ItemStacks.builder(Material.PLAYER_HEAD)
        .setName("&eRegion residents");

    if (waypoint.get(WaypointProperties.HIDE_RESIDENTS)) {
      builder.addLore("&cResidents hidden");
    } else {
      int size = waypoint.getResidents().size();

      if (size == 0) {
        builder.addLore("&7Region has no residents :(");
      } else if (size == 1) {
        builder.addLore("&7Region has 1 resident");
      } else {
        builder.addLore("&7Region has " + size + " residents");
      }
    }

    return builder.build();
  }

  @Override
  public void onClick(User user, Context context, ClickContext click) throws CommandSyntaxException {
    ensureValid(context);
    Waypoint waypoint = context.getOrThrow(WAYPOINT);

    if (waypoint.get(WaypointProperties.HIDE_RESIDENTS)) {
      throw Exceptions.create("This waypoint has chosen to hide its residents");
    }

    super.onClick(user, context, click);
  }

  @Override
  protected List<Entry<UUID>> getList(User user, Context context) {
    Waypoint waypoint = context.getOrThrow(WAYPOINT);

    if (waypoint.get(WaypointProperties.HIDE_RESIDENTS)) {
      return List.of();
    }

    var residents = waypoint.getResidents();

    if (residents.isEmpty()) {
      return List.of();
    }

    List<Entry<UUID>> entries = new ArrayList<>(residents.size());
    entries.addAll(residents.object2LongEntrySet());
    entries.sort(Map.Entry.comparingByValue());

    return entries;
  }

  @Override
  protected ItemStack getItem(User user, Entry<UUID> entry, Context context) {
    User resident = Users.get(entry.getKey());
    long movein = entry.getLongValue();

    var builder = ItemStacks.headBuilder()
        .setProfile(resident.getProfile())
        .setName(resident.displayName(user, DisplayIntent.HOVER_TEXT));

    builder.addLore(
        Text.format("Moved in &e{0, time, -timestamp -biggest}&r ago", NamedTextColor.GRAY, movein)
    );

    builder.addLore(
        Text.format("Move in date: &e{0, date}", NamedTextColor.GRAY, movein)
    );

    return builder.build();
  }
}

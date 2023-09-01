package net.forthecrown.waypoints.command;

import com.google.common.base.Strings;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.forthecrown.command.Commands;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.page.Footer;
import net.forthecrown.text.page.PageEntry;
import net.forthecrown.text.page.PagedIterator;
import net.forthecrown.text.page.PageFormat;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointProperties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandListWaypoints extends FtcCommand {

  final PageFormat<Waypoint> waypointPageFormat;

  private static final int DEFAULT_PAGE_SIZE = 10;

  public CommandListWaypoints() {
    super("ListWaypoints");

    setPermission(WPermissions.WAYPOINTS);
    setDescription("Lists named waypoints");
    setAliases("listregions", "regions", "regionlist", "waypointlist");

    this.waypointPageFormat = createPageFormat();

    register();
  }

  private PageFormat<Waypoint> createPageFormat() {
    PageFormat<Waypoint> format = PageFormat.create();
    format.setHeader(Component.text("Named waypoints"));

    format.setFooter(
        Footer.create().setPageButton("/" + getName() + " %s %s")
    );

    PageEntry<Waypoint> entry = PageEntry.create();
    entry.setEntryDisplay((writer, waypoint, viewerIndex, context, it) -> {
      Component displayName = waypoint.displayName();
      writer.write(displayName);
    });

    entry.setIndex((viewerIndex, waypoint, it) -> {
      return Component.text(viewerIndex + ")", NamedTextColor.GRAY);
    });

    format.setEntry(entry);
    return format;
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Lists named waypoints");

    factory.usage("<page: number(1..)> [<page size: number(5..20)>]")
        .addInfo("Lists named waypoints on a specific <page>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> listWaypoints(c.getSource(), 1, DEFAULT_PAGE_SIZE))

        .then(argument("page", IntegerArgumentType.integer(1))
            .executes(c -> {
              int page = c.getArgument("page", Integer.class);
              return listWaypoints(c.getSource(), page, DEFAULT_PAGE_SIZE);
            })

            .then(argument("pageSize" , IntegerArgumentType.integer(5, 20))
                .executes(c -> {
                  int page = c.getArgument("page", Integer.class);
                  int pageSize = c.getArgument("pageSize", Integer.class);
                  return listWaypoints(c.getSource(), page, pageSize);
                })
            )
        );
  }

  private int listWaypoints(CommandSource source, int page, int pageSize)
      throws CommandSyntaxException
  {
    WaypointManager manager = WaypointManager.getInstance();
    List<Waypoint> waypoints = new ObjectArrayList<>(manager.getWaypoints());

    // Remove private waypoints, and ones with no effective names
    waypoints.removeIf(waypoint -> {
      var name = waypoint.getEffectiveName();

      if (Strings.isNullOrEmpty(name)) {
        return true;
      }

      return !waypoint.get(WaypointProperties.PUBLIC);
    });

    Commands.ensurePageValid(--page, pageSize, waypoints.size());
    var it = PagedIterator.of(waypoints, page, pageSize);

    var text = waypointPageFormat.format(it);
    source.sendMessage(text);

    return 0;
  }
}
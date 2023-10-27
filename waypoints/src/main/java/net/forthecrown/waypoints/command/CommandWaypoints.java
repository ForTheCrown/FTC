package net.forthecrown.waypoints.command;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.ParseResult;
import net.forthecrown.command.arguments.chat.MessageSuggestions;
import net.forthecrown.grenadier.CommandContexts;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.CommandFile;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Particles;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.WMessages;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointHomes;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.WaypointProperty;
import net.forthecrown.waypoints.Waypoints;
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.forthecrown.waypoints.util.DelayedWaypointIterator;
import net.forthecrown.waypoints.util.WaypointAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

@CommandFile("waypoints.gcn")
public class CommandWaypoints {

  private final WaypointManager manager;

  public CommandWaypoints(WaypointManager manager) {
    this.manager = manager;
  }

  @VariableInitializer
  void initVars(Map<String, Object> vars) {
    vars.put("admin_permission", WPermissions.WAYPOINTS_ADMIN);
    vars.put("waypoint", WaypointCommands.WAYPOINT);
    vars.put("property", ArgumentTypes.map(WaypointCommands.PROPERTY, Holder::getValue));
    vars.put("timestamp", new TimeStampArgument());

    ArgumentModifier<ParseResult<Waypoint>, Waypoint> toWaypoint = (context, input) -> {
      return input.get(context.getSource(), false);
    };

    vars.put("to_waypoint", toWaypoint);
  }

  public void saveWaypoints(CommandSource source) throws CommandSyntaxException {
    manager.save();
    source.sendSuccess(text("Saved waypoints"));
  }

  public void reloadConfig(CommandSource source) throws CommandSyntaxException {
    manager.getPlugin().reloadConfig();
    source.sendSuccess(text("Reloaded waypoints config"));
  }

  public void reloadWaypoints(CommandSource source) throws CommandSyntaxException {
    manager.load();
    source.sendSuccess(text("Reloaded all waypoints"));
  }

  public void updateAll(CommandSource source) throws CommandSyntaxException {
    DelayedWaypointIterator it = new DelayedWaypointIterator(
        manager.getWaypoints().iterator(),
        new WaypointUpdateAction(source)
    );

    it.schedule();
  }

  public void createAdmin(CommandSource source) {
    createWaypoint(source, WaypointTypes.ADMIN);
  }

  public void createPole(CommandSource source) {
    createWaypoint(source, WaypointTypes.REGION_POLE);
  }

  public void createWaypoint(CommandSource source, WaypointType type) {
    var created = Waypoints.makeWaypoint(type, source.getLocation());
    created.set(WaypointProperties.INVULNERABLE, true);
    created.placeColumn();
    created.update(true);
    Waypoints.placePlatform(created.getWorld(), created.getPlatform());

    var message = WMessages.createdWaypoint(created.getPosition(), created.getType());
    source.sendSuccess(message);
  }

  public void drawBounds(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint
  ) {
    var bounds = waypoint.getBounds();
    Particles.drawBounds(waypoint.getWorld(), bounds,
        Color.fromRGB(waypoint.getType().getNameColor(waypoint).value())
    );
  }

  public void moveWaypoint(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint,
      @Argument(value = "pos", optional = true) Location location
  ) throws CommandSyntaxException {
    if (location == null) {
      location = source.getLocation();
    }

    if (locationMatches(location, waypoint)) {
      throw Exceptions.create("Nothing changed, waypoint is already located there");
    }

    boolean replaceColumn
        = !waypoint.get(WaypointProperties.INVULNERABLE)
        || waypoint.getType() != WaypointTypes.REGION_POLE;

    if (replaceColumn) {
      waypoint.breakColumn();
    }

    waypoint.setPosition(
        Vectors.intFrom(location),
        location.getWorld()
    );

    if (replaceColumn) {
      waypoint.placeColumn();
    }

    Location finalLocation = location;

    makeWaypointMessage(
        waypoint, source,

        like -> {
          return Text.format("Moved &e{0}&r to &6{1, location, -c -w}&r.",
              // '-c': clickable, '-w': display world name in message

              NamedTextColor.GRAY,
              like, finalLocation
          );
        }
    );
  }

  private boolean locationMatches(Location location, Waypoint waypoint) {
    var world = waypoint.getWorld();
    var pos = waypoint.getPosition();

    if (!Objects.equals(world, location.getWorld())) {
      return false;
    }

    return pos.x() == location.getBlockX()
        && pos.y() == location.getBlockY()
        && pos.z() == location.getBlockZ();
  }

  public void showInfo(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    makeWaypointMessage(
        waypoint, source,

        like -> {
          var writer = TextWriters.newWriter();
          writer.viewer(source);
          waypoint.configureWriter(writer);
          writer.field("Waypoint", like);
          waypoint.writeHover(writer);
          return writer.asComponent();
        },

        false
    );
  }

  public void updateWaypoint(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    waypoint.update(true);
    makeWaypointMessage(
        waypoint, source,

        like -> {
          return Text.format("Updating &e{0}&r.", NamedTextColor.GRAY, like);
        }
    );
  }

  public void removeWaypoint(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    makeWaypointMessage(
        waypoint, source,

        like -> {
          return Text.format("Removing waypoint &e{0}&r.", NamedTextColor.GRAY, like);
        }
    );

    manager.removeWaypoint(waypoint);
  }


  public void listResidents(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    var residents = waypoint.getResidents();
    if (residents.isEmpty()) {
      throw Exceptions.create("Waypoint has no residents");
    }

    TextJoiner joiner = TextJoiner.on("\n- ");
    joiner.setColor(NamedTextColor.GRAY);
    joiner.setPrefix(text("Residents:\n- "));

    residents.forEach((uuid, aLong) -> {
      joiner.add(Text.vformat("&e{0, user} &8(moved in: {1, date})", uuid, aLong).create(source));
    });

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          return text()
              .color(NamedTextColor.GRAY)
              .append(displayName.asComponent().color(NamedTextColor.YELLOW))
              .appendSpace()
              .append(joiner.asComponent())
              .build();
        }
    );
  }

  public void addResidents(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint,
      @Argument("players") List<User> users
  ) throws CommandSyntaxException {
    if (users.size() == 1) {
      var user = users.get(0);

      if (waypoint.isResident(user.getUniqueId())) {
        throw Exceptions.create("Nothing changed, user was already a resident");
      }
    }

    int added = 0;

    for (User user : users) {
      if (waypoint.isResident(user.getUniqueId())) {
        continue;
      }

      waypoint.addResident(user.getUniqueId());
      added++;
    }

    int finalAdded = added;

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          if (users.size() == 1) {
            return Text.vformat("&e{0}&r: Added &6{1, user}&r as a resident",
                NamedTextColor.GRAY, displayName, users.get(0)
            );
          }

          return Text.vformat("&e{0}&r: Added &6{1, number} players&r as residents",
              NamedTextColor.GRAY,
              displayName, finalAdded
          );
        }
    );
  }

  public CompletableFuture<Suggestions> suggestResidents(
      SuggestionsBuilder builder,
      CommandSource source,
      @Argument("waypoint") ParseResult<Waypoint> waypoint
  ) throws CommandSyntaxException {
    return Completions.suggest(builder,
        waypoint.get(source, false)
            .getResidents()
            .keySet()
            .stream()
            .map(Users::get)
            .map(User::getName)
    );
  }

  public void removeResident(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint,
      @Argument("resident") User user
  ) throws CommandSyntaxException {
    if (!waypoint.isResident(user.getUniqueId())) {
      throw Exceptions.create("Nothing changed, user was not a resident");
    }

    waypoint.removeResident(user.getUniqueId());

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          return Text.vformat("Evicted &e{0, user}&r from &6{1}&r.",
              NamedTextColor.GRAY,
              user, displayName
          );
        }
    );
  }

  public void setResident(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint,
      @Argument("player") User user,
      @Argument("time") Instant time
  ) throws CommandSyntaxException {
    UUID currentHome = WaypointHomes.getHome(user).map(Waypoint::getId).orElse(null);

    if (Objects.equals(waypoint.getId(), currentHome)) {
      long movein = waypoint.getResidents().getLong(user.getUniqueId());

      if (movein == time.toEpochMilli()) {
        throw Exceptions.NOTHING_CHANGED;
      }
    }

    waypoint.setResident(user.getUniqueId(), time.toEpochMilli());

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          return Text.vformat("&e{0}&r: &6{1, user}&r is now a resident (movein date={2, date})",
              NamedTextColor.GRAY,
              displayName, user, time
          );
        }
    );
  }

  public void clearResidents(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    waypoint.clearResidents();

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          return Text.format("Cleared &e{0}&r residents", NamedTextColor.GRAY, displayName);
        }
    );
  }

  public void listProperties(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          TextJoiner joiner = TextJoiner.on(text("\n- ", NamedTextColor.GRAY));
          joiner.setPrefix(Text.format("{0} properties:\n- ", NamedTextColor.GRAY, displayName));

          Component notSet = text("not-set", NamedTextColor.GRAY);

          for (WaypointProperty<?> prop : WaypointProperties.REGISTRY) {
            Object value = waypoint.get(prop);

            var builder = text()
                .append(text(prop.getName(), NamedTextColor.YELLOW))
                .append(text(": ", NamedTextColor.GRAY));

            if (value == null) {
              builder.append(notSet);
            } else if (Objects.equals(value, prop.getDefaultValue())) {
              builder
                  .append(notSet)
                  .append(text(" (" + prop.getDefaultValue() + ")", NamedTextColor.DARK_GRAY));
            } else {
              builder.append(Text.valueOf(value, source));
            }

            joiner.add(builder.build());
          }

          return joiner;
        }
    );
  }

  CompletableFuture<Suggestions> suggestPropertyValues(
      SuggestionsBuilder builder,
      CommandContext<CommandSource> context,
      @Argument("property") WaypointProperty<Object> property
  ) {
    return property.getParser().listSuggestions(context, builder);
  }

  public void unsetProperty(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint,
      @Argument("property") WaypointProperty<Object> property
  ) throws CommandSyntaxException {
    Object value = waypoint.get(property);

    if (value == null || Objects.equals(value, property.getDefaultValue())) {
      throw Exceptions.create("Nothing changed, property is already unset");
    }

    waypoint.set(property, null);

    makeWaypointMessage(
        waypoint, source,

        like -> {
          return Text.format("&e{0}&r: Unset property &e{1}&r.",
              NamedTextColor.GRAY,
              like, property.getName()
          );
        }
    );
  }

  public void setProperty(
      CommandContext<CommandSource> context,
      @Argument("waypoint") Waypoint waypoint,
      @Argument("property") WaypointProperty<Object> property
  ) throws CommandSyntaxException {
    var range = CommandContexts.getNodeRange(context, "value");
    assert range != null;

    StringReader reader = Readers.create(context.getInput(), range.getStart());
    Object value = property.getParser().parse(reader);

    Commands.ensureCannotRead(reader);
    property.validateValue(waypoint, value);

    waypoint.set(property, value);

    makeWaypointMessage(
        waypoint, context.getSource(),

        displayName -> {
          return Text.format("&e{0}&r: set property &6{1}&r to '&f{2}&r'",
              NamedTextColor.GRAY, displayName,
              property.getName(), value
          );
        }
    );
  }

  public void getProperty(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint,
      @Argument("property") WaypointProperty<?> property
  ) throws CommandSyntaxException {
    Object value = waypoint.get(property);

    if (value == null) {
      throw Exceptions.format("Waypoint has no value for property '{0}'", property.getName());
    }

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          return Text.vformat("&e{0}&r: property &6{1}&r has value '&f{2}&r'",
              NamedTextColor.GRAY,
              displayName, property.getName(), value
          );
        },
        false
    );
  }

  public CompletableFuture<Suggestions> descriptionSuggests(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return MessageSuggestions.get(context, builder, true);
  }

  public void getDescription(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    showDesc(source, waypoint, false);
  }

  public void getRenderedDescription(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    showDesc(source, waypoint, true);
  }

  private void showDesc(CommandSource source, Waypoint waypoint, boolean rendered)
      throws CommandSyntaxException
  {
    if (waypoint.getDescription() == null) {
      throw Exceptions.create("Waypoint has no set description");
    }

    var desc = waypoint.getDescription();
    Component display;

    if (rendered) {
      display = desc.create(source);
    } else {
      display = text(desc.getMessage());
    }

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          return Text.format("&e{0}&r description is '&f{1}&r'",
              NamedTextColor.GRAY, displayName, display
          );
        },
        false
    );
  }

  public void setDescription(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint,
      @Argument("description") String description
  ) throws CommandSyntaxException {
    waypoint.setDescription(PlayerMessage.allFlags(description));

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          return Text.vformat("Set &e{0}&r description to '&f{1}&r'",
              NamedTextColor.GRAY,
              displayName, waypoint.getDescription()
          );
        }
    );
  }

  public void getCreationDate(CommandSource source, @Argument("waypoint") Waypoint waypoint)
      throws CommandSyntaxException
  {
    if (waypoint.getCreationTime() == null) {
      throw Exceptions.create("Waypoint has no creation-date");
    }

    makeWaypointMessage(
        waypoint, source,

        displayName -> {
          return Text.format("&e{0}&r creation-date is &e{1, date}&r.",
              NamedTextColor.GRAY,
              displayName, waypoint.getCreationTime()
          );
        },
        false
    );
  }

  public void setCreationDate(
      CommandSource source,
      @Argument("waypoint") Waypoint waypoint,
      @Argument("time") Instant creationDate
  ) {
    waypoint.setCreationTime(creationDate);

    makeWaypointMessage(
        waypoint,
        source,

        component -> {
          return Text.vformat("Set &e{0}&r creation-date to '&f{1, date}&r'",
              NamedTextColor.GRAY,
              component,
              creationDate
          );
        }
    );
  }

  public void breakColumn(CommandSource source, @Argument("waypoint") Waypoint waypoint) {
    waypoint.breakColumn();

    makeWaypointMessage(
        waypoint, source,

        like -> {
          return Text.format("Broke &e{0}&r column.", NamedTextColor.GRAY, like);
        }
    );
  }

  public void placeColumn(CommandSource source, @Argument("waypoint") Waypoint waypoint) {
    waypoint.placeColumn();

    makeWaypointMessage(
        waypoint, source,

        like -> {
          return Text.format("Placed &e{0}&r column.", NamedTextColor.GRAY, like);
        }
    );
  }

  void placePlatform(CommandSource source, @Argument("waypoint") Waypoint waypoint) {
    Waypoints.placePlatform(waypoint.getWorld(), waypoint.getPlatform());

    makeWaypointMessage(
        waypoint, source,

        like -> {
          return Text.format("Placed &e{0}&r platform.", NamedTextColor.GRAY, like);
        }
    );
  }

  void breakPlatform(CommandSource source, @Argument("waypoint") Waypoint waypoint) {
    Waypoints.clearPlatform(waypoint.getWorld(), waypoint.getPlatform());

    makeWaypointMessage(
        waypoint, source,

        like -> {
          return Text.format("Broke &e{0}&r platform.", NamedTextColor.GRAY, like);
        }
    );
  }

  private void makeWaypointMessage(
      Waypoint waypoint,
      CommandSource source,
      UnaryOperator<ComponentLike> withName
  ) {
    makeWaypointMessage(waypoint, source, withName, true);
  }

  private void makeWaypointMessage(
      Waypoint waypoint,
      CommandSource source,
      UnaryOperator<ComponentLike> withName,
      boolean admin
  ) {
    Component displayName = waypoint.displayName();

    if (displayName == null) {
      var writer = TextWriters.newWriter();
      writer.viewer(source);

      waypoint.configureWriter(writer);
      waypoint.writeHover(writer);

      displayName = text("[Unnamed Waypoint]").hoverEvent(writer.asComponent());
    }

    Component message = Text.valueOf(withName.apply(displayName), source);

    if (admin) {
      source.sendSuccess(message);
    } else {
      source.sendMessage(message);
    }
  }

  private static class WaypointUpdateAction implements WaypointAction {

    final CommandSource source;
    int updated = 0;

    public WaypointUpdateAction(CommandSource source) {
      this.source = source;
    }

    @Override
    public void accept(Waypoint waypoint) {
      if (waypoint.getType() == WaypointTypes.ADMIN) {
        return;
      }

      var platform = waypoint.getPlatform();
      if (platform != null) {
        Waypoints.placePlatform(waypoint.getWorld(), platform);
      }

      if (waypoint.getType() == WaypointTypes.REGION_POLE) {
        var pos = waypoint.getPosition();
        var world = waypoint.getWorld();

        var block = Vectors.getBlock(pos, world);

        clearColumnUp(block.getRelative( 1, 1,  0));
        clearColumnUp(block.getRelative(-1, 1,  0));
        clearColumnUp(block.getRelative( 0, 1,  1));
        clearColumnUp(block.getRelative( 0, 1, -1));
      }

      waypoint.update(true);
      waypoint.set(WaypointProperties.INVULNERABLE, true);

      updated++;
    }

    private void clearColumnUp(Block block) {
      for (int i = 0; i < 3; i++) {
        Block b = block.getRelative(0, i, 0);
        b.setType(Material.AIR, false);
      }
    }

    @Override
    public void onFinish() {
      source.sendSuccess(Text.format("Updated {0, number} waypoints", updated));
    }
  }
}

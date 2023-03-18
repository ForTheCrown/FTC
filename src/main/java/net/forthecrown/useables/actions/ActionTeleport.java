package net.forthecrown.useables.actions;

import static net.forthecrown.commands.admin.CommandTeleportExact.PITCH;
import static net.forthecrown.commands.admin.CommandTeleportExact.YAW;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.admin.CommandTeleportExact;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class  ActionTeleport extends UsageAction {

  private static final ArgumentOption<ParsedPosition> POS_ARG
      = Options.argument(ArgumentTypes.position())
      .addLabel("pos")
      .setDefaultValue(ParsedPosition.IDENTITY)
      .build();

  private static final OptionsArgument ARGS = OptionsArgument.builder()
      .addOptional(YAW)
      .addOptional(CommandTeleportExact.PITCH)
      .addOptional(CommandTeleportExact.WORLD)
      .addOptional(POS_ARG)
      .build();

  // --- TYPE ---
  public static final UsageType<ActionTeleport> TYPE = UsageType.of(ActionTeleport.class)
      .setSuggests(ARGS::listSuggestions)
      .requiresInput(false);

  private final Location location;

  public ActionTeleport(Location location) {
    super(TYPE);
    this.location = location.clone();
  }

  @Override
  public void onUse(Player player, Usable holder) {
    var user = Users.get(player);
    var location = this.location.clone();

    if (location.getWorld() == null) {
      var world = holder.getWorld();

      if (world == null) {
        location.setWorld(player.getWorld());
      } else {
        location.setWorld(world);
      }
    }

    if (!user.canTeleport()) {
      player.teleport(location);
      return;
    }

    user.createTeleport(() -> location, UserTeleport.Type.TELEPORT)
        .setSilent(holder.isSilent())
        .setDelayed(false)
        .start();
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.clickableLocation(location, true);
  }

  @Override
  public @Nullable BinaryTag save() {
    return TagUtil.writeLocation(location);
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor(ConstructType.PARSE)
  public static ActionTeleport parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    if (!reader.canRead()) {
      return new ActionTeleport(source.getLocation());
    }

    ParsedOptions args = ARGS.parse(reader);

    var loc = source.getLocation();
    args.getValue(POS_ARG).apply(loc);

    if (args.has(CommandTeleportExact.WORLD)) {
      loc.setWorld(args.getValue(CommandTeleportExact.WORLD));
    } else {
      loc.setWorld(null);
    }

    if (args.has(YAW)) {
      loc.setYaw(args.getValue(YAW));
    }

    if (args.has(PITCH)) {
      loc.setPitch(args.getValue(PITCH));
    }

    return new ActionTeleport(loc);
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionTeleport load(BinaryTag tag) {
    return new ActionTeleport(TagUtil.readLocation(tag));
  }
}
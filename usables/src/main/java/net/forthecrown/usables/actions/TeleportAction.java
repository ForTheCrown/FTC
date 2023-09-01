package net.forthecrown.usables.actions;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.text.Text;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.forthecrown.usables.objects.ConditionHolder;
import net.forthecrown.usables.objects.Warp;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport.Type;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Locations;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class TeleportAction implements Action {

  private static final ArgumentOption<Float> YAW
      = Options.argument(FloatArgumentType.floatArg(-180, 180))
      .addLabel("yaw")
      .build();

  private static final ArgumentOption<Float> PITCH
      = Options.argument(FloatArgumentType.floatArg(-90, 90))
      .addLabel("pitch")
      .build();

  private static final ArgumentOption<World> WORLD
      = Options.argument(ArgumentTypes.world())
      .addLabel("world")
      .build();

  private static final ArgumentOption<ParsedPosition> POS_ARG
      = Options.argument(ArgumentTypes.position())
      .addLabel("pos")
      .setDefaultValue(ParsedPosition.IDENTITY)
      .build();

  private static final OptionsArgument ARGS = OptionsArgument.builder()
      .addOptional(YAW)
      .addOptional(PITCH)
      .addOptional(WORLD)
      .addOptional(POS_ARG)
      .build();

  public static final UsageType<TeleportAction> TYPE = BuiltType.<TeleportAction>builder()
      .requiresInput(TriState.FALSE)

      .tagLoader(binaryTag -> new TeleportAction(TagUtil.readLocation(binaryTag)))
      .tagSaver(action -> TagUtil.writeLocation(action.location))

      .parser((reader, source) -> {
        Location loc;

        if (reader.canRead()) {
          ParsedOptions options = ARGS.parse(reader).checkAccess(source);

          if (options.has(POS_ARG)) {
            ParsedPosition pos = options.getValue(POS_ARG);
            loc = pos.apply(source);
          } else {
            loc = source.getLocation();
          }

          if (options.has(YAW)) {
            loc.setYaw(options.getValue(YAW));
          }

          if (options.has(PITCH)) {
            loc.setPitch(options.getValue(PITCH));
          }

          if (options.has(WORLD)) {
            loc.setWorld(options.getValue(WORLD));
          } else {
            loc.setWorld(null);
          }
        } else {
          loc = source.getLocation();
        }

        return new TeleportAction(loc);
      })
      .suggester(ARGS::listSuggestions)
      .applicableTo(object -> object.as(Warp.class).isEmpty())
      .build();

  private final Location location;

  public TeleportAction(Location location) {
    this.location = location;
  }

  @Override
  public void onUse(Interaction interaction) {
    var player = interaction.player();
    Location destination = Locations.clone(location);

    if (!destination.isWorldLoaded()) {
      destination.setWorld(player.getWorld());
    }

    User user = Users.get(player);
    user.createTeleport(() -> destination, Type.TELEPORT)
        .setSilent(
            interaction.object()
                .as(ConditionHolder.class)
                .map(ConditionHolder::isSilent)
                .orElse(false)
        )
        .setDelay(null)
        .start();
  }

  @Override
  public UsageType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.clickableLocation(location, true);
  }
}

package net.forthecrown.worldloader.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.text.Text;
import net.forthecrown.worldloader.LoadingArea;
import net.forthecrown.worldloader.WorldLoaderPlugin;
import net.forthecrown.worldloader.WorldLoaderService;
import net.forthecrown.worldloader.WorldLoaderService.WorldLoad;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.slf4j.Logger;

@CommandData("file = command.gcn")
public class CommandWorldLoader {

  public static final ArgumentOption<Integer> RADIUS
      = Options.argument(IntegerArgumentType.integer(1), "radius");

  public static final ArgumentOption<Integer> RADIUS_X
      = Options.argument(IntegerArgumentType.integer(1))
      .addLabel("radius_x")
      .mutuallyExclusiveWith(RADIUS)
      .build();

  public static final ArgumentOption<Integer> RADIUS_Z
      = Options.argument(IntegerArgumentType.integer(1))
      .mutuallyExclusiveWith(RADIUS)
      .requires(RADIUS_X)
      .addLabel("radius_z")
      .build();

  public static final ArgumentOption<ParsedPosition> CENTER
      = Options.argument(ArgumentTypes.blockPosition2d(), "center");

  public static final OptionsArgument ARGS = OptionsArgument.builder()
      .addOptional(CENTER)
      .addOptional(RADIUS)
      .addOptional(RADIUS_X)
      .addOptional(RADIUS_Z)
      .build();

  private static final Logger LOGGER = Loggers.getLogger();

  private final WorldLoaderPlugin plugin;

  public CommandWorldLoader(WorldLoaderPlugin plugin) {
    this.plugin = plugin;
  }

  @VariableInitializer
  void initVars(Map<String, Object> vars) {
    vars.put("options", ARGS);
  }

  void reloadConfig(CommandSource source) {
    plugin.reloadConfig();
    source.sendSuccess(Text.format("Reloaded WorldLoader config"));
  }

  void loadWorld(
      CommandSource source,
      @Argument("world") World world,
      @Argument(value = "options", optional = true) ParsedOptions options
  ) throws CommandSyntaxException {
    WorldLoaderService service = WorldLoaderService.worldLoader();

    if (service.isLoading(world)) {
      throw Exceptions.format("World '{0}' is already being pre-generated", world.getName());
    }

    String worldName = world.getName();
    WorldLoad load = service.loadWorld(world);

    if (options != null) {
      var area = areaFromOptions(options, world, source);
      area.apply(load);
    }

    load.start().whenComplete((world1, throwable) -> {
      if (throwable == null) {
        return;
      }

      LOGGER.error("Error pre-generating world {}", worldName, throwable);
      source.sendFailure(Component.text("Failed to pre-generate world " + worldName));
    });

    source.sendSuccess(Text.format("Started pre-generating world '{0}'", world.getName()));
  }

  LoadingArea areaFromOptions(ParsedOptions options, World world, CommandSource source) {
    int centerX;
    int centerZ;

    if (options.has(CENTER)) {
      Location center = options.getValue(CENTER).apply(source);
      centerX = center.getBlockX();
      centerZ = center.getBlockZ();
    } else {
      centerX = 0;
      centerZ = 0;
    }

    int radiusX;
    int radiusZ;

    if (options.has(RADIUS)) {
      int radius = options.getValue(RADIUS);
      radiusX = radius;
      radiusZ = radius;
    } else if (options.has(RADIUS_X) && options.has(RADIUS_Z)) {
      radiusX = options.getValue(RADIUS_X);
      radiusZ = options.getValue(RADIUS_Z);
    } else {
      double radius = world.getWorldBorder().getSize() / 2;
      radiusX = (int) radius;
      radiusZ = (int) radius;
    }

    return LoadingArea.ofRadius(centerX, centerZ, radiusX, radiusZ);
  }

  void stopLoading(CommandSource source, @Argument("world") World world)
      throws CommandSyntaxException
  {
    WorldLoaderService service = WorldLoaderService.worldLoader();
    boolean stopped = service.stopLoading(world);

    if (!stopped) {
      throw Exceptions.format("World '{0}' was not being pre-generated", world.getName());
    }

    source.sendSuccess(
        Text.format("Stopped world '{0}' from being pre-generated", world.getName())
    );
  }

  void remakeWorld(
      CommandSource source,
      @Argument("world") World world,
      @Argument(value = "seed", optional = true) String seedString
  ) throws CommandSyntaxException {
    WorldLoaderService service = WorldLoaderService.worldLoader();

    Long seed;

    if (seedString == null) {
      seed = null;
    } else if (seedString.equals("-current")) {
      seed = world.getSeed();
    } else {
      try {
        seed = Long.parseLong(seedString);
      } catch (NumberFormatException exc) {
        seed = (long) seedString.hashCode();
      }
    }

    String name = world.getName();
    World remade = service.remakeWorld(world, seed);

    if (remade == null) {
      throw Exceptions.format("Failed to remake world '{0}'", name);
    }

    source.sendSuccess(Text.format("Remade world '{0}'", name));
  }
}

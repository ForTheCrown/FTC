package net.forthecrown.structure.commands;

import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.FlagOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.OptionsArgumentBuilder;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.structure.BlockProcessors;
import net.forthecrown.structure.StructureEntitySpawner;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.commands.CommandFtcStruct.VectorParser;
import net.forthecrown.utils.math.Rotation;
import net.forthecrown.utils.math.Transform;
import net.forthecrown.utils.math.Vectors;
import org.spongepowered.math.vector.Vector3d;

public final class StructureCommands {

  static final ArgumentOption<Rotation> ROT_ARG
      = Options.argument(ArgumentTypes.enumType(Rotation.class))
      .setLabel("rotation")
      .setDefaultValue(Rotation.NONE)
      .build();

  private static final ArgumentOption<Vector3d> OFFSET_ARG
      = Options.argument(new VectorParser())
      .setLabel("offset")
      .setDefaultValue(Vector3d.ZERO)
      .build();

  private static final ArgumentOption<Vector3d> PIVOT_ARG
      = Options.argument(new VectorParser())
      .setLabel("pivot")
      .setDefaultValue(Vector3d.ZERO)
      .build();

  private static final ArgumentOption<ParsedPosition> POS_ARG
      = Options.argument(ArgumentTypes.blockPosition())
      .setLabel("pos")
      .setDefaultValue(ParsedPosition.IDENTITY)
      .build();

  static final FlagOption PLACE_ENTITIES = Options.flag("place_entities");
  static final FlagOption IGNORE_AIR = Options.flag("ignore_air");

  public static void createCommands() {
    var ctx = Commands.createAnnotationContext();
  }

  static OptionsArgumentBuilder createPlacementOptions() {
    OptionsArgumentBuilder builder = OptionsArgument.builder();
    builder.addOptional(OFFSET_ARG);
    builder.addOptional(ROT_ARG);
    builder.addOptional(POS_ARG);
    builder.addOptional(PIVOT_ARG);
    builder.addFlag(PLACE_ENTITIES);
    builder.addFlag(IGNORE_AIR);
    return builder;
  }

  static void configurePlacement(
      StructurePlaceConfig.Builder builder,
      CommandSource source,
      ParsedOptions options
  ) {
    options.getValueOptional(POS_ARG).ifPresent(position -> {
      builder.pos(Vectors.intFrom(position.apply(source)));
    });

    Vector3d pivot = options.getValue(PIVOT_ARG);
    Vector3d offset = options.getValue(OFFSET_ARG);
    Rotation rotation = options.getValue(ROT_ARG);

    Transform transform = builder.transform()
        .withPivot(pivot)
        .addOffset(offset)
        .addRotation(rotation);

    builder.transform(transform);

    if (options.has(PLACE_ENTITIES)) {
      builder.entitySpawner(StructureEntitySpawner.world(source.getWorld()));
    }

    if (options.has(IGNORE_AIR)) {
      builder.addProcessor(BlockProcessors.IGNORE_AIR);
    }
  }
}

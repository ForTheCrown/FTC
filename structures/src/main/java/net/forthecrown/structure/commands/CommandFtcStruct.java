package net.forthecrown.structure.commands;

import static net.forthecrown.grenadier.types.options.ParsedOptions.EMPTY;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.forthecrown.Permissions;
import net.forthecrown.command.DataCommands;
import net.forthecrown.command.DataCommands.DataAccessor;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.FtcKeyArgument;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.BlockFilterArgument;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.FlagOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.registry.Holder;
import net.forthecrown.structure.BlockProcessors;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.StructureFillConfig;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.Structures;
import net.forthecrown.text.Text;
import net.forthecrown.utils.math.Rotation;
import net.forthecrown.utils.math.Transform;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@SuppressWarnings("unchecked")
public class CommandFtcStruct extends FtcCommand {
  /* ----------------------------- CREATION ARGUMENTS ------------------------------ */

  private static final ArgumentOption<List<EntityType>> IGNORE_ENT_ARG
      = Options.argument(ArgumentTypes.array(ArgumentTypes.enumType(EntityType.class)))
      .addLabel("ignore_entities")
      .setDefaultValue(Collections.emptyList())
      .build();

  private static final ArgumentOption<List<BlockFilterArgument.Result>> BLOCK_FILTER
      = Options.argument(ArgumentTypes.array(ArgumentTypes.blockFilter()))
      .addLabel("ignore_blocks")
      .setDefaultValue(Collections.emptyList())
      .build();

  private static final FlagOption INCLUDE_FUNCTIONS
      = Options.flag("include_functions");

  private static final OptionsArgument FILL_ARGS = OptionsArgument.builder()
      .addOptional(BLOCK_FILTER)
      .addOptional(IGNORE_ENT_ARG)
      .addFlag(INCLUDE_FUNCTIONS)
      .build();

  /* ----------------------------- PLACEMENT ARGUMENTS ------------------------------ */

  static final ArgumentOption<Rotation> ROT_ARG
      = Options.argument(ArgumentTypes.enumType(Rotation.class))
      .addLabel("rotation")
      .setDefaultValue(Rotation.NONE)
      .build();

  private static final ArgumentOption<Vector3d> OFFSET_ARG
      = Options.argument(new VectorParser())
      .addLabel("offset")
      .setDefaultValue(Vector3d.ZERO)
      .build();

  private static final ArgumentOption<Vector3d> PIVOT_ARG
      = Options.argument(new VectorParser())
      .addLabel("pivot")
      .setDefaultValue(Vector3d.ZERO)
      .build();

  private static final ArgumentOption<ParsedPosition> POS_ARG
      = Options.argument(ArgumentTypes.blockPosition())
      .addLabel("pos")
      .setDefaultValue(ParsedPosition.IDENTITY)
      .build();

  private static final ArgumentOption<String> PALETTE_ARG
      = Options.argument(new PaletteParser())
      .addLabel("palette")
      .setDefaultValue(BlockStructure.DEFAULT_PALETTE_NAME)
      .build();

  private static final FlagOption PLACE_ENTITIES
      = Options.flag("place_entities");

  private static final FlagOption IGNORE_AIR
      = Options.flag("ignore_air");

  private static final OptionsArgument PLACE_ARGS = OptionsArgument.builder()
      .addOptional(OFFSET_ARG)
      .addOptional(ROT_ARG)
      .addOptional(POS_ARG)
      .addOptional(PIVOT_ARG)
      .addOptional(PALETTE_ARG)

      .addFlag(PLACE_ENTITIES)
      .addFlag(IGNORE_AIR)

      .build();

  private static final DataAccessor HEADER_ACCESSOR = new DataAccessor() {
    @Override
    public CompoundTag getTag(CommandContext<CommandSource> context) {
      Holder<BlockStructure> holder
          = context.getArgument("structure", Holder.class);

      return holder.getValue().getHeader()
          .copy()
          .asCompound();
    }

    @Override
    public void setTag(CommandContext<CommandSource> context, CompoundTag tag) {
      Holder<BlockStructure> holder
          = context.getArgument("structure", Holder.class);

      var header = holder.getValue().getHeader();
      header.clear();
      header.merge(tag);
    }
  };

  /* ----------------------------- CONSTRUCTOR ------------------------------ */

  public CommandFtcStruct() {
    super("FtcStruct");

    setAliases("ftcstructure", "structure", "struct");
    setPermission(Permissions.ADMIN);
    setDescription("Command to place, create and manage FTC structures");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /FtcStruct
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    addCreationArg(factory.withPrefix("create"), "Structure");

    var prefixed = factory.withPrefix("<structure name>");
    var palette = prefixed.withPrefix("palette");
    addCreationArg(palette.withPrefix("add"), "Palette");

    palette.usage("remove <name>")
        .addInfo("Removes a palette with <name> from a <structure>");

    prefixed.usage("place")
        .addInfo("Places the structure where you're standing");

    prefixed.usage("remove", "Deletes a <structure>");

    prefixed.usage("place "
        + "[rotation=<rot>] "
        + "[offset=<x,y,z>] "
        + "[pivot=<x,y,z>] "
        + "[pos=<x,y,z>] "
        + "[place_entities=<true | false>] "
        + "[ignore_air=<true | false>] "
        + "[palette=<name>]"
    )
        .addInfo("Places a <structure> with the parameters")
        .addInfo("")
        .addInfo("-rotation: The rotation applied to the structure")
        .addInfo("-offset: Offset applied to the structure")
        .addInfo("-pivot: The pivot used when placing the structure")
        .addInfo("-pos: The position the structure is placed at")
        .addInfo("-place_entities: Whether to place entities")
        .addInfo("-ignore_air: If true, then existing blocks in the world")
        .addInfo("  won't be overridden if they would be overriden by air")
        .addInfo("-palette: the name of structure palette to place");

    var header = prefixed.withPrefix("header");
    DataCommands.addUsages(header, "Structure", null);
  }

  private void addCreationArg(UsageFactory factory, String name) {
    factory.usage("<name>")
        .addInfo("Creates a %s from your selected", name)
        .addInfo("region and gives it the <name>");

    factory.usage("<name> "
            + "[ignore_blocks=<block tags>] "
            + "[ignore_entities=<entity type list>] "
            + "[include_functions=<true | false>]"
        )
        .addInfo("Creates a %s with the given parameters", name)
        .addInfo("-ignore_blocks: A list of blocks that'll be ignored")
        .addInfo("  when the %s is being scanned", name)
        .addInfo("-ignore_entities: A list of entity types that won't be")
        .addInfo("  included in the %s", name)
        .addInfo("-include_functions: Whether to include structure functions")
        .addInfo("  in the %s or not", name);
  }

  private LiteralArgumentBuilder<CommandSource> headerArgument() {
    var literal = literal("header");
    DataCommands.addArguments(literal, "Structure", HEADER_ACCESSOR);
    return literal;
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("reload").executes(c -> {
          Structures.get().load();

          c.getSource().sendSuccess(Component.text("Reloaded structures plugin"));
          return 0;
        }))

        .then(literal("save").executes(c -> {
          Structures.get().save();

          c.getSource().sendSuccess(Component.text("Saved structures plugin"));
          return 0;
        }))

        .then(literal("create")
            .then(argument("name", Arguments.FTC_KEY)
                .executes(c -> create(c, EMPTY))

                .then(argument("args", FILL_ARGS)
                    .executes(c -> create(c, c.getArgument("args", ParsedOptions.class)))
                )
            )
        )

        .then(argument("structure", Structures.get().getStructureArgument())
            .then(literal("place")
                .executes(c -> place(c, EMPTY))

                .then(argument("args", PLACE_ARGS)
                    .executes(c -> place(c, c.getArgument("args", ParsedOptions.class)))
                )
            )

            .then(literal("palette")
                .then(literal("add")
                    .then(argument("name", StringArgumentType.string())
                        .executes(c -> addPalette(c, EMPTY))

                        .then(argument("args", FILL_ARGS)
                            .executes(c -> {
                              var args = c.getArgument("args", ParsedOptions.class);
                              return addPalette(c, args);
                            })
                        )
                    )
                )

                .then(literal("remove")
                    .then(argument("palette", new PaletteParser())
                        .executes(c -> {
                          Holder<BlockStructure> holder = c.getArgument("structure", Holder.class);
                          var structure = holder.getValue();
                          var palette = c.getArgument("palette", String.class);

                          if (structure.getPalette(palette) == null) {
                            throw Exceptions.format("No palette named '{0}' in '{1}'",
                                palette, holder.getKey()
                            );
                          }

                          structure.getPalettes().remove(palette);

                          c.getSource().sendSuccess(
                              Text.format("Removed palette '{0}' from structure '{1}'",
                                  palette, holder.getKey()
                              )
                          );
                          return 0;
                        })
                    )
                )
            )

            .then(literal("remove")
                .executes(c -> {
                  Holder<BlockStructure> holder = c.getArgument("structure", Holder.class);

                  var structures = Structures.get();
                  structures.getRegistry().remove(holder.getId());

                  c.getSource().sendSuccess(
                      Text.format("Removed structure: '{0}'", holder.getKey())
                  );
                  return 0;
                })
            )

            .then(headerArgument())
        );
  }

  /* ----------------------------- PLACEMENT ------------------------------ */

  private int place(CommandContext<CommandSource> c, ParsedOptions args)
      throws CommandSyntaxException {
    Holder<BlockStructure> holder = c.getArgument("structure", Holder.class);
    BlockStructure structure = holder.getValue();

    StructurePlaceConfig.Builder builder = StructurePlaceConfig.builder()
        .addRotationProcessor()
        .addNonNullProcessor();

    if (args.has(IGNORE_AIR)) {
      builder.addProcessor(BlockProcessors.IGNORE_AIR);
    }

    Location location = c.getSource().getLocation();
    args.getValue(POS_ARG).apply(location);

    Transform transform = Transform.IDENTITY
        .withOffset(args.getValue(OFFSET_ARG))
        .withRotation(args.getValue(ROT_ARG))
        .withPivot(args.getValue(PIVOT_ARG));

    var palette = args.getValue(PALETTE_ARG);

    if (structure.getPalette(palette) == null) {
      throw Exceptions.format("No palette named '{0}' in '{1}'",
          palette, holder.getKey()
      );
    }

    builder
        .transform(transform)
        .world(location.getWorld())
        .pos(Vectors.intFrom(location))
        .paletteName(args.getValue(PALETTE_ARG));

    if (!args.has(PLACE_ENTITIES)) {
      builder.entitySpawner(null);
    }

    StructurePlaceConfig config = builder.build();
    structure.place(config);

    c.getSource().sendSuccess(
        Text.format("Placed structure: '{0}'", holder.getKey())
    );
    return 0;
  }

  /* ----------------------------- PALETTE CREATION ------------------------------ */

  private int addPalette(CommandContext<CommandSource> c, ParsedOptions args)
      throws CommandSyntaxException {
    Holder<BlockStructure> holder = c.getArgument("structure", Holder.class);
    BlockStructure structure = holder.getValue();
    String name = c.getArgument("name", String.class);

    if (structure.getPalette(name) != null) {
      throw Exceptions.format("Structure '{0}' already has a palette named '{1}'",
          holder.getKey(), name
      );
    }

    scan(c, structure, name, args);

    c.getSource().sendSuccess(
        Text.format("Added palette named '{0}' to '{1}'",
            name, holder.getKey()
        )
    );
    return 0;
  }

  /* ----------------------------- CREATION ------------------------------ */

  private int create(CommandContext<CommandSource> c, ParsedOptions args)
      throws CommandSyntaxException {
    String key = c.getArgument("name", String.class);
    var registry = Structures.get().getRegistry();

    if (registry.contains(key)) {
      throw Exceptions.alreadyExists("Structure", key);
    }

    BlockStructure structure = new BlockStructure();
    scan(c, structure, BlockStructure.DEFAULT_PALETTE_NAME, args);

    var holder = registry.register(key, structure);

    c.getSource().sendSuccess(
        Text.format("Created structure named '{0}'", holder.getKey())
    );
    return 0;
  }

  private void scan(CommandContext<CommandSource> c,
                    BlockStructure structure,
                    String palette,
                    ParsedOptions args
  ) throws CommandSyntaxException {
    Player player = c.getSource().asPlayer();
    WorldBounds3i bounds3i = WorldBounds3i.ofPlayerSelection(player);

    if (bounds3i == null) {
      throw Exceptions.NO_REGION_SELECTION;
    }

    // Ensure palette is not a different size from default palette
    if (palette != null
        && !palette.equals(BlockStructure.DEFAULT_PALETTE_NAME)
        && !structure.getDefaultSize().equals(Vector3i.ZERO)
        && !bounds3i.size().equals(structure.getDefaultSize())
    ) {
      throw Exceptions.format(
          "Invalid size: {} for palette!" +
              "\nSelection must have same size as existing structure {}",

          bounds3i.save(),
          structure.getDefaultSize()
      );
    }

    Predicate<Block> blockFilter = block -> block.getType() != Material.STRUCTURE_VOID;
    Predicate<Entity> entityFilter = entity -> entity.getType() != EntityType.PLAYER;

    if (args.has(BLOCK_FILTER)) {
      var list = args.getValue(BLOCK_FILTER);

      blockFilter = blockFilter.and(block -> {
        for (var r : list) {
          if (r.test(block)) {
            return false;
          }
        }

        return true;
      });
    }

    if (args.has(IGNORE_ENT_ARG)) {
      var list = args.getValue(IGNORE_ENT_ARG);
      Set<EntityType> ignoreTypes = new ObjectOpenHashSet<>(list);

      entityFilter = entityFilter.and(entity -> {
        return !ignoreTypes.contains(entity.getType());
      });
    }

    var config = StructureFillConfig.builder()
        .area(bounds3i)
        .blockPredicate(blockFilter)
        .entityPredicate(entityFilter)
        .includeFunctionBlocks(args.has(INCLUDE_FUNCTIONS))
        .paletteName(palette)
        .build();

    structure.fill(config);
  }

  /* ----------------------------- ARGUMENT PARSERS ------------------------------ */

  public static class VectorParser implements ArgumentType<Vector3d> {

    @Override
    public Vector3d parse(StringReader reader) throws CommandSyntaxException {
      double x = reader.readDouble();
      skipSeparator(reader);
      double y = reader.readDouble();
      skipSeparator(reader);
      double z = reader.readDouble();

      return Vector3d.from(x, y, z);
    }

    private void skipSeparator(StringReader reader) {
      reader.skipWhitespace();

      if (reader.canRead() && reader.peek() == ',') {
        reader.skip();
        reader.skipWhitespace();
      }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                              SuggestionsBuilder builder
    ) {
      return Completions.suggest(builder, "0 0 0", "1 1 1", "-1 -1 -1");
    }
  }

  private static class PaletteParser extends FtcKeyArgument {

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                              SuggestionsBuilder builder
    ) {
      Holder<BlockStructure> holder = context.getArgument("structure", Holder.class);
      return Completions.suggest(builder, holder.getValue().getPalettes().keySet());
    }
  }
}
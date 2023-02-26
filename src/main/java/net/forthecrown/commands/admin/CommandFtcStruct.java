package net.forthecrown.commands.admin;

import static net.forthecrown.commands.economy.CommandShopHistory.EMPTY;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.forthecrown.commands.DataCommands;
import net.forthecrown.commands.DataCommands.DataAccessor;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.grenadier.types.block.BlockPredicateArgument;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.structure.BlockProcessors;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.Rotation;
import net.forthecrown.structure.StructureFillConfig;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.Structures;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Transform;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.text.Text;
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

  private static final Argument<List<EntityType>> IGNORE_ENT_ARG
      = Argument.builder("ignore_entities", ArrayArgument.of(EnumArgument.of(EntityType.class)))
      .setDefaultValue(Collections.emptyList())
      .build();

  private static final Argument<List<BlockPredicateArgument.Result>> BLOCK_FILTER
      = Argument.builder("ignore_blocks", ArrayArgument.of(BlockPredicateArgument.blockPredicate()))
      .setDefaultValue(Collections.emptyList())
      .build();

  private static final Argument<Boolean> INCLUDE_FUNCTIONS
      = Argument.builder("include_functions", BoolArgumentType.bool())
      .setDefaultValue(false)
      .build();

  private static final ArgsArgument FILL_ARGS = ArgsArgument.builder()
      .addOptional(BLOCK_FILTER)
      .addOptional(IGNORE_ENT_ARG)
      .addOptional(INCLUDE_FUNCTIONS)
      .build();

  /* ----------------------------- PLACEMENT ARGUMENTS ------------------------------ */

  static final Argument<Rotation> ROT_ARG
      = Argument.builder("rotation", EnumArgument.of(Rotation.class))
      .setAliases("rot", "rotated")
      .setDefaultValue(Rotation.NONE)
      .build();

  private static final Argument<Vector3d> OFFSET_ARG
      = Argument.builder("offset", new VectorParser())
      .setDefaultValue(Vector3d.ZERO)
      .build();

  private static final Argument<Vector3d> PIVOT_ARG
      = Argument.builder("pivot", new VectorParser())
      .setDefaultValue(Vector3d.ZERO)
      .build();

  private static final Argument<Position> POS_ARG
      = Argument.builder("pos", PositionArgument.blockPos())
      .setDefaultValue(Position.SELF)
      .build();

  private static final Argument<Boolean> PLACE_ENTITIES
      = Argument.builder("place_entities", BoolArgumentType.bool())
      .setDefaultValue(true)
      .build();

  private static final Argument<Boolean> IGNORE_AIR
      = Argument.builder("ignore_air", BoolArgumentType.bool())
      .setDefaultValue(false)
      .build();

  private static final Argument<String> PALETTE_ARG
      = Argument.builder("palette", new PaletteParser())
      .setDefaultValue(BlockStructure.DEFAULT_PALETTE_NAME)
      .build();

  private static final ArgsArgument PLACE_ARGS = ArgsArgument.builder()
      .addOptional(OFFSET_ARG)
      .addOptional(ROT_ARG)
      .addOptional(POS_ARG)
      .addOptional(PIVOT_ARG)
      .addOptional(PLACE_ENTITIES)
      .addOptional(PALETTE_ARG)
      .addOptional(IGNORE_AIR)
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
  protected void createCommand(BrigadierCommand command) {
    command
        .then(literal("create")
            .then(argument("name", Arguments.FTC_KEY)
                .executes(c -> create(c, EMPTY))

                .then(argument("args", FILL_ARGS)
                    .executes(c -> create(c, c.getArgument("args", ParsedArgs.class)))
                )
            )
        )

        .then(argument("structure", RegistryArguments.STRUCTURE)
            .then(literal("place")
                .executes(c -> place(c, EMPTY))

                .then(argument("args", PLACE_ARGS)
                    .executes(c -> place(c, c.getArgument("args", ParsedArgs.class)))
                )
            )

            .then(literal("palette")
                .then(literal("add")
                    .then(argument("name", StringArgumentType.string())
                        .executes(c -> addPalette(c, EMPTY))

                        .then(argument("args", FILL_ARGS)
                            .executes(c -> {
                              var args = c.getArgument("args", ParsedArgs.class);
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

                          c.getSource().sendAdmin(
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

                  if (!structures.getRegistry().remove(holder.getId())) {
                    throw Exceptions.REMOVED_NO_DATA;
                  }

                  c.getSource().sendAdmin(
                      Text.format("Removed structure: '{0}'", holder.getKey())
                  );
                  return 0;
                })
            )

            .then(headerArgument())
        );
  }

  /* ----------------------------- PLACEMENT ------------------------------ */

  private int place(CommandContext<CommandSource> c, ParsedArgs args)
      throws CommandSyntaxException {
    Holder<BlockStructure> holder = c.getArgument("structure", Holder.class);
    BlockStructure structure = holder.getValue();

    StructurePlaceConfig.Builder builder = StructurePlaceConfig.builder()
        .addRotationProcessor()
        .addNonNullProcessor()
        .placeEntities(args.get(PLACE_ENTITIES));

    if (args.has(IGNORE_AIR)) {
      builder.addProcessor(BlockProcessors.IGNORE_AIR);
    }

    Location location = c.getSource().getLocation();
    args.get(POS_ARG).apply(location);

    Transform transform = Transform.IDENTITY
        .withOffset(args.get(OFFSET_ARG))
        .withRotation(args.get(ROT_ARG))
        .withPivot(args.get(PIVOT_ARG));

    var palette = args.get(PALETTE_ARG);

    if (structure.getPalette(palette) == null) {
      throw Exceptions.format("No palette named '{0}' in '{1}'",
          palette, holder.getKey()
      );
    }

    builder
        .transform(transform)
        .world(location.getWorld())
        .pos(Vectors.intFrom(location))
        .paletteName(args.get(PALETTE_ARG));

    StructurePlaceConfig config = builder.build();
    structure.place(config);

    c.getSource().sendAdmin(
        Text.format("Placed structure: '{0}'", holder.getKey())
    );
    return 0;
  }

  /* ----------------------------- PALETTE CREATION ------------------------------ */

  private int addPalette(CommandContext<CommandSource> c, ParsedArgs args)
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

    c.getSource().sendAdmin(
        Text.format("Added palette named '{0}' to '{1}'",
            name, holder.getKey()
        )
    );
    return 0;
  }

  /* ----------------------------- CREATION ------------------------------ */

  private int create(CommandContext<CommandSource> c, ParsedArgs args)
      throws CommandSyntaxException {
    String key = c.getArgument("name", String.class);
    var registry = Structures.get().getRegistry();

    if (registry.contains(key)) {
      throw Exceptions.alreadyExists("Structure", key);
    }

    BlockStructure structure = new BlockStructure();
    scan(c, structure, BlockStructure.DEFAULT_PALETTE_NAME, args);

    var holder = registry.register(key, structure);

    c.getSource().sendAdmin(
        Text.format("Created structure named '{0}'", holder.getKey())
    );
    return 0;
  }

  private void scan(CommandContext<CommandSource> c, BlockStructure structure, String palette,
                    ParsedArgs args
  ) throws CommandSyntaxException {
    Player player = c.getSource().asPlayer();

    Region selection = Util.getSelectionSafe(BukkitAdapter.adapt(player));
    WorldBounds3i bounds3i = WorldBounds3i.of(selection);

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
      var list = args.get(BLOCK_FILTER);

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
      var list = args.get(IGNORE_ENT_ARG);
      Set<EntityType> ignoreTypes = new ObjectOpenHashSet<>(list);

      entityFilter = entityFilter.and(entity -> {
        return !ignoreTypes.contains(entity.getType());
      });
    }

    var config = StructureFillConfig.builder()
        .area(bounds3i)
        .blockPredicate(blockFilter)
        .entityPredicate(entityFilter)
        .includeFunctionBlocks(args.get(INCLUDE_FUNCTIONS))
        .paletteName(palette)
        .build();

    structure.fill(config);
  }

  /* ----------------------------- ARGUMENT PARSERS ------------------------------ */

  public static class VectorParser implements ArgumentType<Vector3d> {

    @Override
    public Vector3d parse(StringReader reader) throws CommandSyntaxException {
      double x = reader.readDouble();
      reader.skipWhitespace();
      double y = reader.readDouble();
      reader.skipWhitespace();
      double z = reader.readDouble();

      return Vector3d.from(x, y, z);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                              SuggestionsBuilder builder
    ) {
      return CompletionProvider.suggestMatching(builder, "0 0 0", "1 1 1", "-1 -1 -1");
    }
  }

  private static class PaletteParser implements ArgumentType<String>, VanillaMappedArgument {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
      return Arguments.FTC_KEY.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                              SuggestionsBuilder builder
    ) {
      Holder<BlockStructure> holder = context.getArgument("structure", Holder.class);
      return CompletionProvider.suggestMatching(builder, holder.getValue().getPalettes().keySet());
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
      return Arguments.FTC_KEY.getVanillaArgumentType();
    }
  }
}
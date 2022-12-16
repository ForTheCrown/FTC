package net.forthecrown.commands.admin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.structure.*;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.Util;
import net.forthecrown.structure.Rotation;
import net.forthecrown.utils.math.Transform;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static net.forthecrown.commands.economy.CommandShopHistory.EMPTY;

public class CommandFtcStruct extends FtcCommand {
    /* ----------------------------- CREATION ARGUMENTS ------------------------------ */

    private static final Argument<List<EntityType>>
            IGNORE_ENT_ARG = Argument.builder("ignore_entities", ArrayArgument.of(EnumArgument.of(EntityType.class)))
                    .setDefaultValue(Collections.emptyList())
                    .build();

    private static final Argument<List<BlockPredicateArgument.Result>>
            BLOCK_FILTER = Argument.builder("ignore_blocks", ArrayArgument.of(BlockPredicateArgument.blockPredicate()))
                    .setDefaultValue(Collections.emptyList())
                    .build();

    private static final Argument<Boolean>
            INCLUDE_FUNCTIONS = Argument.builder("include_functions", BoolArgumentType.bool())
                    .setDefaultValue(false)
                    .build();

    private static final ArgsArgument FILL_ARGS = ArgsArgument.builder()
            .addOptional(BLOCK_FILTER)
            .addOptional(IGNORE_ENT_ARG)
            .addOptional(INCLUDE_FUNCTIONS)
            .build();

    /* ----------------------------- PLACEMENT ARGUMENTS ------------------------------ */

    static final Argument<Rotation> ROT_ARG = Argument.builder("rotation", EnumArgument.of(Rotation.class))
            .setAliases("rot", "rotated")
            .setDefaultValue(Rotation.NONE)
            .build();

    private static final Argument<Vector3d> OFFSET_ARG = Argument.builder("offset", new VectorParser())
            .setDefaultValue(Vector3d.ZERO)
            .build();

    private static final Argument<Vector3d> PIVOT_ARG = Argument.builder("pivot", new VectorParser())
            .setDefaultValue(Vector3d.ZERO)
            .build();

    private static final Argument<Position> POS_ARG = Argument.builder("pos", PositionArgument.blockPos())
            .setDefaultValue(Position.SELF)
            .build();

    private static final Argument<Boolean> PLACE_ENTITIES = Argument.builder("place_entities", BoolArgumentType.bool())
            .setDefaultValue(true)
            .build();

    private static final Argument<Boolean> IGNORE_AIR = Argument.builder("ignore_air", BoolArgumentType.bool())
            .setDefaultValue(false)
            .build();

    private static final Argument<String> PALETTE_ARG = Argument.builder("palette", new PaletteParser())
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

    /* ----------------------------- CONSTRUCTOR ------------------------------ */

    public CommandFtcStruct() {
        super("FtcStruct");

        setAliases("ftcstructure", "structure", "struct");
        setPermission(Permissions.ADMIN);
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
                                    BlockStructure structure = holder.getValue();

                                    var structures = Structures.get();

                                    if (!structures.getRegistry().remove(holder.getId())) {
                                        throw Exceptions.REMOVED_NO_DATA;
                                    }

                                    structures.delete(holder);

                                    c.getSource().sendAdmin(
                                            Text.format("Removed structure: '{0}'", holder.getKey())
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("header")
                                .then(literal("view")
                                        .executes(c -> {
                                            Holder<BlockStructure> holder = c.getArgument("structure", Holder.class);
                                            BlockStructure structure = holder.getValue();

                                            c.getSource().sendMessage(
                                                    Text.format("{0}'s header data: {1}",
                                                            holder.getKey(),
                                                            Text.displayTag(structure.getHeader(), true)
                                                    )
                                            );
                                            return 0;
                                        })
                                )

                                .then(literal("put")
                                        .then(argument("nbt", CompoundTagArgument.compoundTag())
                                                .executes(c -> {
                                                    CompoundTag tag = c.getArgument("nbt", CompoundTag.class);
                                                    Holder<BlockStructure> holder = c.getArgument("structure", Holder.class);
                                                    BlockStructure structure = holder.getValue();

                                                    structure.getHeader().merge(tag);

                                                    c.getSource().sendMessage(
                                                            Text.format("Put {0} into {1}'s header",
                                                                    Text.displayTag(tag, false),
                                                                    holder.getKey()
                                                            )
                                                    );
                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("remove")
                                        .then(argument("path", NbtPathArgument.nbtPath())
                                                .executes(c -> {
                                                    NbtPathArgument.NbtPath path = c.getArgument("path", NbtPathArgument.NbtPath.class);
                                                    Holder<BlockStructure> holder = c.getArgument("structure", Holder.class);
                                                    BlockStructure structure = holder.getValue();

                                                    if (path.remove(structure.getHeader()) < 1) {
                                                        throw Exceptions.REMOVED_NO_DATA;
                                                    }

                                                    c.getSource().sendAdmin("Removed tags from structure header");
                                                    return 0;
                                                })
                                        )
                                )
                        )
                );
    }

    /* ----------------------------- PLACEMENT ------------------------------ */

    private int place(CommandContext<CommandSource> c, ParsedArgs args) throws CommandSyntaxException {
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

    private int addPalette(CommandContext<CommandSource> c, ParsedArgs args) throws CommandSyntaxException {
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

    private int create(CommandContext<CommandSource> c, ParsedArgs args) throws CommandSyntaxException {
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

    private void scan(CommandContext<CommandSource> c, BlockStructure structure, String palette, ParsedArgs args) throws CommandSyntaxException {
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
                for (var r: list) {
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
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return CompletionProvider.suggestMatching(builder, "0 0 0", "1 1 1", "-1 -1 -1");
        }
    }

    private static class PaletteParser implements ArgumentType<String>, VanillaMappedArgument {
        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            return Arguments.FTC_KEY.parse(reader);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            Holder<BlockStructure> holder = context.getArgument("structure", Holder.class);
            return CompletionProvider.suggestMatching(builder, holder.getValue().getPalettes().keySet());
        }

        @Override
        public ArgumentType<?> getVanillaArgumentType() {
            return Arguments.FTC_KEY.getVanillaArgumentType();
        }
    }
}
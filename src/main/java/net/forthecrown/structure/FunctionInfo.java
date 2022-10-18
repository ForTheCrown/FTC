package net.forthecrown.structure;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.Readers;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.grenadier.types.block.BlockArgument;
import net.forthecrown.grenadier.types.block.ParsedBlock;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import static net.forthecrown.commands.admin.CommandStructFunction.COMMAND_NAME;

@Getter
@RequiredArgsConstructor
public class FunctionInfo {
    private static final Logger LOGGER = Crown.logger();

    /* ----------------------------- CONSTANTS ------------------------------ */

    public static final String
            FUNCTION_CMD_PREFIX = "function_build";

    private static final Argument<String> FUNC_ARG = Argument.builder(FUNCTION_CMD_PREFIX, Arguments.FTC_KEY)
            .build();

    private static final Argument<CompoundTag> TAG_ARG = Argument.builder("data", CompoundTagArgument.compoundTag())
            .build();

    private static final Argument<ParsedBlock> TURNS_INTO_ARG = Argument.builder("turns_into", BlockArgument.block())
            .build();

    public static final ArgsArgument PARSER = ArgsArgument.builder()
            .addRequired(FUNC_ARG)
            .addOptional(TAG_ARG)
            .addOptional(TURNS_INTO_ARG)
            .build();

    private static final String
            TAG_FUNCTION = "function",
            TAG_FACING = "direction",
            TAG_POSITION = "position",
            TAG_TURNS_INTO = "turns_into",
            TAG_INFO = "value";

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    private final String functionKey;
    private final Direction facing;

    private final BlockData turnsInto;
    private final Vector3i offset;
    private final CompoundTag tag;

    /* ----------------------------- METHODS ------------------------------ */

    public void place(StructurePlaceConfig config) {
        if (turnsInto == null) {
            return;
        }

        Vector3i pos = config.getTransform().apply(offset);
        BlockData data = turnsInto.clone();

        if (data instanceof Rotatable rotatable) {
            Direction dir = this.facing;

            if (dir.isRotatable()) {
                dir = dir.rotate(config.getTransform().getRotation());
            }

            rotatable.setRotation(dir.asBlockFace());
        }

        Vectors.getBlock(pos, config.getWorld())
                .setBlockData(data);
    }

    /* ----------------------------- PARSE ------------------------------ */

    public static FunctionInfo parse(Vector3d origin, CommandBlock cmd) throws CommandSyntaxException {
        String command = cmd.getCommand();
        LOGGER.info("cmd='{}'", command);

        StringReader reader = new StringReader(command);

        if (reader.peek() == '/') {
            reader.skip();
        }

        // If the input does not start with the command name,
        // this will do nothing, as there's nothing to skip over
        Readers.skip(reader, COMMAND_NAME);
        reader.skipWhitespace();

        var arg = PARSER.getArg(FUNCTION_CMD_PREFIX);

        if (arg == null) {
            LOGGER.error("Missing arg '{}' from parser... HOWWWWWW", FUNCTION_CMD_PREFIX);
            LOGGER.error("ArgName='{}'", FUNC_ARG.getName());
        }

        ParsedArgs args = PARSER.parse(reader);
        var direction = Direction.fromBukkit(((Directional) cmd.getBlockData()).getFacing());

        return new FunctionInfo(
                args.get(FUNC_ARG),
                direction,

                args.has(TURNS_INTO_ARG) ? args.get(TURNS_INTO_ARG).getData() : null,
                Vectors.from(cmd.getBlock()).sub(origin.toInt()),
                args.get(TAG_ARG)
        );
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public Tag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString(TAG_FUNCTION, functionKey);
        tag.put(TAG_FACING, TagUtil.writeEnum(facing));
        tag.put(TAG_POSITION, Vectors.writeTag(offset));

        if (turnsInto != null) {
            tag.put(
                    TAG_TURNS_INTO,
                    NbtUtils.writeBlockState(VanillaAccess.getState(turnsInto)))
            ;
        }

        if (this.tag != null && !this.tag.isEmpty()) {
            tag.put(TAG_INFO, this.tag);
        }

        return tag;
    }

    public static FunctionInfo load(Tag t) {
        CompoundTag tag = (CompoundTag) t;
        BlockData data = null;

        if (tag.contains(TAG_TURNS_INTO)) {
            data = NbtUtils.readBlockState(tag.getCompound(TAG_TURNS_INTO))
                    .createCraftBlockData();
        }

        return new FunctionInfo(
                tag.getString(TAG_FUNCTION),
                TagUtil.readEnum(Direction.class, tag.get(TAG_FACING)),
                data,
                Vectors.read3i(tag.get(TAG_POSITION)),
                tag.contains(TAG_INFO) ? tag.getCompound(TAG_INFO) : null
        );
    }
}
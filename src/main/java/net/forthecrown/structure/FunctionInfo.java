package net.forthecrown.structure;

import static net.forthecrown.commands.admin.CommandStructFunction.COMMAND_NAME;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Readers;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.grenadier.types.block.BlockArgument;
import net.forthecrown.grenadier.types.block.ParsedBlock;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Vectors;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@Getter
@With
@RequiredArgsConstructor
public class FunctionInfo {

  private static final Logger LOGGER = Loggers.getLogger();

  /* ----------------------------- CONSTANTS ------------------------------ */

  public static final String
      FUNCTION_CMD_PREFIX = "function_build";

  private static final Argument<String> FUNC_ARG
      = Argument.builder(FUNCTION_CMD_PREFIX, Arguments.FTC_KEY)
      .build();

  private static final Argument<CompoundTag> TAG_ARG
      = Argument.builder("data", Arguments.COMPOUND)
      .build();

  private static final Argument<ParsedBlock> TURNS_INTO_ARG
      = Argument.builder("turns_into", BlockArgument.block())
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

  /**
   * An arbitrary function key defined by staff/systems which use structures
   */
  private final String functionKey;

  /**
   * The direction the command block this function was scanned from was facing
   */
  private final Direction facing;

  /**
   * The block data this function's block will turn into when a structure is being placed, may be
   * null.
   * <p>
   * Note: If a {@link StructurePlaceConfig} has a defined {@link FunctionProcessor} for functions
   * with the same names as this one, this block data will not be placed into the world unless a
   * processor calls the {@link #place(StructurePlaceConfig)} method manually
   */
  private final BlockData turnsInto;

  /**
   * This function's block position relative to the structure
   */
  private final Vector3i offset;

  /**
   * Optional data given to the function
   * <p>
   * Note: will not be applied to block
   */
  private final CompoundTag tag;

  /* ----------------------------- METHODS ------------------------------ */

  public void place(StructurePlaceConfig config) {
    if (turnsInto == null) {
      return;
    }

    Vector3i pos = config.getTransform().apply(offset);
    BlockData data = turnsInto.clone();
    data = VanillaAccess.rotate(data, config.getTransform().getRotation());

    config.getBuffer().setBlock(pos, data, null);
  }

  /* ----------------------------- PARSE ------------------------------ */

  public static FunctionInfo parse(Vector3d origin, CommandBlock cmd)
      throws CommandSyntaxException {
    String command = cmd.getCommand();
    StringReader reader = new StringReader(command);

    if (reader.peek() == '/') {
      reader.skip();
    }

    // If the input does not start with the command name,
    // this will do nothing, as there's nothing to skip over
    Readers.skip(reader, COMMAND_NAME);
    reader.skipWhitespace();

    ParsedArgs args = PARSER.parse(reader);
    var direction = Direction.fromBukkit(
        ((Directional) cmd.getBlockData()).getFacing()
    );

    return new FunctionInfo(
        args.get(FUNC_ARG),
        direction,

        args.has(TURNS_INTO_ARG) ? args.get(TURNS_INTO_ARG).getData() : null,
        Vectors.from(cmd.getBlock()).sub(origin.toInt()),
        args.get(TAG_ARG)
    );
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public BinaryTag save() {
    CompoundTag tag = BinaryTags.compoundTag();

    tag.putString(TAG_FUNCTION, functionKey);
    tag.put(TAG_FACING, TagUtil.writeEnum(facing));
    tag.put(TAG_POSITION, Vectors.writeTag(offset));

    if (turnsInto != null) {
      tag.put(
          TAG_TURNS_INTO,
          TagUtil.writeBlockData(turnsInto)
      );
    }

    if (this.tag != null && !this.tag.isEmpty()) {
      tag.put(TAG_INFO, this.tag);
    }

    return tag;
  }

  public static FunctionInfo load(BinaryTag t) {
    CompoundTag tag = t.asCompound();
    BlockData data = null;

    if (tag.containsKey(TAG_TURNS_INTO)) {
      data = TagUtil.readBlockData(tag.get(TAG_TURNS_INTO));
    }

    return new FunctionInfo(
        tag.getString(TAG_FUNCTION),
        TagUtil.readEnum(Direction.class, tag.get(TAG_FACING)),
        data,
        Vectors.read3i(tag.get(TAG_POSITION)),
        tag.containsKey(TAG_INFO)
            ? tag.get(TAG_INFO).asCompound()
            : null
    );
  }
}
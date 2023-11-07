package net.forthecrown.utils;

import com.mojang.datafixers.DataFixer;
import io.papermc.paper.util.StacktraceDeobfuscator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.TagTranslators;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftCommandBlock;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.spongepowered.math.vector.Vector3i;

/**
 * Utility class for accessing vanilla code
 */
public final class VanillaAccess {
  private VanillaAccess() {}

  public static final Logger LOGGER = Loggers.getLogger();

  /**
   * Gets a vanilla entity object
   *
   * @param entity The bukkit enity
   * @return The vanilla entity
   */
  public static Entity getEntity(org.bukkit.entity.Entity entity) {
    return ((CraftEntity) entity).getHandle();
  }

  /**
   * Gets a vanilla player object
   *
   * @param player The bukkit player
   * @return The vanilla player
   */
  public static ServerPlayer getPlayer(Player player) {
    return (ServerPlayer) getEntity(player);
  }

  /**
   * Gets a vanilla level
   *
   * @param world The bukkit level
   * @return The vanilla level
   */
  public static ServerLevel getLevel(World world) {
    return ((CraftWorld) world).getHandle();
  }

  /**
   * Gets a block state from a bukkit block
   *
   * @param block The bukkit block
   * @return The block state
   */
  public static BlockState getState(Block block) {
    return ((CraftBlock) block).getNMS();
  }

  public static BlockState getState(BlockData data) {
    return ((CraftBlockData) data).getState();
  }

  /**
   * Gets the vanilla tile entity instance
   *
   * @param state The bukkit tile entity
   * @return The vanilla equivalent
   */
  @SuppressWarnings("rawtypes")
  public static BlockEntity getBlockEntity(TileState state) {
    return ((CraftBlockEntityState) state).getTileEntity();
  }

  /**
   * Gets the vanilla server instance
   *
   * @return The vanilla server instance
   */
  public static DedicatedServer getServer() {
    return ((CraftServer) Bukkit.getServer()).getServer();
  }

  /**
   * Gets the player's server-side packet listener
   *
   * @param player The player to get the packet listener of
   * @return The player's packet listener
   */
  public static ServerGamePacketListenerImpl getPacketListener(Player player) {
    return getPlayer(player).connection;
  }

  public static Rotation toVanilla(net.forthecrown.utils.math.Rotation rotation) {
    return switch (rotation) {
      case COUNTERCLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
      case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
      case CLOCKWISE_90 -> Rotation.CLOCKWISE_90;
      default -> Rotation.NONE;
    };
  }

  public static BlockData rotate(BlockData data, net.forthecrown.utils.math.Rotation rotation) {
    var state = getState(data);
    state = state.rotate(toVanilla(rotation));
    return state.createCraftBlockData();
  }

  public static org.bukkit.block.BlockState getState(
      Vector3i pos,
      BlockData data,
      CompoundTag tag
  ) {
    return CraftBlockStates.getBlockState(
        Vectors.toMinecraft(pos),
        VanillaAccess.getState(data),
        tag == null ? null : TagTranslators.COMPOUND.toMinecraft(tag)
    );
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static BlockData merge(BlockData target, BlockData source) {
    BlockState nmsTarget = getState(target);
    BlockState nmsSource = getState(source);

    BlockState result = nmsTarget;

    var properties = nmsSource.getProperties();

    for (var p: properties) {
      Comparable value = nmsSource.getValue(p);
      Property property = p;

      result = result.setValue(property, value);
    }

    return result.createCraftBlockData();
  }

  public static void deobfuscate(Throwable throwable) {
    StacktraceDeobfuscator.INSTANCE.deobfuscateThrowable(throwable);
  }

  /**
   * Unfreezes the given registry
   *
   * @param registry The registry to unfreeze
   */
  public static void unfreeze(MappedRegistry<?> registry) {
    try {
      // The only liability here is the frozen variable
      // It may change with each release, but I also don't
      // believe we could do some kind of
      // for each method (if method == boolean) lookup,
      // as they may just add a different boolean variable
      // to the class for whatever reason
      Field frozen = findFrozenField(registry.getClass());
      frozen.setAccessible(true);

      boolean currentlyFrozen = frozen.getBoolean(registry);

      if (!currentlyFrozen) {
        return;
      }

      frozen.setBoolean(registry, false);

      Field intrusiveMapField = intrusiveHolderField(registry.getClass());
      intrusiveMapField.setAccessible(true);
      intrusiveMapField.set(registry, new IdentityHashMap<>());

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static final String FROZEN_FIELD = "ca";

  private static Field intrusiveHolderField(Class<?> c) throws NoSuchFieldException {
    Field f = c.getDeclaredField("m");
    f.setAccessible(true);
    return f;
  }

  private static Field findFrozenField(Class<?> c) throws NoSuchFieldException {
    for (var f : c.getDeclaredFields()) {
      if (f.getType() == Boolean.TYPE
          || f.getType() == Boolean.class
      ) {
        return f;
      }
    }

    return c.getDeclaredField(FROZEN_FIELD);
  }

  public static void updateNeighbours(Block b) {
    // Thank you once more, Bukkit, for not providing
    // an API for this stuff. Cuz who could ever want
    // to update a block state
    Level world = VanillaAccess.getLevel(b.getWorld());
    CraftBlock craft = (CraftBlock) b;
    BlockPos pos = craft.getPosition();
    BlockState state = craft.getNMS();

    world.updateNeighborsAt(pos, Blocks.LEVER);
    world.updateNeighborsAt(pos.relative(getConnectedDirection(state).getOpposite()), Blocks.LEVER);
  }

  // Copy and pasted method from LeverBlock class, it was protected there
  // So I had to lol
  protected static Direction getConnectedDirection(BlockState state) {
    return switch (state.getValue(LeverBlock.FACE)) {
      case CEILING -> Direction.DOWN;
      case FLOOR -> Direction.UP;
      default -> state.getValue(LeverBlock.FACING);
    };
  }

  public static CommandSender getSender(TileState sender) {
    CommandBlockEntity entity = ((CraftCommandBlock) sender).getTileEntity();
    return entity.getCommandBlock().createCommandSourceStack().getBukkitSender();
  }

  public static DataFixer getFixer() {
    return DataFixers.getDataFixer();
  }

  public static int getDataVersion() {
    return Bukkit.getUnsafe().getDataVersion();
  }

  public static Map<PatternType, String> getPatternFilenames() {
    Map<PatternType, String> typeFilenames = new Object2ObjectOpenHashMap<>();
    Registry<BannerPattern> registry = BuiltInRegistries.BANNER_PATTERN;

    registry.entrySet().forEach(entry -> {
      var fileName = entry.getKey().location().getPath();
      PatternType type = PatternType.getByIdentifier(entry.getValue().getHashname());

      if (type == null) {
        LOGGER.warn(
            "Vanilla pattern type {} doesn't have matching bukkit value",
            fileName
        );
        return;
      }

      typeFilenames.put(type, fileName);
    });

    return typeFilenames;
  }

  public static NamespacedKey getBlockEntityType(BlockData data) {
    var state = getState(data);

    Registry<BlockEntityType<?>> types = BuiltInRegistries.BLOCK_ENTITY_TYPE;

    return types.holders()
        .filter(holder -> holder.value().isValid(state))
        .map(holder -> holder.key().location())
        .map(CraftNamespacedKey::fromMinecraft)
        .findFirst()
        .orElse(null);
  }

  public static boolean isValidTranslationKey(String key, Locale locale) {
    if (Language.getInstance().has(key)) {
      return true;
    }

    return GlobalTranslator.translator().translate(key, locale) != null;
  }
}
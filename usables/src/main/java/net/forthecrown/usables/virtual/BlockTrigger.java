package net.forthecrown.usables.virtual;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.registry.Registry;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3i;

@Getter
public class BlockTrigger implements Trigger {

  private final BlockAction action;

  private final String world;
  private final Vector3i position;

  BlockTrigger(BlockAction action, String world, Vector3i position) {
    this.action = action;
    this.world = world;
    this.position = position;
  }

  public long blockKey() {
    return Block.getBlockKey(position.x(), position.y(), position.z());
  }

  public Optional<Block> getBlock() {
    World world = Bukkit.getWorld(this.world);
    if (world == null) {
      return Optional.empty();
    }
    return Optional.of(Vectors.getBlock(position, world));
  }

  public ObjectType<? extends UsableComponent> getType() {
    return action.type;
  }
}

enum BlockAction {
  ON_BLOCK_BREAK,
  ON_BLOCK_PLACE,
  ON_BLOCK_MINING_START,
  ON_BLOCK_INTERACT,

  ;

  static final Codec<BlockAction> CODEC = FtcCodecs.enumCodec(BlockAction.class);

  final BlockTriggerType type;

  BlockAction() {
    this.type = new BlockTriggerType(this);
  }

  static void registerAll(Registry<ObjectType<? extends Trigger>> r) {
    for (var value : values()) {
      r.register(value.name().toLowerCase(), value.type);
    }
  }
}

class BlockTriggerType implements ObjectType<BlockTrigger> {

  final BlockAction actionType;
  final Codec<BlockTrigger> codec;

  public BlockTriggerType(BlockAction actionType) {
    this.actionType = actionType;

    this.codec = RecordCodecBuilder.create(instance -> {
      return instance
          .group(
              Vectors.V3I_CODEC.fieldOf("pos").forGetter(BlockTrigger::getPosition),
              Codec.STRING.fieldOf("world").forGetter(BlockTrigger::getWorld)
          )
          .apply(instance, (pos, world) -> new BlockTrigger(actionType, world, pos));
    });
  }

  @Override
  public BlockTrigger parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    ParsedPosition position = ArgumentTypes.position().parse(reader);
    Location location = position.apply(source);

    Vector3i pos = Vectors.intFrom(location);
    World world = location.getWorld();

    return new BlockTrigger(actionType, world.getName(), pos);
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return ArgumentTypes.position().listSuggestions(context, builder);
  }

  @Override
  public <S> DataResult<BlockTrigger> load(Dynamic<S> dynamic) {
    return codec.parse(dynamic);
  }

  @Override
  public <S> DataResult<S> save(@NotNull BlockTrigger value, @NotNull DynamicOps<S> ops) {
    return codec.encodeStart(ops, value);
  }
}
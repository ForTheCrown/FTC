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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.registry.Registry;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.utils.EntityRef;
import net.forthecrown.utils.io.FtcCodecs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class EntityTrigger implements Trigger {

  private final EntityAction action;
  private final EntityRef reference;

  public EntityTrigger(EntityAction action, EntityRef reference) {
    this.action = action;
    this.reference = reference;
  }

  public Optional<Entity> getEntity() {
    return Optional.ofNullable(reference.get());
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return action.type;
  }
}

enum EntityAction {
  ON_ENTITY_KILL,
  ON_ENTITY_INTERACT;

  static final Codec<EntityAction> CODEC = FtcCodecs.enumCodec(EntityAction.class);

  final EntityTriggerType type;

  EntityAction() {
    this.type = new EntityTriggerType(this);
  }

  static void registerAll(Registry<ObjectType<? extends Trigger>> r) {
    for (var value : values()) {
      r.register(value.name().toLowerCase(), value.type);
    }
  }
}

record EntityTriggerType(EntityAction action) implements ObjectType<EntityTrigger> {

  @Override
  public EntityTrigger parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    var selector = ArgumentTypes.entity().parse(reader);
    var entity = selector.findEntity(source);

    if (entity instanceof Player) {
      throw Exceptions.create("Player not allowed here");
    }

    var ref = EntityRef.of(entity);
    return new EntityTrigger(action, ref);
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return ArgumentTypes.entity().listSuggestions(context, builder);
  }

  @Override
  public <S> DataResult<EntityTrigger> load(Dynamic<S> dynamic) {
    return EntityRef.CODEC.parse(dynamic).map(entityRef -> new EntityTrigger(action, entityRef));
  }

  @Override
  public <S> DataResult<S> save(@NotNull EntityTrigger value, @NotNull DynamicOps<S> ops) {
    return EntityRef.CODEC.encodeStart(ops, value.getReference());
  }
}

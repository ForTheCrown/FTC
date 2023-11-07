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
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.commands.UsableTriggerCommand.TriggerArgumentType;
import net.forthecrown.usables.trigger.AreaTrigger;
import org.jetbrains.annotations.NotNull;

@Getter
public class RegionTrigger implements Trigger {

  private final RegionAction action;
  private final String regionName;

  public RegionTrigger(RegionAction action, String regionName) {
    this.action = action;
    this.regionName = regionName;
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return action.type;
  }
}

class RegionTriggerType implements ObjectType<RegionTrigger> {

  private final RegionAction action;
  private final TriggerArgumentType argumentType;

  public RegionTriggerType(RegionAction action, TriggerArgumentType type) {
    this.action = action;
    this.argumentType = type;
  }

  @Override
  public RegionTrigger parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    AreaTrigger trigger = argumentType.parse(reader);
    return new RegionTrigger(action, trigger.getName());
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return argumentType.listSuggestions(context, builder);
  }

  @Override
  public <S> DataResult<RegionTrigger> load(Dynamic<S> dynamic) {
    return Codec.STRING.parse(dynamic).map(s -> new RegionTrigger(action, s));
  }

  @Override
  public <S> DataResult<S> save(@NotNull RegionTrigger value, @NotNull DynamicOps<S> ops) {
    return Codec.STRING.encodeStart(ops, value.getRegionName());
  }
}

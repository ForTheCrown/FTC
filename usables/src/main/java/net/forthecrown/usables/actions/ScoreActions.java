package net.forthecrown.usables.actions;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.forthecrown.Loggers;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.registry.Registry;
import net.forthecrown.text.Text;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.ObjectType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ScoreActions {

  static void registerAll(Registry<ObjectType<? extends Action>> r) {
    var values = ScoreOp.values();
    for (ScoreOp value : values) {
      ScoreActionType type = new ScoreActionType(value);
      String key = value.key + "_score";
      r.register(key, type);
    }
  }

}

enum ScoreOp {
  ADD ("add"),
  SET ("set"),
  SUB ("subtract"),
  DIV ("divide"),
  MUL ("multiply");

  final String key;

  ScoreOp(String key) {
    this.key = key;
  }

  static float apply(ScoreOp op, int score, float value) {
    return switch (op) {
      case ADD -> score + value;
      case SUB -> score - value;
      case MUL -> score * value;
      case DIV -> score / value;
      case SET -> value;
    };
  }
}

record ScoreAction(
    String objectiveName,
    ScoreOp op,
    float value,
    String entryName,
    ScoreActionType type
) implements Action {

  private static final Logger LOGGER = Loggers.getLogger();

  @Override
  public void onUse(Interaction interaction) {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    Objective objective = scoreboard.getObjective(objectiveName);

    if (objective == null) {
      LOGGER.warn("No objective named '{}' found, cannot perform action", objectiveName);
      return;
    }

    Score score;

    if (Strings.isNullOrEmpty(entryName)) {
      score = objective.getScore(interaction.player());
    } else {
      score = objective.getScore(entryName);
    }

    int v = score.getScore();
    int modified = (int) ScoreOp.apply(op, v, value);

    score.setScore(modified);
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return type;
  }

  @Override
  public Component displayInfo() {
    var builder = text();
    builder.append(text(objectiveName + ", value=" + Text.NUMBER_FORMAT.format(value)));

    if (!Strings.isNullOrEmpty(entryName)) {
      builder.append(text(", entry='" + entryName + "'"));
    }

    return builder.build();
  }
}

class ScoreActionType implements ObjectType<ScoreAction> {

  static final ArgumentOption<String> entryName
      = Options.argument(StringArgumentType.string(), "entry");

  static final ArgumentOption<String> objectiveName
      = Options.argument(StringArgumentType.string())
      .setSuggester((context, builder) -> Completions.suggestObjectives(builder))
      .setLabel("objective")
      .build();

  static final ArgumentOption<Float> value
      = Options.argument(FloatArgumentType.floatArg(), "value");

  static final OptionsArgument optionsArg = OptionsArgument.builder()
      .addRequired(value)
      .addRequired(objectiveName)
      .addOptional(entryName)
      .build();

  private final ScoreOp op;
  private final Codec<ScoreAction> codec;

  public ScoreActionType(ScoreOp op) {
    this.op = op;

    this.codec = RecordCodecBuilder.create(instance -> {
      return instance
          .group(
              Codec.STRING.fieldOf("objective").forGetter(ScoreAction::objectiveName),
              Codec.FLOAT.fieldOf("value").forGetter(ScoreAction::value),

              Codec.STRING.optionalFieldOf("entry")
                  .forGetter(a -> Optional.ofNullable(a.entryName()))
          )
          .apply(instance, (obj, val, entry) -> {
            return new ScoreAction(obj, op, val, entry.orElse(null), this);
          });
    });
  }

  @Override
  public ScoreAction parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    ParsedOptions options = optionsArg.parse(reader);
    options.checkAccess(source);

    String objective = options.getValue(objectiveName);
    String entry = options.getValue(entryName);
    float v = options.getValue(value);

    return new ScoreAction(objective, op, v, entry, this);
  }

  @Override
  public <S> DataResult<ScoreAction> load(Dynamic<S> dynamic) {
    return codec.parse(dynamic);
  }

  @Override
  public <S> DataResult<S> save(@NotNull ScoreAction value, @NotNull DynamicOps<S> ops) {
    return codec.encodeStart(ops, value);
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return optionsArg.listSuggestions(context, builder);
  }
}
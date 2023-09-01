package net.forthecrown.usables.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.forthecrown.Loggers;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.IntRangeArgument.IntRange;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.text.Text;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableCodecs;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TestScore implements Condition {

  public static final Logger LOGGER = Loggers.getLogger();

  static final ArgumentOption<Objective> OBJ_ARG
      = Options.argument(ArgumentTypes.objective())
      .addLabel("objective")
      .build();

  static final ArgumentOption<IntRange> BOUNDS_ARG
      = Options.argument(ArgumentTypes.intRange())
      .addLabel("bounds")
      .build();

  static final OptionsArgument PARSER = OptionsArgument.builder()
      .addRequired(OBJ_ARG)
      .addRequired(BOUNDS_ARG)
      .build();

  public static final UsageType<TestScore> TYPE = BuiltType.<TestScore>builder()
      .suggester(PARSER::listSuggestions)
      .parser((reader, source) -> {
        ParsedOptions options = PARSER.parse(reader);
        IntRange range = options.getValue(BOUNDS_ARG);
        Objective obj = options.getValue(OBJ_ARG);
        return new TestScore(range, obj.getName());
      })

      .saver((value, ops) -> {
        var builder =  ops.mapBuilder();
        builder.add("objective", ops.createString(value.objectiveName));
        builder.add("bounds", UsableCodecs.INT_RANGE.encodeStart(ops, value.scoreRange));
        return builder.build(ops.empty());
      })

      .loader(dynamic -> {
        var boundsRes = dynamic.get("bounds")
            .decode(UsableCodecs.INT_RANGE)
            .map(Pair::getFirst);

        if (boundsRes.error().isPresent()) {
          return boundsRes.map(intRange -> null);
        }

        IntRange range = boundsRes.result().get();

        return dynamic.get("objective").flatMap(Dynamic::asString)
            .map(s -> new TestScore(range, s));
      })

      .build();

  private final IntRange scoreRange;
  private final String objectiveName;

  public TestScore(IntRange scoreRange, String objectiveName) {
    this.scoreRange = scoreRange;
    this.objectiveName = objectiveName;
  }

  @Override
  public boolean test(Interaction interaction) {
    Objective objective = Bukkit.getScoreboardManager()
        .getMainScoreboard()
        .getObjective(objectiveName);

    if (objective == null) {
      LOGGER.warn("Found no objective named '{}' in usable", objectiveName);
      return false;
    }

    Score score = objective.getScore(interaction.player());
    return scoreRange.contains(score.getScore());
  }

  @Override
  public UsageType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("objective='{0}' values={1}", objectiveName, scoreRange);
  }
}

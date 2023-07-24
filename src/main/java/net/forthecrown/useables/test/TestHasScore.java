package net.forthecrown.useables.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.util.UsageUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.commands.arguments.RangeArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

public class TestHasScore extends UsageTest {

  // --- PARSING ---

  static final ArgumentOption<Objective> OBJ_ARG
      = Options.argument(ArgumentTypes.objective())
      .addLabel("objective")
      .build();

  static final ArgumentOption<Ints> BOUNDS_ARG
      = Options.argument(RangeArgument.intRange())
      .addLabel("bounds")
      .build();

  static final OptionsArgument PARSER = OptionsArgument.builder()
      .addRequired(OBJ_ARG)
      .addRequired(BOUNDS_ARG)
      .build();

  // --- TYPE ---
  public static final UsageType<TestHasScore> TYPE = UsageType.of(TestHasScore.class)
      .setSuggests(PARSER::listSuggestions);

  private final String objective;
  private final MinMaxBounds.Ints bounds;

  public TestHasScore(String objective, MinMaxBounds.Ints bounds) {
    super(TYPE);

    this.objective = objective;
    this.bounds = bounds;
  }

  @Override
  public Component displayInfo() {
    return Text.format("objective='{0}', required='{1}'",
        NamedTextColor.GRAY,

        objective, UsageUtil.boundsDisplay(bounds)
    );
  }

  @Override
  public BinaryTag save() {
    CompoundTag tag = BinaryTags.compoundTag();

    tag.putString("objective", objective);
    tag.put("bounds", UsageUtil.writeBounds(bounds));

    return tag;
  }

  @Override
  public boolean test(Player player, CheckHolder holder) {
    Objective objective = Bukkit.getScoreboardManager()
        .getMainScoreboard()
        .getObjective(this.objective);

    if (objective == null) {
      Loggers.getLogger().warn("Unknown objective in score test usage type in: '{}'",
          this.objective
      );
      return false;
    }

    return bounds.matches(objective.getScore(player).getScore());
  }

  @Override
  public Component getFailMessage(Player player, CheckHolder holder) {
    return Text.format("You don't have the {0} required score.",
        NamedTextColor.GRAY,
        UsageUtil.boundsDisplay(bounds)
    );
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor(ConstructType.PARSE)
  public static TestHasScore parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    var parsed = PARSER.parse(reader);

    return new TestHasScore(
        parsed.getValue(OBJ_ARG).getName(),
        parsed.getValue(BOUNDS_ARG)
    );
  }

  @UsableConstructor(ConstructType.TAG)
  public static TestHasScore load(BinaryTag element) throws CommandSyntaxException {
    var wrapper = element.asCompound();
    Objective objective = ArgumentTypes.objective()
        .parse(new StringReader(wrapper.getString("objective")));

    return new TestHasScore(
        objective.getName(),
        UsageUtil.readBounds(wrapper.get("bounds"))
    );
  }
}
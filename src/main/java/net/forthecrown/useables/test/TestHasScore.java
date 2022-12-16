package net.forthecrown.useables.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FTC;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.useables.*;
import net.forthecrown.useables.util.UsageUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

public class TestHasScore extends UsageTest {

    // --- PARSING ---

    static final Argument<Objective> OBJ_ARG = Argument.builder("objective", ObjectiveArgument.objective())
            .build();

    static final Argument<MinMaxBounds.Ints> BOUNDS_ARG = Argument.builder("bounds", RangeArgument.intRange())
            .build();

    static final ArgsArgument PARSER = ArgsArgument.builder()
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
    public Tag save() {
        CompoundTag tag = new CompoundTag();

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
            FTC.getLogger().warn("Unknown objective in score test usage type in: '{}'",
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
    public static TestHasScore parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        var parsed = PARSER.parse(reader);

        return new TestHasScore(
                parsed.get(OBJ_ARG).getName(),
                parsed.get(BOUNDS_ARG)
        );
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestHasScore load(Tag element) throws CommandSyntaxException {
        var wrapper = ((CompoundTag) element);
        Objective objective = ObjectiveArgument.objective()
                .parse(new StringReader(wrapper.getString("objective")));

        return new TestHasScore(objective.getName(), UsageUtil.readBounds(wrapper.get("bounds")));
    }
}
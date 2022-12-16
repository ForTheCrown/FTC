package net.forthecrown.useables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.FtcKeyed;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.useables.*;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.Nullable;

@Getter
public class ActionScore extends UsageAction {
    private static final Argument<Objective> OBJ_ARG = Argument.builder("objective", ObjectiveArgument.objective())
            .build();

    private static final Argument<Float> VAL_ARG = Argument.builder("amount", FloatArgumentType.floatArg())
            .build();

    private static final ArgsArgument ARGS = ArgsArgument.builder()
            .addRequired(VAL_ARG)
            .addRequired(OBJ_ARG)
            .build();

    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_OBJ = "objective";

    private final float amount;
    private final String objective;
    private final Action action;

    public ActionScore(UsageType<ActionScore> type, float amount, String objective) {
        super(type);

        this.amount = amount;
        this.action = fromType(type);
        this.objective = objective;
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        Objective obj = Bukkit.getScoreboardManager()
                .getMainScoreboard()
                .getObjective(objective);

        if (obj == null) {
            FTC.getLogger().warn("Found unknown objective '{}' in score change action", objective);
            return;
        }

        Score score = obj.getScore(player);
        score.setScore(action.apply(score.getScore(), amount));
    }

    @Override
    public @Nullable Component displayInfo() {
        return Text.format("amount={0, number}", amount);
    }

    @Override
    public @Nullable Tag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_OBJ, objective);
        tag.putFloat(TAG_AMOUNT, amount);

        return tag;
    }

    private static Action fromType(UsageType<ActionScore> type) {
        for (var e: Action.values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }

        throw new IllegalStateException();
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static ActionScore parse(UsageType<ActionScore> type, StringReader reader, CommandSource source) throws CommandSyntaxException {
        var parsed = ARGS.parse(reader);

        return new ActionScore(
                type,
                parsed.get(VAL_ARG),
                parsed.get(OBJ_ARG).getName()
        );
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionScore load(UsageType<ActionScore> type, Tag tag) {
        CompoundTag cTag = (CompoundTag) tag;

        return new ActionScore(
                type,
                cTag.getFloat(TAG_AMOUNT),
                cTag.getString(TAG_OBJ)
        );
    }

    // --- SUB CLASSES ---

    public enum Action implements FtcKeyed {
        ADD {
            @Override
            public int apply(int score, float amount) {
                return (int) (score + amount);
            }
        },

        REMOVE {
            @Override
            public int apply(int score, float amount) {
                return (int) (score - amount);
            }
        },

        DIVIDE {
            @Override
            public int apply(int score, float amount) {
                return (int) (((float) score) / amount);
            }
        },

        MULTIPLY {
            @Override
            public int apply(int score, float amount) {
                return (int) (((float) score) * amount);
            }
        },

        SET {
            @Override
            public int apply(int score, float amount) {
                return (int) amount;
            }
        };

        private final UsageType<ActionScore> type = UsageType.of(ActionScore.class)
                .setSuggests(ARGS::listSuggestions);

        public abstract int apply(int score, float amount);

        @Override
        public String getKey() {
            return name().toLowerCase() + "_score";
        }

        public static void registerAll() {
            for (var e: values()) {
                Registries.USAGE_ACTIONS.register(e.getKey(), e.type);
            }
        }
    }
}
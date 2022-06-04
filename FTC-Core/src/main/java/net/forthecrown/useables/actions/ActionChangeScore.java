package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionChangeScore implements UsageAction<ActionChangeScore.ActionInstance> {
    public static final NamespacedKey
            ADD_KEY = Keys.forthecrown("add_score"),
            REM_KEY = Keys.forthecrown("remove_score"),
            SET_KEY = Keys.forthecrown("set_score"),
            DIV_KEY = Keys.forthecrown("divide_score"),
            MUL_KEY = Keys.forthecrown("multiply_score");

    private final Action action;

    public ActionChangeScore(Action action){
        this.action = action;
    }

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        Objective objective = ObjectiveArgument.objective().parse(reader);
        reader.expect(' ');

        float amount = reader.readFloat();

        return new ActionInstance(action, objective, amount);
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        int amount = json.getInt("amount");
        Objective obj = ObjectiveArgument.objective().parse(new StringReader(json.getString("objective")));

        return new ActionInstance(action, obj, amount);
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        JsonWrapper json = JsonWrapper.empty();

        json.add("amount", value.getAmount());
        json.add("objective", value.getObjective().getName());

        return json.getSource();
    }

    @Override
    public @NotNull Key key() {
        return keyFromAction(action);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return ObjectiveArgument.objective().listSuggestions(context, builder);
    }

    private static Key keyFromAction(Action action) {
        return switch (action) {
            case DECREMENT  -> REM_KEY;
            case SET        -> SET_KEY;
            case INCREMENT  -> ADD_KEY;
            case DIVIDE     -> DIV_KEY;
            case MULTIPLY   -> MUL_KEY;
        };
    }

    public static class ActionInstance implements UsageActionInstance {

        @Getter private final Action action;
        @Getter private final Objective objective;
        @Getter private final float amount;

        public ActionInstance(Action action, Objective objective, float amount) {
            this.action = action;
            this.objective = objective;
            this.amount = amount;
        }

        @Override
        public void onInteract(Player player) {
            Score score = objective.getScore(player.getName());

            int newScore = score.isScoreSet() ? score.getScore() : 0;
            newScore = action.apply(newScore, amount);

            score.setScore(newScore);
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "obj=" + objective.getName() + ", amount=" + String.format("%.02f", amount) + '}';
        }

        @Override
        public @NotNull Key typeKey() {
            return keyFromAction(action);
        }
    }

    public interface ScoreOperator {
        int apply(int score, float amount);
    }

    public enum Action {
        INCREMENT ((score, amount) -> (int) (score + amount)),
        DECREMENT ((score, amount) -> (int) (score - amount)),
        DIVIDE ((score, amount) ->    (int) (score / amount)),
        MULTIPLY (((score, amount) -> (int) (score * amount))),
        SET ((score, amount) ->       (int) amount);

        public final ScoreOperator operator;

        Action(ScoreOperator operator){
            this.operator = operator;
        }

        public int apply(int score, float amount) {
            return operator.apply(score, amount);
        }
    }
}
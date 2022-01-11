package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionChangeScore implements UsageAction<ActionChangeScore.ActionInstance> {
    public static final Key ADD_KEY = Keys.forthecrown("add_score");
    public static final Key REMOVE_KEY = Keys.forthecrown("remove_score");
    public static final Key SET_KEY = Keys.forthecrown("set_score");

    private final Action action;

    public ActionChangeScore(Action action){
        this.action = action;
    }

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        Objective objective = ObjectiveArgument.objective().parse(reader);
        reader.expect(' ');

        int amount = reader.readInt();

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
        return switch (action) {
            case DECREMENT -> REMOVE_KEY;
            case SET -> SET_KEY;
            case INCREMENT -> ADD_KEY;
        };
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return ObjectiveArgument.objective().listSuggestions(context, builder);
    }

    public static class ActionInstance implements UsageActionInstance {

        private final Action action;
        private final Objective objective;
        private final int amount;

        public ActionInstance(Action action, Objective objective, int amount) {
            this.action = action;
            this.objective = objective;
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public Objective getObjective() {
            return objective;
        }

        public Action getAction() {
            return action;
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
            return typeKey().asString() + '{' + "obj=" + objective.getName() + ", amount=" + amount + '}';
        }

        @Override
        public @NotNull Key typeKey() {
            return switch (action) {
                case DECREMENT -> REMOVE_KEY;
                case SET -> SET_KEY;
                case INCREMENT -> ADD_KEY;
            };
        }
    }

    public interface IntBiOperator {
        int apply(int score, int amount);
    }

    public enum Action {
        INCREMENT (Integer::sum),
        DECREMENT ((score, amount) -> score - amount),
        SET ((score, amount) -> amount);

        public final IntBiOperator operator;

        Action(IntBiOperator operator){
            this.operator = operator;
        }

        public int apply(int score, int amount){
            return operator.apply(score, amount);
        }
    }
}
package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.serializer.JsonBuf;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionChangeScore implements UsageAction<ActionChangeScore.ActionInstance> {
    public static final Key ADD_KEY = Key.key(ForTheCrown.inst(), "add_score");
    public static final Key REMOVE_KEY = Key.key(ForTheCrown.inst(), "remove_score");
    public static final Key SET_KEY = Key.key(ForTheCrown.inst(), "set_score");

    private final Action action;
    //private Objective objective;
    //private int amount;

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
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        int amount = json.getInt("amount");
        Objective obj = ObjectiveArgument.objective().parse(new StringReader(json.getString("objective")));

        return new ActionInstance(action, obj, amount);
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        JsonBuf json = JsonBuf.empty();

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

    /*@Override
    public void onInteract(Player player) {
        Score score = objective.getScore(player.getName());

        int newScore = score.isScoreSet() ? score.getScore() : 0;
        newScore = action.apply(newScore, amount);

        score.setScore(newScore);
    }

    @Override
    public String asString() {
        return key().asString() + "{" +
                "amount=" + amount +
                ",objective=" + objective.getName() +
                "}";
    }

    @Override
    public @NonNull Key key() {
        return switch (action) {
            case DECREMENT -> REMOVE_KEY;
            case SET -> SET_KEY;
            case INCREMENT -> ADD_KEY;
        };
    }

    public Action getAction() {
        return action;
    }

    public Objective getObjective() {
        return objective;
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }*/

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


    /*@Override
    public void parse(JsonElement element) throws CommandSyntaxException {
        JsonObject json = element.getAsJsonObject();

        amount = json.get("amount").getAsInt();

        String objName = json.get("objective").getAsString();

        this.objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objName);
        if(objective == null) throw ObjectiveArgumentImpl.UNKNOWN_OBJECTIVE.create(objName);
    }

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        String obj = reader.readUnquotedString();
        if(!reader.canRead()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);
        reader.skipWhitespace();

        int amount = reader.readInt();

        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(obj);
        if(objective == null) throw ObjectiveArgumentImpl.UNKNOWN_OBJECTIVE.createWithContext(reader, obj);

        this.objective = objective;
        this.amount = amount;
    }

        @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.add("objective", new JsonPrimitive(objective.getName()));
        json.add("amount", new JsonPrimitive(amount));

        return json;
    }*/
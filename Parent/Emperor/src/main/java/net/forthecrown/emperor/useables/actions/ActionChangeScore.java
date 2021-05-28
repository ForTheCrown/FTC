package net.forthecrown.emperor.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.useables.UsageAction;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.types.scoreboard.ObjectiveArgumentImpl;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.BiFunction;

public class ActionChangeScore implements UsageAction {
    public static final Key ADD_KEY = Key.key(CrownCore.getNamespace(), "add_score");
    public static final Key REMOVE_KEY = Key.key(CrownCore.getNamespace(), "remove_score");
    public static final Key SET_KEY = Key.key(CrownCore.getNamespace(), "set_score");

    private final Action action;
    private Objective objective;
    private int amount;

    public ActionChangeScore(Action action){
        this.action = action;
    }

    @Override
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
    public void onInteract(Player player) {
        Score score = objective.getScore(player.getName());

        int newScore = score.isScoreSet() ? score.getScore() : 0;
        newScore = action.apply(newScore, amount);

        score.setScore(newScore);
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{" +
                "action=" + action.name().toLowerCase() +
                ",amount=" + amount +
                ",objective=" + objective.getName() +
                "}";
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.add("objective", new JsonPrimitive(objective.getName()));
        json.add("amount", new JsonPrimitive(amount));

        return json;
    }

    @Override
    public @NonNull Key key() {
        switch (action){
            case DECREMENT: return REMOVE_KEY;
            case SET: return SET_KEY;
            case INCREMENT: return ADD_KEY;

            default: throw new IllegalStateException("Unexpected value: " + action);
        }
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
    }

    public interface IntBiOperator extends BiFunction<Integer, Integer, Integer> {
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

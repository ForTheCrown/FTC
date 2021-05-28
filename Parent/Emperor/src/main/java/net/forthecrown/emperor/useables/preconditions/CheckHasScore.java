package net.forthecrown.emperor.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.types.scoreboard.ObjectiveArgumentImpl;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

public class CheckHasScore implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.getNamespace(), "has_score");

    private Objective objective;
    private int amount;

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
    public void parse(JsonElement element) throws CommandSyntaxException {
        JsonObject json = element.getAsJsonObject();
        objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(json.get("objective").getAsString());
        amount = json.get("amount").getAsInt();
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{objective=" + objective.getName() + ",amount=" + amount + "}";
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You aren't able to use this");
    }

    @Override
    public boolean test(Player player) {
        Score score = objective.getScore(player.getName());

        return score.isScoreSet() && score.getScore() >= amount;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.add("objective", new JsonPrimitive(objective.getName()));
        json.add("amount", new JsonPrimitive(amount));

        return json;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletionProvider.suggestObjectives(builder);
    }

    @Override
    public @NonNull Key key() {
        return KEY;
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
}

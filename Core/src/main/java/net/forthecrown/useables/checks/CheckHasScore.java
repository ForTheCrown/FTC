package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;

public class CheckHasScore implements UsageCheck<CheckHasScore.CheckInstance> {
    private static final Key KEY = Crown.coreKey("has_score");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        Objective objective = ObjectiveArgument.objective().parse(reader);

        reader.expect(' ');
        int amount = reader.readInt();

        return new CheckInstance(objective, amount);
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        Objective objective = ObjectiveArgument.objective().parse(new StringReader(json.getString("objective")));
        int amount = json.getInt("amount");

        return new CheckInstance(objective, amount);
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        JsonWrapper json = JsonWrapper.empty();

        json.add("objective", value.getObjective().getName());
        json.add("amount", value.getAmount());

        return json.getSource();
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class CheckInstance implements UsageCheckInstance {

        private final Objective objective;
        private final int amount;

        CheckInstance(Objective objective, int amount) {
            this.objective = objective;
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public Objective getObjective() {
            return objective;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "objective=" + objective.getName() + ", amount=" + amount + '}';
        }

        @Override
        public Component failMessage(Player player) {
            return Component.text()
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("You need at least "))
                    .append(Component.text(amount))
                    .append(Component.space())
                    .append(objective.displayName())
                    .append(Component.text(" score."))
                    .build();
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }

        @Override
        public boolean test(Player player) {
            Score score = objective.getScore(player.getName());

            return score.isScoreSet() && score.getScore() >= amount;
        }
    }
}

package net.forthecrown.economy.guilds;

import com.google.gson.JsonElement;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * A history record of a vote.
 * <p></p>
 * If pro || against || abstentions == -1,
 * the vote did not finish and was ended
 * abruptly.
 */
public record VoteHistoryEntry(
        long voteStart, long voteEnd,
        int pro, int against, int abstentions,
        Component topic
) implements JsonSerializable, ComponentLike {

    public static VoteHistoryEntry ofFinished(VoteState state, VoteCount count) {
        return new VoteHistoryEntry(
                state.getStarted(), state.getEnds(),
                count.getPro(), count.getAgainst(), count.getAbstentions(),
                state.getTopic().displayText(state.getData())
        );
    }

    public static VoteHistoryEntry ofUnfinished(VoteState state) {
        return new VoteHistoryEntry(
                state.getStarted(), System.currentTimeMillis(),
                -1, -1, -1,
                state.getTopic().displayText(state.getData())
        );
    }

    public static VoteHistoryEntry ofJson(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new VoteHistoryEntry(
                json.getLong("started"),
                json.getLong("ended"),

                json.getInt("for", -1),
                json.getInt("against", -1),
                json.getInt("abstentions", -1),

                json.getComponent("topic")
        );
    }

    public boolean endedInstant() {
        return pro == -1;
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("started", voteStart);
        json.add("ended", voteEnd);

        if(pro != -1) {
            json.add("for", pro);
            json.add("against", against);
            json.add("abstentions", abstentions);
        }

        json.addComponent("topic", topic);

        return json.getSource();
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.translatable("guilds.history.format" + (endedInstant() ? ".instantEnd" : ""),
                NamedTextColor.GRAY,
                topic.color(NamedTextColor.GOLD),
                FtcFormatter.formatDate(voteStart).color(NamedTextColor.YELLOW),
                FtcFormatter.formatDate(voteEnd).color(NamedTextColor.YELLOW),
                BallotBox.voteLine(pro, against, abstentions)
        );
    }
}
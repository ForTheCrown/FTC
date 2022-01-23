package net.forthecrown.economy.guilds.topics;

import com.google.gson.JsonElement;
import net.forthecrown.economy.guilds.DelayedVoteTask;
import net.forthecrown.economy.guilds.TradeGuild;
import net.forthecrown.economy.guilds.VoteCount;
import net.forthecrown.economy.guilds.screen.InvPosProvider;
import net.forthecrown.economy.guilds.screen.TopicSelectionScreen;
import net.forthecrown.economy.houses.House;
import net.forthecrown.economy.houses.VoteModifier;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A vote topic is a topic that can be voted upon.
 * Fucking amazing description, I know lmao.
 *
 * @param <T> The type of data class this topic uses
 */
public interface VoteTopic<T extends VoteData> extends Keyed {
    /**
     * Called when a vote on this topic ends
     * @param data The vote data
     * @param count The final count of the vote
     * @return A delayed task to be run later, null, if no task is to be run
     */
    @Nullable
    DelayedVoteTask onEnd(T data, VoteCount count);

    default Component extraVictoryText(T data) {
        return null;
    }

    /**
     * Serialize the data into JSON
     * @param data The data to serialize
     * @return A serialized representation of the data
     */
    JsonElement serialize(T data);

    /**
     * Deserialize vote data
     * @param element The element to read from
     * @return The deserialized data
     */
    T deserialize(JsonElement element);

    /**
     * Displays the most basic info about the
     * topic, like "Evict {USER}"
     *
     * @param data The data to display
     * @return The display text
     */
    Component displayText(T data);

    /**
     * Creates the item {@link TopicSelectionScreen} will use
     * to display this topic as a selection.
     *
     * @return The topic's selection item
     */
    ItemStack createSelectionScreenItem();

    /**
     * Creates inventory options for this topic
     * @param provider The position provider
     * @return Inventory options
     */
    Collection<? extends CordedInventoryOption> getClickOptions(InvPosProvider provider);

    default Component screenTitle() {
        return Component.text("Select an option");
    }

    default int screenSize() {
        return 54;
    }

    /**
     * Run the given delayed task
     * @param task The task to run
     */
    void runTask(DelayedVoteTask task);

    /**
     * Create a modifier to influence how a
     * house will vote when it comes to this
     * topic
     *
     * @param h The house that's voting
     * @param data The data of the vote
     *
     * @return A modifier which will influence
     *         how the house votes
     */
    VoteModifier createModifier(House h, T data);

    /**
     * Checks whether the vote should continue or end
     * imminently
     * <p></p>
     * You may want to end a vote, if, for example,
     * in the {@link EvictTopic} the eviction target
     * unclaims their shop, then there's no point in
     * continuing the vote.
     *
     * @param guild The guild
     * @param markets The market manager
     * @param data The vote data
     *
     * @return Whether the vote should continue
     */
    boolean shouldContinueVote(TradeGuild guild, Markets markets, T data);
}

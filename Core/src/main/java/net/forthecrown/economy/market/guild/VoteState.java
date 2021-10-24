package net.forthecrown.economy.market.guild;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.houses.Dynasty;
import net.forthecrown.economy.market.guild.topics.VoteTopic;
import net.forthecrown.economy.market.guild.topics.VoteTopicType;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;

import java.util.Date;
import java.util.UUID;

/**
 * The state of a vote in the guild
 */
public class VoteState implements JsonSerializable, JsonDeserializable {
    private final TradersGuild guild;

    private final ObjectSet<UUID> pro = new ObjectOpenHashSet<>();
    private final ObjectSet<UUID> against = new ObjectOpenHashSet<>();
    private final ObjectSet<Dynasty> proDynasties = new ObjectOpenHashSet<>();
    private final ObjectSet<Dynasty> againstDynasties = new ObjectOpenHashSet<>();

    VoteTopic topic;
    Date started;

    public VoteState(TradersGuild guild) {
        this.guild = guild;
    }

    /**
     * Calls the {@link GuildVoter#vote(VoteState)} function
     * @param voter The voter
     */
    public void vote(GuildVoter voter) {
        voter.vote(this);
    }

    /**
     * Gets the topic's current topic
     * @return The current topic
     */
    public VoteTopic getTopic() {
        return topic;
    }

    /**
     * Gets the date the vote started
     * @return The vote's starting date
     */
    public Date getStarted() {
        return started;
    }

    private void checkShouldEnd() {
        if(!allHaveVoted()) return;

        guild.finishVoting();
    }

    /**
     * Checks if all guild members have voted
     * @return Whether all guild members have voted
     */
    private boolean allHaveVoted() {
        int total = pro.size() + against.size();
        int totalPossible = guild.getMembers().size();

        return total >= totalPossible;
    }

    /**
     * Votes for in favor of the vote
     * @param id The voter
     */
    public void voteFor(UUID id) {
        pro.add(id);
        vote();
    }

    public void voteFor(Dynasty dynasty) {
        proDynasties.add(dynasty);
        vote();
    }

    /**
     * Votes against the vote
     * @param id The voter
     */
    public void voteAgainst(UUID id) {
        against.add(id);
        vote();
    }

    public void voteAgainst(Dynasty dynasty) {
        againstDynasties.add(dynasty);
        vote();
    }

    private void vote() {
        Crown.getTradersGuild().getVoteBox().updateSign(this, guild.getWorld());
        checkShouldEnd();
    }

    /**
     * Gets all those who voted against the motion
     * @return All who voted against the vote
     */
    public ObjectSet<UUID> getAgainst() {
        return against;
    }

    /**
     * Gets all those who voted for the motion
     * @return All who voted for the vote.
     */
    public ObjectSet<UUID> getPro() {
        return pro;
    }

    /**
     * Gets all the houses that voted against the motion
     * @return All houses that voted against
     */
    public ObjectSet<Dynasty> getAgainstHouses() {
        return againstDynasties;
    }

    /**
     * Gets all the houses that voted for the motion
     * @return All houses that voted for
     */
    public ObjectSet<Dynasty> getProHouses() {
        return proDynasties;
    }

    /**
     * Checks if the given house has voted
     * @param dynasty The house to check
     * @return Whether the house has voted
     */
    public boolean hasVoted(Dynasty dynasty) {
        return againstDynasties.contains(dynasty) || proDynasties.contains(dynasty);
    }

    /**
     * Checks if the given ID has voted
     * @param id The ID to check
     * @return Whether the given ID has vote for or against the vote
     */
    public boolean hasVoted(UUID id) {
        return pro.contains(id) || against.contains(id);
    }

    /**
     * Gets all who have not voted
     * @return All non voters
     */
    public ObjectList<UUID> compileNonVoters() {
        ObjectList<UUID> result = new ObjectArrayList<>();

        for (UUID id: guild.getMembers()) {
            if(hasVoted(id)) continue;
            result.add(id);
        }

        return result;
    }

    public int proCount() {
        return pro.size() + proDynasties.size();
    }

    public int againstCount() {
        return against.size() + againstDynasties.size();
    }

    /**
     * Counts the result
     * @return The result of the vote
     */
    public VoteResult countVotes() {
        //If no one voted, return appropriate result
        if(proCount() + againstCount() == 0) return VoteResult.NO_VOTES;

        //Count votes
        int totalPossible = guild.getMembers().size() + Registries.DYNASTIES.size();
        int pro = proCount();
        int against = againstCount();
        int didVote = pro + against;

        //If less than half voted, return a result with_abstentions
        //Else return a normal result
        return didVote < (totalPossible / 2) ?
                (pro == against ?
                        VoteResult.TIE_WITH_ABSTENTIONS :
                        (pro > against ? VoteResult.WIN_WITH_ABSTENTIONS : VoteResult.LOSE_WITH_ABSTENTIONS)
                )

                : (pro == against ?
                    VoteResult.TIE :
                    (pro > against ? VoteResult.WIN : VoteResult.LOSE)
                );
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        pro.clear();
        against.clear();
        againstDynasties.clear();
        proDynasties.clear();

        if(!json.missingOrNull("pro")) pro.addAll(json.getList("pro", JsonUtils::readUUID));
        if(!json.missingOrNull("against")) against.addAll(json.getList("against", JsonUtils::readUUID));
        if(!json.missingOrNull("proHouses")) proDynasties.addAll(json.getList("proHouses", e -> Registries.DYNASTIES.get(JsonUtils.readKey(e))));
        if(!json.missingOrNull("againstHouses")) againstDynasties.addAll(json.getList("againstHouses", e -> Registries.DYNASTIES.get(JsonUtils.readKey(e))));

        //topic
        JsonWrapper topicJson = json.getWrapped("topic");
        Key typeKey = topicJson.getKey("type");

        topic = Registries.VOTE_TOPICS.get(typeKey).deserialize(topicJson.get("value"));
        started = json.getDate("started");
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        if(!pro.isEmpty()) json.addList("pro", pro, JsonUtils::writeUUID);
        if(!against.isEmpty()) json.addList("against", against, JsonUtils::writeUUID);

        if(!proDynasties.isEmpty()) json.addList("proHouses", proDynasties, h -> JsonUtils.writeKey(h.key()));
        if(!againstDynasties.isEmpty()) json.addList("againstHouses", againstDynasties, h -> JsonUtils.writeKey(h.key()));

        //Topic
        JsonWrapper topicJson = JsonWrapper.empty();
        topicJson.addKey("type", topic.typeKey());

        VoteTopicType type = topic.getType();
        topicJson.add("value", type.serialize(topic));

        json.add("topic", topicJson);

        //date
        json.addDate("started", started);

        return json.getSource();
    }
}

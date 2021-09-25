package net.forthecrown.economy.market.guild;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.forthecrown.core.Crown;
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

public class VoteState implements JsonSerializable, JsonDeserializable {
    private final TradersGuild guild;

    private final ObjectSet<UUID> pro = new ObjectOpenHashSet<>();
    private final ObjectSet<UUID> against = new ObjectOpenHashSet<>();

    VoteTopic topic;
    Date started;

    public VoteState(TradersGuild guild) {
        this.guild = guild;
    }

    public void vote(GuildVoter voter) {
        voter.vote(this);
    }

    public VoteTopic getTopic() {
        return topic;
    }

    public Date getStarted() {
        return started;
    }

    private void checkShouldEnd() {
        if(!shouldEnd()) return;

        guild.finishVoting();
    }

    private boolean shouldEnd() {
        int total = pro.size() + against.size();
        int totalPossible = guild.getMembers().size();

        return total >= totalPossible;
    }

    public void voteFor(UUID id) {
        pro.add(id);
        vote();
    }

    public void voteAgainst(UUID id) {
        against.add(id);
        vote();
    }

    private void vote() {
        Crown.getTradersGuild().getVoteBox().updateSign(this, guild.getWorld());
        checkShouldEnd();
    }

    public ObjectSet<UUID> getAgainst() {
        return against;
    }

    public ObjectSet<UUID> getPro() {
        return pro;
    }

    public boolean hasVoted(UUID id) {
        return pro.contains(id) || against.contains(id);
    }

    public ObjectList<UUID> compileNonVoters() {
        ObjectList<UUID> result = new ObjectArrayList<>();

        for (UUID id: guild.getMembers()) {
            if(hasVoted(id)) continue;
            result.add(id);
        }

        return result;
    }

    public VoteResult countVotes() {
        int totalPossible = guild.getMembers().size();
        int pro = this.pro.size();
        int against = this.against.size();
        int didVote = pro + against;

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

        if(!json.missingOrNull("pro")) pro.addAll(json.getList("pro", JsonUtils::readUUID));
        else pro.clear();

        if(!json.missingOrNull("against")) against.addAll(json.getList("against", JsonUtils::readUUID));
        else against.clear();

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

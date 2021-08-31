package net.forthecrown.economy.market.guild;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.economy.market.guild.topics.VoteTopic;
import net.forthecrown.economy.market.guild.topics.VoteTopicType;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

public class VoteState implements JsonSerializable, JsonDeserializable {
    private final ObjectList<UUID> pro = new ObjectArrayList<>();
    private final ObjectList<UUID> against = new ObjectArrayList<>();

    VoteTopic topic;
    Date started;

    VoteState() {}

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
        int total = pro.size() + against.size();


    }

    public void endVoting() {
    }

    public void voteFor(UUID id) {
        pro.add(id);
    }

    public void voteAgainst(UUID id) {
        against.add(id);
    }

    public ObjectList<UUID> getAgainst() {
        return against;
    }

    public ObjectList<UUID> getPro() {
        return pro;
    }

    public VoteResult countVotes(TradersGuild guild) {
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
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        if(!json.missingOrNull("pro")) pro.addAll(json.getList("pro", JsonUtils::readUUID));
        if(!json.missingOrNull("against")) pro.addAll(json.getList("against", JsonUtils::readUUID));

        //topic
        JsonBuf topicJson = json.getBuf("topic");
        Key typeKey = topicJson.getKey("type");

        topic = Registries.VOTE_TOPICS.get(typeKey).deserialize(topicJson.get("value"));

        try {
            started = DateFormat.getDateInstance().parse(json.getString("started"));
        } catch (ParseException e) {
            started = null;
            e.printStackTrace();
        }
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        if(!pro.isEmpty()) json.addList("pro", pro, JsonUtils::writeUUID);
        if(!against.isEmpty()) json.addList("against", against, JsonUtils::writeUUID);

        //Topic
        JsonBuf topicJson = JsonBuf.empty();
        topicJson.addKey("type", topic.typeKey());

        VoteTopicType type = topic.getType();
        topicJson.add("value", type.serialize(topic));

        json.add("topic", topicJson);

        //date
        json.add("started", started.toString());

        return json.getSource();
    }
}

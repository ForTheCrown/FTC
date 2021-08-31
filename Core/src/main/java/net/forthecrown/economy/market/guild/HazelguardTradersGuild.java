package net.forthecrown.economy.market.guild;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.market.guild.topics.VoteTopic;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.Vector3i;

import java.util.Date;
import java.util.UUID;

public class HazelguardTradersGuild extends AbstractJsonSerializer implements TradersGuild {
    public static VoteBoxPos VOTE_BOX = new VoteBoxPos(
            Worlds.OVERWORLD,
            new Vector3i(12, 12, 12),
            new Vector3i(12, 12, 12)
    );

    private final ObjectList<UUID> members = new ObjectArrayList<>();
    private VoteState currentState;

    public HazelguardTradersGuild() {
        super("traders_guild");

        reload();

        Crown.logger().info("Trader's guild loaded");
    }

    @Override
    public VoteState getVoteState() {
        return currentState;
    }

    @Override
    public void createVote(VoteTopic topic) {
        currentState = new VoteState();

        currentState.started = new Date();
        currentState.topic = topic;
    }

    @Override
    public ObjectList<UUID> getMembers() {
        return members;
    }

    @Override
    public void addMember(UUID member) {
        members.add(member);
    }

    @Override
    public void removeMember(UUID member) {
        members.remove(member);
    }

    @Override
    public VoteBoxPos getVoteBox() {
        return VOTE_BOX;
    }

    @Override
    protected void save(JsonBuf json) {
        json.addList("members", members, JsonUtils::writeUUID);
        if(isCurrentlyVoting()) json.add("currentVote", currentState);
    }

    @Override
    protected void reload(JsonBuf json) {

    }
}

package net.forthecrown.economy.market.guild;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.economy.market.guild.topics.VoteTopic;
import net.forthecrown.serializer.CrownSerializer;

import java.util.UUID;

public interface TradersGuild extends CrownSerializer {
    VoteState getVoteState();

    default boolean isCurrentlyVoting() {
        return getVoteState() != null;
    }

    void createVote(VoteTopic topic);

    ObjectList<UUID> getMembers();

    default boolean isMember(UUID id) {
        return getMembers().contains(id);
    }

    void addMember(UUID member);
    void removeMember(UUID member);

    VoteBoxPos getVoteBox();
}

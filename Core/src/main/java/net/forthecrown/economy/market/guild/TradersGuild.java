package net.forthecrown.economy.market.guild;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.economy.market.guild.topics.VoteTopic;

import java.util.UUID;

public interface TradersGuild {
    VoteTopic getCurrentVote();
    VoteState getVoteState();

    ObjectList<UUID> getMembers();

    void addMember(UUID member);
    void removeMember(UUID member);
}

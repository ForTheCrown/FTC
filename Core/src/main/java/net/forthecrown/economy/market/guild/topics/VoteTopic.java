package net.forthecrown.economy.market.guild.topics;

import net.forthecrown.serializer.SerializationTypeHolder;

public interface VoteTopic extends SerializationTypeHolder<VoteTopic> {
    void onVoteSucceed();
    void onVoteFail();
    void onVotingBegin();

    @Override
    VoteTopicType<? extends VoteTopic> getType();
}

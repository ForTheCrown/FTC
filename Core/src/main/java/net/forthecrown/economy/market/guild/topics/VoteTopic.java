package net.forthecrown.economy.market.guild.topics;

import net.forthecrown.economy.market.guild.VoteResult;
import net.forthecrown.serializer.SerializationTypeHolder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public interface VoteTopic extends SerializationTypeHolder<VoteTopic> {
    @Nullable PostVoteTask onVoteEnd(VoteResult result);

    void onVotingBegin();

    @Override
    VoteTopicType<? extends VoteTopic> getType();

    default Component signDisplay() {
        VoteTopicType type = getType();

        return type.signDisplay(this);
    }
}

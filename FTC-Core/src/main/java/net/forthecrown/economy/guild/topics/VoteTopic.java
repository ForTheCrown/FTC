package net.forthecrown.economy.guild.topics;

import net.forthecrown.economy.houses.House;
import net.forthecrown.economy.houses.VoteModifier;
import net.forthecrown.economy.guild.VoteResult;
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

    VoteModifier makeModifier(House house);
}

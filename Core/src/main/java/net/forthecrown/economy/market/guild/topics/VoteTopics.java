package net.forthecrown.economy.market.guild.topics;

import net.forthecrown.registry.Registries;

public final class VoteTopics {
    private VoteTopics() {}

    public static void init() {
        register(EvictOwnerTopic.INSTANCE);
    }

    private static void register(VoteTopicType type) {
        Registries.VOTE_TOPICS.register(type.key(), type);
    }
}

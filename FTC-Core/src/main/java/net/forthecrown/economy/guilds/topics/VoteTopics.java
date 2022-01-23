package net.forthecrown.economy.guilds.topics;

import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;

public final class VoteTopics {
    private VoteTopics() {}

    public static void init() {
        register(new EvictTopic());
        register(new KickMemberTopic());

        Registries.VOTE_TOPICS.close();
        Crown.logger().info("Vote Topics initialized");
    }

    private static void register(VoteTopic t) {
        Registries.VOTE_TOPICS.register(t.key(), t);
    }
}
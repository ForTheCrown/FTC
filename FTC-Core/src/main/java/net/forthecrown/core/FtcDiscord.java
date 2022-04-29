package net.forthecrown.core;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.apache.logging.log4j.Logger;

/**
 * Class for interacting with the Discord server using
 * the discord srv plugin
 */
public final class FtcDiscord {
    private FtcDiscord() {}

    private static final Logger LOGGER = Crown.logger();

    public static final String
            UPDATE_CHANNEL  = "updates",
            GENERAL_CHANNEL = "chat",
            STAFF_CHAT      = "cool-club";

    public static DiscordSRV getHandle() {
        return DiscordSRV.getPlugin();
    }

    public static TextChannel getTextChannel(String name) {
        return getHandle().getOptionalTextChannel(name);
    }

    public static TextChannel updateChannel() {
        return getTextChannel(UPDATE_CHANNEL);
    }

    public static TextChannel staffChat() {
        return getTextChannel(STAFF_CHAT);
    }
}
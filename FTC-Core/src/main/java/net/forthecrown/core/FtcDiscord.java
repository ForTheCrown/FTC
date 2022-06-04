package net.forthecrown.core;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.forthecrown.core.chat.ChatUtils;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

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
            STAFF_CHAT      = "cool-club",
            STAFF_LOG       = "staff-log";

    public static DiscordSRV getHandle() {
        return DiscordSRV.getPlugin();
    }

    public static boolean isActive() {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            return false;
        }

        return getHandle().isEnabled() && getHandle().getJda() != null;
    }

    public static TextChannel getTextChannel(String name) {
        // Bro this fucking method name lmao
        return getHandle().getDestinationTextChannelForGameChannelName(name);
    }

    public static TextChannel updateChannel() {
        return getTextChannel(UPDATE_CHANNEL);
    }

    public static TextChannel staffChat() {
        return getTextChannel(STAFF_CHAT);
    }

    public static void staffLog(String cat, String msg, Object... args) {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return;
        if (!FtcVars.staffLogEnabled.get()) return;

        TextChannel channel = getTextChannel(STAFF_LOG);
        DiscordUtil.queueMessage(channel, "**[" + cat + "]** " + ChatUtils.format(msg, args));
    }
}
package net.forthecrown.events;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.events.GenericEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.guild.GuildBanEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.EventListener;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.user.UserManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DiscordListener implements EventListener {
    private static final Logger LOGGER = Crown.logger();

    // Basically just bans people ingame when they're banned in the discord
    public void onBan(GuildBanEvent event) {
        DiscordSRV srv = FtcDiscord.getHandle();
        UUID id = srv.getAccountLinkManager().getUuid(event.getUser().getId());

        // No linked account
        if (id == null) {
            LOGGER.info("Couldn't match discord ban: {}:{}, no linked MC account found", event.getUser().getName(), event.getUser().getId());
            return;
        }

        FtcDiscord.staffLog("Punishments", "Automated ban:");
        Punishments.handlePunish(
                UserManager.getUser(id),
                GrenadierUtils.wrap(Bukkit.getConsoleSender(), null),
                null,
                Punishments.INDEFINITE_EXPIRY,
                PunishType.BAN,
                null
        );
    }

    // This is how the event listener works :(
    // just listen to everything and check it's a ban event lol
    // How inefficient
    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GuildBanEvent guildBanEvent) {
            onBan(guildBanEvent);
        }
    }
}
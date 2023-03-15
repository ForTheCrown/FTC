package net.forthecrown.core;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.Util;
import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * Class for interacting with the Discord server using the discord srv plugin
 */
public final class FtcDiscord {
  private FtcDiscord() {}

  public static final String
      // Channel names
      STAFF_LOG = "staff-log",
      COOL_CLUB = "cool-club";

  public static boolean isActive() {
    if (!Util.isPluginEnabled("DiscordSRV")) {
      return false;
    }

    return DiscordSRV.getPlugin()
        .getJda() != null;
  }

  public static UUID getPlayerId(String discordId) {
    Objects.requireNonNull(discordId);

    return DiscordSRV.getPlugin()
        .getAccountLinkManager()
        .getUuid(discordId);
  }

  public static Optional<TextChannel> findChannel(String name) {
    return Optional.ofNullable(
        DiscordSRV.getPlugin()
            .getDestinationTextChannelForGameChannelName(name)
    );
        // Only use text channels set in the DiscordSRV config
        /*.or(() -> {
          var jda = DiscordSRV.getPlugin().getJda();

          if (jda == null) {
            return Optional.empty();
          }

          var found = jda.getTextChannelsByName(name, true);

          if (found.isEmpty()) {
            return Optional.empty();
          }

          return Optional.of(found.get(0));
        });*/
  }

  public static UUID getPlayerId(Member member) {
    return getPlayerId(member.getId());
  }

  /**
   * Sends a message to the staff log
   *
   * @param cat  The category of the message
   * @param msg  The message itself. Keep in mind the argument format is the same used by Log4J
   * @param args The message arguments
   */
  public static void staffLog(String cat, String msg, Object... args) {
    if (!isActive() || !GeneralConfig.staffLogEnabled) {
      return;
    }

    // Bro this fucking method name lmao
    findChannel(STAFF_LOG).ifPresent(channel -> {
      String formatted
          = new ParameterizedMessage(msg, args)
          .getFormattedMessage();

      channel.sendMessageFormat("**[%s]** %s", cat, formatted).submit();
    });
  }
}
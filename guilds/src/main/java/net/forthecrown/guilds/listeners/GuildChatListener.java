package net.forthecrown.guilds.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.Loggers;
import net.forthecrown.guilds.GUserProperties;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GuildChatListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onAsyncChat(AsyncChatEvent event) {
    User user = Users.get(event.getPlayer());

    boolean gchat = user.get(GUserProperties.G_CHAT_TOGGLE);
    Guild guild = Guilds.getGuild(user);

    if (!gchat) {
      return;
    }

    if (guild == null) {
      user.set(GUserProperties.G_CHAT_TOGGLE, false);
      return;
    }

    Loggers.getPluginLogger().info("Guild chat: {} > {}",
        user.getName(), event.signedMessage().message()
    );

    event.setCancelled(true);

    Tasks.runSync(() -> {
      guild.chat(user, PlayerMessage.of(event.signedMessage().message(), user));
    });
  }
}

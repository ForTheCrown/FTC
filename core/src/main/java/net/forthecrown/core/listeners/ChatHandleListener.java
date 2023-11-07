package net.forthecrown.core.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.chat.ChatRenderer.Default;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Set;
import net.forthecrown.text.Messages;
import net.forthecrown.text.parse.ChatParseFlag;
import net.forthecrown.text.parse.ChatParser;
import net.forthecrown.text.parse.TextContext;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ChatHandleListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onAsyncChat(AsyncChatEvent event) {
    event.setCancelled(true);

    Set<Audience> viewers = event.viewers();
    Player sender = event.getPlayer();

    ChatRenderer renderer = event.renderer() instanceof Default
        ? FtcChatRenderer.INSTANCE
        : event.renderer();

    if (viewers.isEmpty()) {
      return;
    }

    User user = Users.get(sender);
    ChatParser parser = ChatParser.parser();
    Set<ChatParseFlag> flags = ChatParseFlag.allApplicable(sender);

    Component baseMessage = event.message();

    viewers.forEach(viewer -> {
      TextContext ctx = TextContext.of(flags, viewer);
      Component message = parser.runFunctions(baseMessage, ctx);

      Component displayName = user.displayName(viewer);
      Component fullMessage = renderer.render(sender, displayName, message, viewer);

      viewer.sendMessage(fullMessage);
    });
  }

  private enum FtcChatRenderer implements ChatRenderer {
    INSTANCE;

    @Override
    public @NotNull Component render(
        @NotNull Player source,
        @NotNull Component displayName,
        @NotNull Component message,
        @NotNull Audience viewer
    ) {
      return Messages.chatMessage(displayName, message);
    }
  }
}

package net.forthecrown.events.player;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.core.AfkKicker;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.user.MarriageMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onAsyncChatDecorate(AsyncChatDecorateEvent event) {
    event.result(
        Text.renderString(
            event.player(), Text.toString(event.originalMessage())
        )
    );
  }

  @EventHandler(ignoreCancelled = true)
  public void onAsyncChatCommandDecorate(AsyncChatCommandDecorateEvent event) {
    onAsyncChatDecorate(event);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onHandleChat(AsyncChatEvent event) {
    // Cancel event so no signed chat messages are sent
    // to anyone
    event.setCancelled(true);

    // Manually send chat message to all viewers still
    // left in the viewer list
    event.viewers().forEach(audience -> {
      var message = event.renderer()
          .render(
              event.getPlayer(),
              event.getPlayer().displayName(),
              event.message(),
              audience
          );

      audience.sendMessage(event.getPlayer(), message);
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerChat(AsyncChatEvent event) {
    Player player = event.getPlayer();
    User user = Users.get(player);

    // These have to be run in sync due to them updating the user's
    // TAB menu and thus calling the NametagEdit plugin
    Tasks.runSync(() -> {
      AfkKicker.addOrDelay(event.getPlayer().getUniqueId());
      AfkListener.checkUnafk(event);
    });

    event.renderer(FtcChatRenderer.INSTANCE);
    var rendered = event.renderer().render(player, user.displayName(), event.message(), player);
    var mute = Punishments.checkMute(player);

    // If said banned word, set mute to hard, else keep mute as is
    // then if mute == hard, report to eaves dropper and cancel event
    mute = BannedWords.checkAndWarn(player, rendered) ? Mute.HARD : mute;

    if (mute != Mute.NONE) {
      EavesDropper.reportChat(rendered, mute);
    }

    if (!mute.isVisibleToOthers()) {
      event.setCancelled(true);
      return;
    }

    // If staff chat toggle is enabled for this player
    // then don't send message to chat, just to staff
    if (StaffChat.toggledPlayers.contains(player.getUniqueId())) {
      StaffChat.newMessage()
          .setSource(user.getCommandSource(null))
          .setMessage(event.message())
          .send();

      event.setCancelled(true);
      return;
    }

    // If vanished, don't say nuthin
    if (user.get(Properties.VANISHED)) {
      user.sendMessage(Messages.CHAT_NO_SPEAK_VANISH);

      event.setCancelled(true);
      return;
    }

    if (user.get(Properties.G_CHAT_TOGGLE)) {
      var guild = user.getGuild();

      if (guild == null) {
        user.set(Properties.G_CHAT_TOGGLE, false);
      } else {
        guild.chat(user, event.message());

        event.setCancelled(true);
        return;
      }
    }

    // If marriage chat enabled
    if (user.get(Properties.MARRIAGE_CHAT)) {
      event.setCancelled(true);
      var spouse = user.getInteractions().spouseUser();

      // Ensure spouse exists and is online
      if (spouse == null || !spouse.isOnline()) {
        user.flip(Properties.MARRIAGE_CHAT);
        user.sendMessage(Messages.CANNOT_SEND_MCHAT);

        return;
      }

      MarriageMessage.send(
          user, spouse,
          Text.toString(event.originalMessage()),
          true
      );

      return;
    }

    final Mute finalMute = mute;

    // Filter out all that shouldn't see the message
    event.viewers().removeIf(audience -> {
      if (!(audience instanceof Player playerViewer)) {
        return false;
      }

      var viewer = Users.get(playerViewer);

      if (Users.areBlocked(user, viewer)) {
        return true;
      }

      // If the message sender is soft-muted, then only other
      // soft muted players may see the message
      return finalMute == Mute.SOFT && Punishments.muteStatus(viewer) != Mute.SOFT;
    });
  }

  /**
   * Tests if the given input <code>s</code> is more than half uppercase, if it is, then it warns
   * the source and tells them not to send all upper case messages.
   * <p>
   * If the <code>s</code> input is less than 8 characters long, if the sender is null or has the
   * {@link Permissions#CHAT_IGNORE_CASE} permission, then this method won't check the string
   *
   * @param source The sender of the message
   * @param s      The input to test
   */
  public static void warnCase(@Nullable CommandSender source, String s) {
    if (s.length() <= 8
        || source == null
        || source.hasPermission(Permissions.CHAT_IGNORE_CASE)
    ) {
      return;
    }

    int upperCaseCount = 0;
    int half = s.length() / 2;

    for (int i = 0; i < s.length(); i++) {
      if (Character.isUpperCase(s.charAt(i))) {
        upperCaseCount++;
      }

      if (upperCaseCount > half) {
        //1 and a half minute cooldown
        if (!Cooldown.containsOrAdd(source, "uppercase_warning", (60 + 30) * 20)) {
          source.sendMessage(Messages.ALL_CAPS);
        }

        return;
      }
    }
  }

  private enum FtcChatRenderer implements ChatRenderer {
    INSTANCE;

    @Override
    public @NotNull Component render(@NotNull Player source,
                                     @NotNull Component displayName,
                                     @NotNull Component message,
                                     @NotNull Audience viewer
    ) {
      return format(source, message, viewer);
    }

    private Component format(Player source, Component message, Audience viewer) {
      var plain = Text.plain(message);
      warnCase(source, plain);

      User user = Users.get(source);
      boolean prependRank = Users.allowsRankedChat(viewer);

      return Messages.chatMessage(user, message, prependRank);
    }
  }
}
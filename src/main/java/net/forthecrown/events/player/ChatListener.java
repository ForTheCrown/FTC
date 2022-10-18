package net.forthecrown.events.player;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.*;
import net.forthecrown.user.MarriageMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
        // This may be a crime to call another event listener in
        // this listener... but these events perform the same operation
        onAsyncChatDecorate(event);
    }



    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        User user = Users.get(player);

        event.renderer(new FtcChatRenderer());
        var rendered = event.renderer().render(player, user.displayName(), event.message(), player);
        var mute = Punishments.checkMute(player);

        // If said banned word, set mute to hard, else keep mute as is
        // then if mute == hard, report to eaves dropper and cancel event
        if ((mute = BannedWords.checkAndWarn(player, rendered) ? Mute.HARD : mute) == Mute.HARD) {
            EavesDropper.reportChat(rendered, mute);

            event.setCancelled(true);
            return;
        }

        // If staff chat toggle is enabled for this player
        // then don't send message to chat, just to staff
        if (StaffChat.toggledPlayers.contains(player.getUniqueId())) {
            StaffChat.send(user.getCommandSource(null), event.message(), false);

            event.setCancelled(true);
            return;
        }

        // If vanished, don't say nuthin
        if (user.get(Properties.VANISHED)) {
            user.sendMessage(Messages.CHAT_NO_SPEAK_VANISH);

            event.setCancelled(true);
            return;
        }

        // If marriage chat enabled
        if (user.get(Properties.MARRIAGE_CHAT)) {
            var spouse = user.getInteractions().spouseUser();
            event.setCancelled(true);

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
     * Tests if the given input <code>s</code> is more than half uppercase,
     * if it is, then it warns the source and tells them not to send all
     * upper case messages.
     * <p>
     * If the <code>s</code> input is less than 8 characters long, if the
     * sender is null or has the {@link Permissions#CHAT_IGNORE_CASE} permission,
     * then this method won't check the string
     *
     * @param source The sender of the message
     * @param s The input to test
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

    private static class FtcChatRenderer implements ChatRenderer {
        private Component message;

        @Override
        public @NotNull Component render(@NotNull Player source,
                                         @NotNull Component displayName,
                                         @NotNull Component message,
                                         @NotNull Audience viewer
        ) {
            return Objects.requireNonNullElseGet(
                    this.message,
                    () -> this.message = format(source, message)
            );
        }

        private Component format(Player source, Component message) {
            var plain = Text.plain(message);
            warnCase(source, plain);

            User user = Users.get(source);
            return Messages.chatMessage(user, message);
        }
    }
}
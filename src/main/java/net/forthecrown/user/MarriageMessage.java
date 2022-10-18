package net.forthecrown.user;

import lombok.Getter;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.text.Text;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.forthecrown.text.Messages.*;

/**
 * Message sent by a user to only be seen by their spouse
 */
@Getter
public class MarriageMessage {
    /**
     * The user that sent the message
     */
    private final User sender;

    /**
     * The user the message is aimed at
     */
    private final User target;

    /**
     * The original input of the message
     */
    private final String input;

    /**
     * The formatted message, if the sender doesn't
     * have permissions for color-coded chat messages
     * or emojis, this will basically just be
     * {@link #input} as a plain text component
     */
    private final Component formattedText;

    /**
     * True, if this marriage message was sent in normal chat
     * by a user with {@link Properties#MARRIAGE_CHAT}
     * enabled.
     * If false, this was sent via command.
     */
    private final boolean chat;

    private MarriageMessage(User sender, User target, String input, boolean chat) {
        this.sender = sender;
        this.target = target;
        this.input = input;
        this.chat = chat;

        this.formattedText = Text.renderString(sender.getPlayer(), input);
    }

    /**
     * Sends a marriage message from the given sender
     * to the given target
     *
     * @param sender The user sending the message
     * @param target The target sending the message
     * @param input The original message input
     * @param chat True, if message was sent from chat, false if from command
     */
    public static void send(User sender, User target, String input, boolean chat) {
        new MarriageMessage(sender, target, input, chat).run();
    }

    /**
     * Formats a marriage message
     * @param displayName The sender's display name
     * @param message The message contents
     * @return The formatted message
     */
    public static Component format(Component displayName, Component message) {
        return Component.text()
                .append(MARRIAGE_PREFIX)
                .append(displayName.color(NamedTextColor.GOLD))
                .append(MARRIAGE_POINTER)
                .append(message)
                .build();
    }

    /**
     * Runs the logic that executes the message.
     * <p>
     * This will make sure the sender didn't use any banned words, isn't
     * muted/softmuted and isn't seperated or blocked by their spouse.
     * If all those checks are passed, the target and sender are both
     * send the same formatted message
     */
    public void run() {
        Component formatted = format(getSender().displayName(), this.getFormattedText());
        Mute mute = Punishments.checkMute(sender.getPlayer());

        // Validate they didn't just use a slur or something lol
        if (BannedWords.checkAndWarn(getSender().getPlayer(), formatted)) {
            mute = Mute.HARD;
        }

        // Test blocked
        if (Users.testBlocked(
                sender, target,
                MC_BLOCKED_SENDER, MC_BLOCKED_TARGET
        )) {
            return;
        }

        EavesDropper.reportMarriageChat(this, mute);

        // If mute status allows sender to see
        // then tell sender they just send message
        if(mute.isVisibleToSender()) {
            sender.sendMessage(target, formatted);
        }

        // If the sender isn't muted at all, send
        // target message
        if(mute.isVisibleToOthers()) {
            target.sendMessage(sender, formatted);
        }
    }
}
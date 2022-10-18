package net.forthecrown.user;

import lombok.Getter;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import static net.forthecrown.text.Messages.*;

/**
 * A message sent from one command source to another.
 * Typically, this will be between a player and another player, but
 * the way this system is structured allows for command blocks, entities
 * and everything between to send users direct messages.
 */
@Getter
public class DirectMessage {

    /**
     * The sender of this message
     */
    @Getter
    private final CommandSource sender;

    /**
     * The target of the message
     */
    @Getter
    private final CommandSource target;

    /**
     * The text, formatted according to the sender's
     * permissions and levels.
     */
    private final Component formattedText;

    /**
     * True, if this message was sent via a /reply command
     */
    private final boolean responding;

    /**
     * The original input of the message
     */
    private final String input;

    private DirectMessage(CommandSource sender, CommandSource target, boolean responding, String input) {
        this.sender = sender;
        this.target = target;
        this.responding = responding;
        this.input = input;

        formattedText = Text.renderString(sender.asBukkit(), input);
    }

    /**
     * Creates a {@link DirectMessage} instance and calls its {@link #run()} function.
     * @param sender The sender of the message
     * @param target The message's target
     * @param responding True, if the message is being sent as a reply
     * @param input The input text of the message
     */
    public static void run(CommandSource sender, CommandSource target, boolean responding, String input) {
        new DirectMessage(sender, target, responding, input).run();
    }

    /**
     * Gets the sender's display name.
     * <p>
     * Delegate method for {@link Text#sourceDisplayName(CommandSource)}
     * with {@link #sender} as the input.
     *
     * @return The sender's display name
     */
    public Component senderDisplayName() {
        return Text.sourceDisplayName(sender);
    }

    /**
     * Gets the target's display name.
     * <p>
     * Delegate method for {@link Text#sourceDisplayName(CommandSource)}
     * with {@link #target} as the input.
     *
     * @return The target's display name
     */
    public Component targetDisplayName() {
        return Text.sourceDisplayName(target);
    }

    /**
     * Gets the message header the sender will see
     * @return The sender's message header
     */
    public Component getSenderHeader() {
        return getHeader(
                DM_ME_HEADER,
                targetDisplayName(),
                NamedTextColor.GOLD
        );
    }

    /**
     * Gets the target's message header
     * @return The target's message header
     */
    public Component getTargetHeader() {
        return getHeader(
                senderDisplayName(),
                DM_ME_HEADER,
                NamedTextColor.GOLD
        );
    }

    /**
     * Creates a direct message header
     * @param first The first display name
     * @param second The second display naem
     * @param color The color of the message
     * @return The formatted header
     */
    public static Component getHeader(Component first, Component second, TextColor color) {
        return Text.format("[&e{0}&r -> &e{1}&r]", color, first, second);
    }

    /**
     * Executes the direct message.
     * <p>
     * Checks if the message contained any banned words, if it does
     * it stops executing.
     * It will also ensure that both the sender and target aren't muted,
     * haven't blocked each other and haven't been forcefully separated.
     * <p>
     * If all those checks are passed both the sender and target are
     * sent the direct message
     */
    public void run() {
        Mute mute = Punishments.checkMute(sender.asBukkit());

        //Validate they didn't just use a slur or something lol
        if (BannedWords.checkAndWarn(sender.asBukkit(), formattedText)) {
            mute = Mute.HARD;
        }

        Identity sender = Identity.nil();
        Identity target = Identity.nil();

        EavesDropper.reportDirectMessage(this, mute);

        //If no mute whatsoever
        if (mute.isVisibleToOthers()) {
            if (this.target.isPlayer()) {
                User targetUser = Users.get(this.target.asOrNull(Player.class));
                targetUser.setLastMessage(this.sender);

                target = targetUser;

                if (this.sender.isPlayer()) {
                    User senderUser = Users.get(this.sender.asOrNull(Player.class));
                    senderUser.setLastMessage(getTarget());

                    sender = senderUser;

                    // Test if blocked
                    if (Users.testBlocked(
                            senderUser, targetUser,
                            DM_BLOCKED_SENDER, DM_BLOCKED_TARGET
                    )) {
                        return;
                    }
                }

                //If AFK, warn sender target might not see
                if (targetUser.isAfk()) {
                    this.sender.sendMessage(Messages.afkDirectMessage(targetUser));
                }
            }

            Component receiverMessage = Component.text()
                    .append(getTargetHeader())
                    .append(Component.space())
                    .append(formattedText)
                    .build();

            this.target.sendMessage(sender, receiverMessage);
        }

        //If soft or no mute, send to sender
        if (mute.isVisibleToSender()) {
            Component senderMessage = Component.text()
                    .append(getSenderHeader())
                    .append(Component.space())
                    .append(formattedText)
                    .build();

            this.sender.sendMessage(target, senderMessage);
        }
    }

}
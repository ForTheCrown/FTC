package net.forthecrown.core.admin;

import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.guilds.Guild;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.forthecrown.user.DirectMessage;
import net.forthecrown.user.MarriageMessage;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.BoolProperty;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

import static net.forthecrown.core.Messages.*;

/**
 *
 */
public class EavesDropper {
    private static final Logger LOGGER = FTC.getLogger();

    public static void send(Component message, BoolProperty property, boolean log) {
        var formatted = edPrependPrefix(message);

        Users.getOnline()
                .forEach(user -> {
                    if (!user.hasPermission(Permissions.EAVESDROP)) {
                        return;
                    }

                    if (!user.get(property)) {
                        return;
                    }

                    user.sendMessage(formatted);
                });


        if (log) {
            LOGGER.info(Text.plain(message));
        }
    }

    public static void reportDirectMessage(DirectMessage message, Mute status) {
        if (message.getSender().hasPermission(Permissions.EAVESDROP_ADMIN)
                || message.getTarget().hasPermission(Permissions.EAVESDROP_ADMIN)
        ) {
            return;
        }

        send(edDirectMessage(message, status), Properties.EAVES_DROP_DM, false);
    }

    public static void reportMarriageChat(MarriageMessage message, Mute mute) {
        if (message.getTarget().hasPermission(Permissions.EAVESDROP_ADMIN)
                || message.getSender().hasPermission(Permissions.EAVESDROP_ADMIN)
        ) {
            return;
        }

        send(edMarriageChat(message, mute), Properties.EAVES_DROP_MCHAT, message.isChat());
    }

    public static void reportSign(Player placer, Block sign, List<Component> lines) {
        if (lines.isEmpty()
                || isEmpty(lines)
                || placer.hasPermission(Permissions.EAVESDROP_ADMIN)
        ) {
            return;
        }

        var pos = WorldVec3i.of(sign);
        send(edSign(placer, pos, lines), Properties.EAVES_DROP_SIGN, false);
    }

    private static boolean isEmpty(List<Component> lines) {
        for (var c: lines) {
            if (!Text.plain(c).isBlank()) {
                return false;
            }
        }

        return true;
    }

    public static void reportChat(Component chatMessage, Mute mute) {
        send(edChat(chatMessage, mute), Properties.EAVES_DROP_MUTED, true);
    }

    public static void reportOreMining(Block block, int count, Player player) {
        if (player.hasPermission(Permissions.EAVESDROP_ADMIN)) {
            return;
        }

        send(edOreMining(player, block, count), Properties.EAVES_DROP_MINING, true);
    }

    public static void reportGuildChat(User sender, Mute mute, Guild guild, Component message) {
        if (sender.hasPermission(Permissions.EAVESDROP_ADMIN)) {
            return;
        }

        send(
                edGuildChat(sender, guild, mute, message),
                Properties.EAVES_DROP_GUILD_CHAT,
                false
        );
    }
}
package net.forthecrown.events.dynamic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildMessage;
import net.forthecrown.guilds.menu.GuildMenus;
import net.forthecrown.user.User;
import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public class GuildSignPacketListener implements PacketListener {
    private static final Logger LOGGER = FTC.getLogger();

    // Literally copied and pasted from the packet listener class
    private static final int MAX_SIGN_LINE_LENGTH = Integer.getInteger("Paper.maxSignLength", 80); // Paper

    private final User user;
    private final Guild guild;
    private final Material material;

    @PacketHandler(ignoreCancelled = true)
    public void onSignPacket(ServerboundSignUpdatePacket packet, PacketCall call) {
        if (!call.getUser().equals(user)) {
            return;
        }

        call.setCancelled(true);

        Component[] lines = capLength(packet.getLines(), call.getPlayer());
        var pos = packet.getPos();
        var loc = new Location(user.getWorld(), pos.getX(), pos.getY(), pos.getZ());

        user.getPlayer().sendBlockChange(loc, Material.AIR.createBlockData());

        guild.addMsgBoardPost(
                new GuildMessage(
                        material,
                        user.getUniqueId(),
                        System.currentTimeMillis(),
                        lines
                )
        );

        Tasks.runLater(() -> {
            GuildMenus.open(
                    GuildMenus.MAIN_MENU.getMessageBoard(),
                    user, guild
            );

            PacketListeners.unregister(this);
        }, 1);
    }

    private Component[] capLength(String[] lines, Player player) {
        Component[] result = new Component[4];

        for (int i = 0; i < result.length; i++) {
            String line = lines[i];

            if (line.length() > MAX_SIGN_LINE_LENGTH) {
                line = line.substring(0, MAX_SIGN_LINE_LENGTH);
            }

            result[i] = Text.renderString(player, line);
        }

        return result;
    }
}
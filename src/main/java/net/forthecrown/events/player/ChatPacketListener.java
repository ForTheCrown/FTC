package net.forthecrown.events.player;

import io.papermc.paper.adventure.ChatDecorationProcessor;
import net.forthecrown.core.FTC;
import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.forthecrown.utils.VanillaAccess;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Objects;

public class ChatPacketListener implements PacketListener {
    private static final Logger LOGGER = FTC.getLogger();

    @PacketHandler
    public void onChat(ServerboundChatPacket packet, PacketCall call) {
        call.setCancelled(true);

        if (containsIllegalCharacters(packet.message(), call)) {
            return;
        }

        // Handle player conversing
        if (call.getPlayer().isConversing()) {
            call.getExecutor().execute(() -> {
                call.getPlayer().acceptConversationInput(packet.message());
            });
            return;
        }

        var decoProcessor = new ChatDecorationProcessor(
                VanillaAccess.getServer(),
                VanillaAccess.getPlayer(call.getPlayer()),
                null,
                net.minecraft.network.chat.Component.literal(packet.message())
        );

        decoProcessor.process().whenComplete((result, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Chat decoration error!", throwable);
                return;
            }

            try {
                call.getPacketListener().chat(
                        packet.message(),
                        PlayerChatMessage.system(packet.message())
                                .withResult(Objects.requireNonNull(result)),
                        true
                );
            } catch (Throwable t) {
                LOGGER.error("Couldn't process chat!", t);
            }
        });
    }

    @PacketHandler
    public void onChatCommand(ServerboundChatCommandPacket packet, PacketCall call) {
        call.setCancelled(true);

        if (containsIllegalCharacters(packet.command(), call)) {
            return;
        }

        // Switch to main thread to execute command logic
        call.getExecutor().execute(() -> {
            // Prepend this onto it, or it won't find the command lol
            String command = "/" + packet.command();

            call.getPacketListener()
                    .handleCommand(command);
        });
    }

    /** Copies vanilla behaviour in regard to illegal character handling */
    private static boolean containsIllegalCharacters(String s, PacketCall call) {
        var player = call.getPlayer();

        if (ServerGamePacketListenerImpl.isChatMessageIllegal(s)) {
            call.getExecutor().execute(() -> {
                player.kick(
                        Component.translatable("multiplayer.disconnect.illegal_characters"),
                        PlayerKickEvent.Cause.ILLEGAL_CHARACTERS
                );
            });

            return true;
        }

        return false;
    }
}
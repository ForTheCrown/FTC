package net.forthecrown.user.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.forthecrown.user.packet.PacketListeners.call;

@RequiredArgsConstructor
public class PacketListenerChannelHandler extends ChannelDuplexHandler {
    private final Player player;

    // Server bound packets
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        // Call packet listeners, if call() returns true
        // packet shouldn't be read
        if (msg instanceof Packet<?> packet && call(packet, player)) {
            return;
        }

        super.channelRead(ctx, msg);
    }

    // Client bound packets
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Call packet listeners, if call() returns true
        // packet shouldn't be written
        if (msg instanceof Packet<?> packet && call(packet, player)) {
            return;
        }

        ctx.write(msg, promise);
    }
}
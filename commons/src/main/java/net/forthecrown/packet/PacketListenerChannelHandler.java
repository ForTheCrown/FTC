package net.forthecrown.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
class PacketListenerChannelHandler extends ChannelDuplexHandler {
  private final Player player;

  private final ListenersImpl listeners;

  // Server bound packets
  @Override
  public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg)
      throws Exception
  {
    // Call packet listeners, if call() returns true
    // packet shouldn't be read
    if (msg instanceof Packet<?> packet) {
      var call = listeners.call(packet, player);

      if (call == null) {
        super.channelRead(ctx, msg);
        return;
      }

      if (call.isCancelled()) {
        return;
      }

      if (call.getReplacementPacket() != null) {
        ctx.fireChannelRead(call.getReplacementPacket());
        return;
      }
    }

    super.channelRead(ctx, msg);
  }

  // Client bound packets
  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception
  {
    // Call packet listeners, if call() returns true
    // packet shouldn't be written
    if (!(msg instanceof Packet<?> packet)) {
      super.write(ctx, msg, promise);
      return;
    }

    var call = listeners.call(packet, player);

    if (call == null) {
      super.write(ctx, msg, promise);
      return;
    }

    if (call.isCancelled()) {
      return;
    }

    if (call.getReplacementPacket() != null) {
      ctx.write(call.getReplacementPacket(), promise);
      return;
    }

    super.write(ctx, msg, promise);
  }
}
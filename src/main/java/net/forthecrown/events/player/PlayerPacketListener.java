package net.forthecrown.events.player;

import com.sk89q.util.ReflectionUtil;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.UUID;
import net.forthecrown.core.FTC;
import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.world.level.GameType;
import org.apache.logging.log4j.Logger;

public class PlayerPacketListener implements PacketListener {
  private static final Logger LOGGER = FTC.getLogger();

  public static final String WRITER_FIELD = "h";

  @PacketHandler(ignoreCancelled = true)
  public void onGameModePacket(ClientboundPlayerInfoUpdatePacket packet,
                               PacketCall call
  ) {
    var actions = packet.actions();

    if (!actions.contains(Action.UPDATE_GAME_MODE)
        && !actions.contains(Action.ADD_PLAYER)
    ) {
      return;
    }

    LOGGER.debug("actions={}", actions);

    UUID target = call.getPlayer().getUniqueId();
    List<ClientboundPlayerInfoUpdatePacket.Entry>
        entries = new ObjectArrayList<>(packet.entries());

    var it = entries.listIterator();

    LOGGER.debug("Before loop");

    boolean changed = false;

    while (it.hasNext()) {
      var n = it.next();

      if (n.profileId().equals(target)) {
        LOGGER.debug("profileId == target: {}", target);
        continue;
      }

      if (n.gameMode() != GameType.SPECTATOR) {
        LOGGER.debug("Not spectator");
        continue;
      }

      LOGGER.debug("Setting entry");

      changed = true;
      it.set(
          new ClientboundPlayerInfoUpdatePacket.Entry(
              n.profileId(),
              n.profile(),
              n.listed(),
              n.latency(),
              GameType.DEFAULT_MODE,
              n.displayName(),
              n.chatSession()
          )
      );
    }

    LOGGER.debug("After loop");

    if (!changed) {
      LOGGER.debug("None changed");
      return;
    }

    var ioBuf = ByteBufAllocator.DEFAULT.ioBuffer();
    var buf = new FriendlyByteBuf(ioBuf);

    buf.writeEnumSet(
        packet.actions(),
        ClientboundPlayerInfoUpdatePacket.Action.class
    );

    buf.writeCollection(entries, (buf1, entry) -> {
      buf1.writeUUID(entry.profileId());

      for (var a : packet.actions()) {
        ClientboundPlayerInfoUpdatePacket.Action.Writer writer
            = ReflectionUtil.getField(a, WRITER_FIELD);

        writer.write(buf1, entry);
      }
    });

    buf.readerIndex(0);
    ClientboundPlayerInfoUpdatePacket replacementPacket
        = new ClientboundPlayerInfoUpdatePacket(buf);

    LOGGER.debug("Setting replacement packet: {}", replacementPacket);
    call.setReplacementPacket(replacementPacket);
    LOGGER.debug("END");
  }
}
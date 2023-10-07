package net.forthecrown.vanilla.packet;

import io.papermc.paper.adventure.PaperAdventure;
import java.util.Optional;
import net.forthecrown.packet.PacketCall;
import net.forthecrown.packet.PacketHandler;
import net.forthecrown.registry.Holder;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class EntityPacketListener {

  static final int ID_CUSTOM_NAME = 6;

  final ListenersImpl listeners;

  public EntityPacketListener(ListenersImpl listeners) {
    this.listeners = listeners;
  }

  @PacketHandler(ignoreCancelled = true)
  public void onEntityPacket(ClientboundSetEntityDataPacket packet, PacketCall call) {
    var registry = listeners.getEntityRenderers();

    if (registry.isEmpty()) {
      return;
    }

    var packedItems = packet.packedItems();
    int entityId = packet.id();

    var it = packedItems.listIterator();
    while (it.hasNext()) {
      var n = it.next();

      if (n.id() != ID_CUSTOM_NAME) {
        continue;
      }

      Optional<net.minecraft.network.chat.Component> vanillaChatOpt
          = (Optional<net.minecraft.network.chat.Component>) n.value();

      Component kyoriText = vanillaChatOpt.map(PaperAdventure::asAdventure).orElse(null);

      var stream = listeners.getEntityRenderers().stream()
          .map(Holder::getValue)
          .filter(entityRenderer -> entityRenderer.test(call.getPlayer(), entityId, kyoriText));

      Mutable<Component> mutableResult = new MutableObject<>(kyoriText);

      stream.forEach(r -> {
        Component result = r.render(call.getPlayer(), entityId, mutableResult.getValue());
        mutableResult.setValue(result);
      });

      DataValue<?> replacedValue;
      Component result = mutableResult.getValue();
      EntityDataSerializer<Optional> serializer = (EntityDataSerializer<Optional>) n.serializer();

      if (result == null) {
        replacedValue = new DataValue<>(n.id(), serializer, Optional.empty());
      } else {
        Optional opt = Optional.of(PaperAdventure.asVanilla(result));
        replacedValue = new DataValue<>(n.id(), serializer, opt);
      }

      it.set(replacedValue);
      return;
    }
  }
}

package net.forthecrown.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
class PacketHandlerList<T extends Packet<?>> {

  final Class<T> packetClass;
  final List<PacketExecutor<T>> executors = new ObjectArrayList<>();

  public void addExecutor(PacketExecutor<T> executor) {
    executors.add(executor);
    executors.sort(Comparator.comparingInt(PacketExecutor::getPriority));
  }

  public void removeAll(Class<?> c) {
    executors.removeIf(executor -> c == executor.getExecutorClass());
  }

  public PacketCall run(T packet, Player player) {
    PacketCall call = new PacketCall(player);

    for (var v : executors) {
      if (call.isCancelled() && v.isIgnoreCancelled()) {
        continue;
      }

      try {
        v.run(packet, call);
      } catch (Throwable t) {
        Throwable throwable = t;

        if (throwable instanceof InvocationTargetException exc) {
          throwable = exc.getCause();
        }

        ListenersImpl.LOGGER.error(
            "Error running packet listener executor '{}'",
            v.getExecutorClass().getSimpleName(),
            throwable
        );
      }
    }

    return call;
  }

  public boolean isEmpty() {
    return executors.isEmpty();
  }
}
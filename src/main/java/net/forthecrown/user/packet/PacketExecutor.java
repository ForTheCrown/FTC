package net.forthecrown.user.packet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.protocol.Packet;

@Getter
@RequiredArgsConstructor
public class PacketExecutor<T extends Packet<?>> {

  private final int priority;
  private final boolean ignoreCancelled;

  private final PacketListener listener;
  private final Method method;

  public void run(T packet, PacketCall call)
      throws InvocationTargetException, IllegalAccessException {
    method.invoke(listener, packet, call);
  }

  public Class<?> getExecutorClass() {
    return getListener().getClass();
  }
}
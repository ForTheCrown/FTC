package net.forthecrown.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Permissions;
import net.forthecrown.command.Exceptions;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class WorldAccessTestEvent extends Event implements Cancellable {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final CommandSender accessor;
  private final World world;

  private boolean accessible;

  private Component reason;

  public WorldAccessTestEvent(CommandSender accessor, World world) {
    this.accessor = accessor;
    this.world = world;
  }

  public static void testOrThrow(CommandSender user, World world, CommandSyntaxException noMessage)
      throws CommandSyntaxException
  {
    AccessResult result = testWorldAccess(user, world);

    if (result.accessible()) {
      return;
    }

    if (result.denyReason() == null) {
      throw noMessage;
    }

    throw Exceptions.create(result.denyReason());
  }

  public static AccessResult testWorldAccess(CommandSender user, World world) {
    if (user.hasPermission(Permissions.WORLD_BYPASS)) {
      return AccessResult.ACCESSIBLE;
    }

    WorldAccessTestEvent event = new WorldAccessTestEvent(user, world);
    event.callEvent();

    if (event.accessible) {
      return AccessResult.ACCESSIBLE;
    }

    Component reason = event.getReason();

    if (reason == null) {
      return AccessResult.NOT_ACCESSIBLE;
    }

    return AccessResult.denied(reason);
  }

  public void and(boolean b) {
    this.accessible &= b;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.accessible = !cancel;
  }

  @Override
  public boolean isCancelled() {
    return !accessible;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }

  public interface AccessResult {

    AccessResult ACCESSIBLE = new AccessResult() {
      @Override
      public boolean accessible() {
        return true;
      }

      @Override
      public Component denyReason() {
        return null;
      }
    };

    AccessResult NOT_ACCESSIBLE = new AccessResult() {
      @Override
      public boolean accessible() {
        return false;
      }

      @Override
      public Component denyReason() {
        return null;
      }
    };

    static AccessResult denied(Component reason) {
      Objects.requireNonNull(reason);

      return new AccessResult() {
        @Override
        public boolean accessible() {
          return false;
        }

        @Override
        public Component denyReason() {
          return reason;
        }
      };
    }

    boolean accessible();

    @Nullable Component denyReason();

    default Component denyReasonOr(Supplier<Component> supplier) {
      Objects.requireNonNull(supplier);
      return Optional.ofNullable(denyReason()).orElseGet(supplier);
    }

    default void orThrow(Supplier<Component> supplier) throws CommandSyntaxException {
      if (accessible()) {
        return;
      }

      throw Exceptions.create(denyReasonOr(supplier));
    }
  }
}

package net.forthecrown.usables.virtual;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.forthecrown.registry.Registry;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.ObjectType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public final class Triggers {
  private Triggers() {}

  static VirtualUsableManager manager;

  static void registerAll(Registry<ObjectType<? extends Trigger>> r) {
    EntityAction.registerAll(r);
    BlockAction.registerAll(r);
    RegionAction.registerAll(r);
  }

  public static Iterator<VirtualUsable> mapReferences(
      List<String> refList,
      VirtualUsableManager manager
  ) {
    return refList.stream()
        .map(manager::getUsable)
        .filter(Objects::nonNull)
        .iterator();
  }

  public static void runReferences(
      List<String> refList,
      Player player,
      @Nullable Cancellable cancellable,
      @Nullable Consumer<Interaction> interactionConsumer,
      @Nullable Consumer<Interaction> postExec
  ) {
    runReferences(refList, manager, player, cancellable, interactionConsumer, postExec);
  }

  public static void runReferences(
      List<String> refList,
      VirtualUsableManager manager,
      Player player,
      @Nullable Cancellable cancellable,
      @Nullable Consumer<Interaction> interactionConsumer,
      @Nullable Consumer<Interaction> postExec
  ) {
    var it = mapReferences(refList, manager);

    if (!it.hasNext()) {
      return;
    }

    while (it.hasNext()) {
      VirtualUsable usable = it.next();
      Interaction interaction = usable.createInteraction(player);

      if (interactionConsumer != null) {
        interactionConsumer.accept(interaction);
      }

      usable.interact(interaction);

      if (postExec != null) {
        postExec.accept(interaction);
      }

      if (cancellable != null && interaction.getBoolean("cancelVanilla").orElse(false)) {
        cancellable.setCancelled(true);
        return;
      }
    }
  }
}

package net.forthecrown.usables.listeners;

import static net.forthecrown.events.Events.register;

import net.forthecrown.Loggers;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsablesPlugin;
import net.forthecrown.usables.objects.InWorldUsable;
import net.forthecrown.utils.Cooldown;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.slf4j.Logger;

public final class UsablesListeners {
  private UsablesListeners() {}

  private static final Logger LOGGER = Loggers.getLogger();

  public static void registerAll(UsablesPlugin plugin) {
    register(new BlockListener());
    register(new ItemListener());
    register(new EntityListener());
    register(new JoinListener(plugin));
    register(new ServerLoadListener());
  }

  static void execute(InWorldUsable usable, Interaction interaction, Cancellable cancellable) {
    var player = interaction.player();

    if (player.getGameMode() == GameMode.SPECTATOR) {
      return;
    }

    if (Cooldown.containsOrAdd(player, 5)) {
      return;
    }

    usable.interact(interaction);
    usable.save();

    if (interaction.getBoolean("cancelVanilla").orElse(usable.isCancelVanilla())) {
      cancellable.setCancelled(true);
    }
  }

  static void executeInteract(InWorldUsable usable, Player player, PlayerInteractEvent event) {
    var interaction = usable.createInteraction(player);
    var ctx = interaction.context();
    ctx.put("hand", event.getHand());
    ctx.put("useItem", event.useItemInHand());
    ctx.put("useBlock", event.useInteractedBlock());

    execute(usable, interaction, event);

    interaction.getValue("useItem", Result.class).ifPresent(event::setUseItemInHand);
    interaction.getValue("useBlock", Result.class).ifPresent(event::setUseInteractedBlock);
  }
}

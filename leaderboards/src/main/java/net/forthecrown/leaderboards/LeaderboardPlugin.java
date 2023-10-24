package net.forthecrown.leaderboards;

import net.forthecrown.BukkitServices;
import net.forthecrown.events.Events;
import net.forthecrown.leaderboards.listeners.EntityListeners;
import net.forthecrown.packet.EntityRenderer;
import net.forthecrown.packet.PacketListeners;
import net.forthecrown.registry.Registry;
import org.bukkit.plugin.java.JavaPlugin;

public class LeaderboardPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    ServiceImpl service = new ServiceImpl();
    BukkitServices.register(LeaderboardService.class, service);

    Registry<EntityRenderer> entityRenderers = PacketListeners.listeners().getEntityRenderers();
    BoardRenderer renderer = new BoardRenderer(service);
    entityRenderers.register("leaderboards", renderer);

    Events.register(new EntityListeners(service));
  }

  @Override
  public void onDisable() {
    var entityRenderers = PacketListeners.listeners().getEntityRenderers();
    entityRenderers.remove("leaderboards");
  }
}

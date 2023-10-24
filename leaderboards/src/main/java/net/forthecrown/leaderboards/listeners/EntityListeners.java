package net.forthecrown.leaderboards.listeners;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.forthecrown.Loggers;
import net.forthecrown.leaderboards.BoardData;
import net.forthecrown.leaderboards.BoardImpl;
import net.forthecrown.leaderboards.ServiceImpl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.slf4j.Logger;

public class EntityListeners implements Listener {

  private static final Logger LOGGER = Loggers.getLogger();

  private final ServiceImpl service;

  public EntityListeners(ServiceImpl service) {
    this.service = service;
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityAddToWorld(EntityAddToWorldEvent event) {
    Entity entity = event.getEntity();

    if (!(entity instanceof TextDisplay display)) {
      return;
    }

    if (!entity.getPersistentDataContainer().has(BoardImpl.LEADERBOARD_KEY)) {
      return;
    }

    String boardName = entity.getPersistentDataContainer()
        .get(BoardImpl.LEADERBOARD_KEY, PersistentDataType.STRING);

    service.getBoard(boardName)
        .ifPresentOrElse(
            leaderboard -> {
              int id = display.getEntityId();
              BoardData data = new BoardData(id, leaderboard, display);
              service.getByEntityId().put(id, data);
            },

            () -> {
              LOGGER.warn(
                  "Loaded entity {} that was attached to leaderboard '{}', which doesn't exist",
                  entity, boardName
              );
            }
        );
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
    Entity entity = event.getEntity();
    int id = entity.getEntityId();
    service.getByEntityId().remove(id);
  }
}

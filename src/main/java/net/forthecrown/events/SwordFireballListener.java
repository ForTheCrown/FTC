package net.forthecrown.events;

import io.papermc.paper.event.entity.EntityMoveEvent;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SwordFireballListener implements Listener {
  public static final int FIREBALL_MAX_DIST = 40;
  public static final int MAX_DIST_SQ = FIREBALL_MAX_DIST * FIREBALL_MAX_DIST;

  public static final String FIREBALL_TAG = "SwordFireBall";

  @EventHandler(ignoreCancelled = true)
  public void onEntityMove(EntityMoveEvent event) {
    var entity = event.getEntity();

    if (!entity.getScoreboardTags().contains(FIREBALL_TAG)) {
      return;
    }

    var dest = event.getTo();
    var origin = entity.getOrigin();

    if (origin == null || !Objects.equals(dest.getWorld(), origin.getWorld())) {
      return;
    }

    double distSq = dest.distanceSquared(origin);

    if (distSq >= MAX_DIST_SQ) {
      entity.remove();
    }
  }
}
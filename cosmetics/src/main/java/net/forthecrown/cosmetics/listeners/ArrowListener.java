package net.forthecrown.cosmetics.listeners;

import com.destroystokyo.paper.ParticleBuilder;
import java.util.function.Consumer;
import net.forthecrown.cosmetics.CosmeticData;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitTask;

public class ArrowListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onProjectileLaunch(ProjectileLaunchEvent event) {
    if (!(event.getEntity() instanceof AbstractArrow arrow) || arrow instanceof Trident) {
      return;
    }

    if (!(arrow.getShooter() instanceof Player player)) {
      return;
    }

    User user = Users.get(player);
    CosmeticData data = user.getComponent(CosmeticData.class);
    Particle cosmetic = data.getValue(Cosmetics.ARROW_EFFECTS);

    if (cosmetic == null) {
      return;
    }

    Tasks.runTimer(new ArrowScheduler(arrow, player, cosmetic), 1, 1);
  }

  public static class ArrowScheduler implements Consumer<BukkitTask> {

    private final AbstractArrow arrow;
    private final ParticleBuilder builder;

    public ArrowScheduler(AbstractArrow arrow, Player player, Particle effect) {
      this.arrow = arrow;

      builder = new ParticleBuilder(effect)
          .location(arrow.getLocation())
          .source(player)
          .extra(0);
    }

    @Override
    public void accept(BukkitTask task) {
      builder.location(arrow.getLocation()).spawn();

      if (arrow.isDead() || arrow.isOnGround()) {
        Tasks.cancel(task);
      }
    }
  }
}
package net.forthecrown.cosmetics;

import net.forthecrown.utils.Tasks;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DeathEffect {

  DeathEffect ENDER_RING = loc -> {
    loc.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.5f, 1f);
    double y2 = loc.getY();

    for (int i = 0; i < 3; i++) {
      loc.setY(y2 + i);

      for (int j = 0; j < 5; j++) {
        loc.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 1);
      }
    }
  };

  DeathEffect EXPLOSION = loc -> {
    loc.getWorld().playEffect(loc, Effect.END_GATEWAY_SPAWN, 1);
  };

  DeathEffect SOUL_DEATH = loc -> {
    double x = loc.getX();
    double y = loc.getY() + 1;
    double z = loc.getZ();
    loc.getWorld().playEffect(loc, Effect.ZOMBIE_INFECT, 1);

    for (int i = 0; i < 50; i++) {
      loc.getWorld()
          .spawnParticle(Particle.SOUL, x, y + (((float) i) / 50), z, 1, 0.5, 0, 0.5, 0.05);
    }
  };

  DeathEffect TOTEM = loc -> {
    double x = loc.getX();
    double y = loc.getY() + 1;
    double z = loc.getZ();

    for (int i = 0; i < 20; i++) {
      Tasks.runLater(() -> {
        for (int i1 = 0; i1 < 2; i1++) {
          loc.getWorld().spawnParticle(Particle.TOTEM, x, y, z, 5, 0, 0, 0, 0.4);
        }
      }, i);
    }

    loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1, 1);
  };

  void activate(@NotNull Location location);
}
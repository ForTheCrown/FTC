import "@ftc.cosmetics.travel.TravelUtil";
import "@bukkit.Particle";

const RADIUS = 1.5;

function onRightClick(player, clicked) {
  let living = player.getLocation().getNearbyLivingEntities(RADIUS);
  living.removeIf(entity => {
    return entity.equals(player);
  });

  if (living.isEmpty()) {
    return false;
  }

  living.forEach(entity => {
    player.attack(entity);
  });

  spawnEffects(player.getLocation(), living);
  return true;
}

function spawnEffects(playerLocation, entityList) {
  const points = Math.min(entityList.size() * 2, 15);

  if (points <= 1) {
    return;
  }

  TravelUtil.spawnInCircle(playerLocation, 0.5, 1, points, Particle.SWEEP_ATTACK, 1);
}
import "@ftc.cosmetics.travel.TravelUtil";
import "@bukkit.Particle";

import "@kyori.sound.Sound";
import "@kyori.key.Key";

const RADIUS = 2;
const SOUND_KEY = Key.key("entity.player.attack.sweep");
const SOUND = Sound.sound()
    .type(SOUND_KEY)
    .build();

function onRightClick(player, clicked) {
  let living = player.getEyeLocation().getNearbyLivingEntities(RADIUS + level / 2);
  living.removeIf(entity => {
    return entity.equals(player);
  });

  if (living.isEmpty()) {
    return false;
  }

  let dmg = getDamage();

  living.forEach(e => {
    e.damage(dmg, player);
  });

  spawnEffects(player.getLocation(), living);
  player.playSound(SOUND);

  return true;
}

function getDamage() {
  let swordLevel = getSwordLevel();
  return (swordLevel / 2) + (level * 3.3) + 2;
}

function getSwordLevel() {
  return royalSword.getRank().getViewerRank();
}

function spawnEffects(playerLocation, entityList) {
  const points = Math.min(entityList.size() * 2, 15);

  if (points <= 1) {
    return;
  }

  TravelUtil.spawnInCircle(playerLocation, 0.5, 1, points, Particle.SWEEP_ATTACK, 1);
}
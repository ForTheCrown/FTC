import "@bukkit.entity.Fireball";
import "@bukkit.entity.Enemy";
import "@bukkit.entity.Player";
import "@bukkit.block.Block";

import "@kyori.sound.Sound";
import "@kyori.key.Key";

import "@ftc.events.SwordFireballListener"

const SOUND_KEY = Key.key("item.firecharge.use");

const SOUND = Sound.sound()
    .type(SOUND_KEY)
    .build();

const YIELD_MOD = 1.15;

const SCOREBOARD_TAG = "swordUpgrade_fireball";

function onRightClick(player, clicked) {
  if (!shouldLaunch(clicked)) {
    return false;
  }

  let velocity = player.getLocation()
      .getDirection()
      .multiply(level);

  let spawned = player.launchProjectile(Fireball.class, velocity, ball => {
    // Required so that SwordFireballListener can despawn it after 40 blocks
    ball.addScoreboardTag(SwordFireballListener.FIREBALL_TAG);
    ball.setYield(YIELD_MOD * level);
    ball.addScoreboardTag(SCOREBOARD_TAG);
  });

  // Removed in EntitySpawnEvent or something similar
  if (!spawned.isDead()) {
    player.playSound(SOUND);
    return true;
  }

  return false;
}

function shouldLaunch(clicked) {
  if (clicked == null) {
    return true;
  }

  return clicked instanceof Enemy || clicked instanceof Player;
}
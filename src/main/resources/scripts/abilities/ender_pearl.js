import "@bukkit.entity.EnderPearl"
import "@kyori.key.Key";
import "@kyori.sound.Sound";

const SOUND_KEY = Key.key("entity.ender_pearl.throw");

const SOUND = Sound.sound()
    .type(SOUND_KEY)
    .pitch(0.5)
    .build();

function onRightClick(player, clicked) {
  // Launch EnderPearl with the ability's
  // level as the velocity multiplier
  let world = player.getWorld();

  let mod = 1;

  if (level == 2) {
    mod = 0.66;
  } else if (level > 2) {
    mod = 0.66;
  }

  let velocity = player.getLocation().getDirection().multiply(level * mod);
  let projectile = player.launchProjectile(EnderPearl.class, velocity);

  if (!projectile.isDead()) {
    player.playSound(SOUND);
    return true;
  }

  return false;
}
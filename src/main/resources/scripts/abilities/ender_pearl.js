import "@bukkit.entity.EnderPearl"

function onRightClick(player, clicked) {
  if (clicked != null) {
    return false;
  }

  // Launch EnderPearl with the ability's
  // level as the velocity multiplier
  let world = player.getWorld();
  let velocity = player.getLocation().getDirection().multiply(level);

  let projectile = player.launchProjectile(EnderPearl.class, velocity);
  return !projectile.isDead();
}
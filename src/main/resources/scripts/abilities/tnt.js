import "@bukkit.entity.TNTPrimed";
import "@bukkit.block.Block";

function onRightClick(player, clicked) {
  if (clicked != null) {
    return false;
  }

  let loc = player.getLocation();
  let w = loc.getWorld();

  let spawned = w.spawn(loc, TNTPrimed.class);

  // Could be removed by event
  if (spawned.isDead()) {
    return false;
  }

  const shouldThrow = !player.isSneaking();
  if (shouldThrow) {
    let velocity = loc.getDirection().multiply(level);
    spawned.setVelocity(velocity);
  }

  return true;
}
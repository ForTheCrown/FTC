import "@bukkit.entity.TNTPrimed";
import "@bukkit.block.Block";

const SCOREBOARD_TAG = "swordUpgrade_tnt";

function onRightClick(player, clicked) {
  if (clicked != null) {
    return false;
  }

  let loc = player.getLocation();
  let w = loc.getWorld();

  let spawned = w.spawn(loc, TNTPrimed.class, tnt => {
    tnt.addScoreboardTag(SCOREBOARD_TAG);
  });

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
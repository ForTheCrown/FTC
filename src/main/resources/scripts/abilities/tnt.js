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
    let mod = 1;

    if (level == 2) {
        mod = 0.6;
    } else if (level > 2) {
        mod = 0.5;
    }

    let velocity = loc.getDirection().multiply(level * mod);
    spawned.setVelocity(velocity);
  }

  return true;
}
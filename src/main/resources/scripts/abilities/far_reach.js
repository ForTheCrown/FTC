import "@bukkit.FluidCollisionMode";

const VANILLA_REACH = 4;

function onLeftClick(player, clicked) {
  // No clicked means no block or entity, if block clicked,
  // then no entity in the way, if entity clicked, why tf we here
  if (clicked != null) {
    return false;
  }

  let distance = VANILLA_REACH + 1 + (level / 2);
  let origin = player.getEyeLocation();
  let direction = origin.getDirection();
  let world = player.getWorld();

  let result = world.rayTrace(
    origin,
    direction,
    distance,
    FluidCollisionMode.NEVER,
    false,
    0.0,

    // Filter
    entity => {
      return !entity.equals(player);
    }
  );

  if (result == null || result.getHitEntity() == null) {
    return false;
  }

  player.attack(result.getHitEntity());
  return true;
}
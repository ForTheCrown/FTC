const VANILLA_REACH = 3;

function onLeftClick(player, clicked) {
  // No clicked means no block or entity, if block clicked,
  // then no entity in the way, if entity clicked, why tf we here
  if (clicked != null) {
    return false;
  }

  let distance = VANILLA_REACH + level;
  let result = player.rayTraceEntities(distance, false);

  if (result == null || result.getHitEntity() == null) {
    return false;
  }

  player.attack(result.getHitEntity());
  return true;
}
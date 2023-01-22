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

  // TODO: some effects and sound

  return true;
}
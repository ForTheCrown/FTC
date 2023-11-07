function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // EntityDamageByEntityEvent

  // Check if entity was damaged by lightning
  var damager = event.getDamager();
  if (damager.getType() == EntityType.LIGHTNING) {
    var lightningStrike = damager;

    // Check if source of lightning is player
    var caster = lightningStrike.getCausingEntity();
    if (caster != null && caster.getType() == EntityType.PLAYER) {
      handle.givePoint(caster);
    }
  }
}
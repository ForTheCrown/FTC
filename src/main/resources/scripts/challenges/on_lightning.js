function canComplete(user) {
    return user.getGuild() != null;
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

function onComplete(user) {
    logger.warn("Lightning challenge completed by: {}", user.getName())
}
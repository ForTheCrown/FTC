function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // EntityDeathEvent
    var player = event.getEntity().getKiller();

    if (player != null && event.getEntity().getType() == EntityType.WITHER) {
        handle.givePoint(player);
    }
}

function onComplete(user) {
    logger.warn("Wither challenge completed by: {}", user.getName())
}
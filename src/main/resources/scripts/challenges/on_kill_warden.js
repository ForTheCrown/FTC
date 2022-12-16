function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // EntityDeathEvent
    var player = event.getEntity().getKiller();

    if (player != null && event.getEntity().getType() == EntityType.WARDEN) {
        handle.givePoint(player);
    }
}
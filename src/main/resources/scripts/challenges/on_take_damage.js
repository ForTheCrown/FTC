function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    if (event.getEntity().getType() != EntityType.PLAYER) {
        return;
    }

    // EntityDamageEvent
    var dmg = event.getFinalDamage() / 2;
    handle.givePoints(event.getEntity(), dmg);
}
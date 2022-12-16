function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // EntityDamageEvent
    var dmg = (float)(event.getFinalDamage() / 2);

    handle.givePoints(event.getPlayer(), dmg);
}
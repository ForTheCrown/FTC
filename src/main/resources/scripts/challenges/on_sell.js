function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // "Custom" event should be triggered when selling items in /shop
    // (points = Rhines earned)
    handle.givePoints(event.getPlayer(), event.getEarned());
}

function onComplete(user) {
    logger.warn("Sell challenge completed by: {}", user.getName())
}
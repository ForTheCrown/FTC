function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(player, handle) {
    // "Custom" event should only triggered when buying
    // in a shop they haven't shopped in before (today).
    // Input should directly be a Player object
    handle.givePoint(player);
}

function onComplete(user) {
    logger.warn("Buy challenge completed by: {}", user.getName())
}
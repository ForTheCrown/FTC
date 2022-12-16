function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerBedLeaveEvent
    handle.givePoint(event.getPlayer());
}

function onComplete(user) {
    logger.warn("Sleep challenge completed by: {}", user.getName())
}
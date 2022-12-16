function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerAdvancementDoneEvent
    handle.givePoint(event.getPlayer());
}

function onComplete(user) {
    logger.warn("Advancement challenge completed by: {}", user.getName())
}
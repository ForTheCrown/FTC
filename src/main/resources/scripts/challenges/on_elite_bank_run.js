function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // "Custom" event should be triggered when finishing an elite bank run
    handle.givePoint(event.getPlayer());
}

function onComplete(user) {
    logger.warn("Elite Bank Run challenge completed by: {}", user.getName())
}
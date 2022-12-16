function canComplete(user) {
    return user.getGuild() != null;
}

function onComplete(user) {
    logger.warn("Vote challenge completed by: {}", user.getName())
}
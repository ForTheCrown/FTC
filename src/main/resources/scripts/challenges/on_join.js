function canComplete(user) {
    return user.getGuild() != null;
}

function onActivate(handle) {
    Users.getOnline().forEach(user => {
        handle.givePoint(user);
    });
}

function onComplete(user) {
    logger.warn("Login challenge completed by: {}", user.getName())
}
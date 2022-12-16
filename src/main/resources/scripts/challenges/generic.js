function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    handle.givePoint(event.getPlayer());
}
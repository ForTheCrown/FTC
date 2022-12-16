function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    handle.givePoints(event.getPlayer(), event.getAmount());
}
function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    let player = event.getPlayer();

    if (player == null) {
        //logger.warn("Player in event {} is null", event.getClass());
        return;
    }

    handle.givePoint(player);
}
function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerBedLeaveEvent
    handle.givePoint(event.getPlayer());
}
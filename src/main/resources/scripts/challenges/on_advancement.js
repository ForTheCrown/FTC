function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerAdvancementDoneEvent
    handle.givePoint(event.getPlayer());
}
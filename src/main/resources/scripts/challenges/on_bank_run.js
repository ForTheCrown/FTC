function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // "Custom" event should be triggered when finishing a bank run
    handle.givePoint(event.getPlayer());
}
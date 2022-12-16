function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerItemConsumeEvent
    if (event.getItem().getType() == Material.SUSPICIOUS_STEW) {
        handle.givePoint(event.getPlayer());
    }
}
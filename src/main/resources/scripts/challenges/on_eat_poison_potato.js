function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerItemConsumeEvent
    if (event.getItem().getType() == Material.POISONOUS_POTATO) {
        handle.givePoint(event.getPlayer());
    }
}
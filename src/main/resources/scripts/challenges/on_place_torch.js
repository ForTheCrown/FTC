function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // BlockPlaceEvent
    var type = event.getBlockPlaced().getType();

    if (type == Material.TORCH || type == Material.WALL_TORCH) {
        handle.givePoint(event.getPlayer());
    }
}
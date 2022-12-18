function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // BlockBreakEvent
    var block = event.getBlock();

    if (block != null && block.getType().name().contains("_ORE")) {
        let player = event.getPlayer();
        handle.givePoint(player);
    }
}
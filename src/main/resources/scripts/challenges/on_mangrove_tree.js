function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // StructureGrowEvent
    if (event.getSpiecies() == TreeType.MANGROVE || event.getSpiecies() == TreeType.TALL_MANGROVE) {
        handle.givePoint(event.getPlayer());
    }
}
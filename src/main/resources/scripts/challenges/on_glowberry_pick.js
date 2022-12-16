function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerHarvestBlockEvent
    var harvested = event.getHarvestedBlock().getType();

    if (harvested == Material.CAVE_VINES || harvested == Material.CAVE_VINES_PLANT) {
        handle.givePoint(event.getPlayer());
    }
}
function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // EnchantItemEvent
    if (event.getExpLevelCost() >= 30) {
        handle.givePoint(event.getEnchanter());
    }
}
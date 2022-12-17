const TreeType = Java.type("org.bukkit.TreeType");

function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // StructureGrowEvent
    if (event.getSpecies() == TreeType.MANGROVE || event.getSpecies() == TreeType.TALL_MANGROVE) {
        handle.givePoint(event.getPlayer());
    }
}
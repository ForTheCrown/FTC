const TreeType = Java.type("org.bukkit.TreeType");

function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // StructureGrowEvent
    if (event.getSpecies() == TreeType.TREE || event.getSpiecies() == TreeType.BIG_TREE) {
        handle.givePoint(event.getPlayer());
    }
}
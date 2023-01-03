const TreeType = Java.type("org.bukkit.TreeType");

function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    let player = event.getPlayer();
    
    if (player == null) {
        return;
    }
    
    // StructureGrowEvent
    if (event.getSpecies() == TreeType.TREE || event.getSpecies() == TreeType.BIG_TREE) {
        handle.givePoint(event.getPlayer());
    }
}

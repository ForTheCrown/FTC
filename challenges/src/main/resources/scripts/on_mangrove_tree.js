import TreeType from "org.bukkit.TreeType";

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  let player = event.getPlayer();
  
  if (player == null) {
    return;
  }
  
  // StructureGrowEvent
  if (event.getSpecies() == TreeType.MANGROVE || event.getSpecies() == TreeType.TALL_MANGROVE) {
    handle.givePoint(player);
  }
}

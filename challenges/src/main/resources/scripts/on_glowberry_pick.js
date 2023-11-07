function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // PlayerHarvestBlockEvent
  var harvested = event.getHarvestedBlock().getType();

  if (harvested == Material.CAVE_VINES || harvested == Material.CAVE_VINES_PLANT) {
    handle.givePoint(event.getPlayer());
  }
}
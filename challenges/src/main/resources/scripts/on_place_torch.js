function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // BlockPlaceEvent
  var type = event.getBlockPlaced().getType();

  if (type == Material.TORCH || type == Material.WALL_TORCH) {
    handle.givePoint(event.getPlayer());
  }
}
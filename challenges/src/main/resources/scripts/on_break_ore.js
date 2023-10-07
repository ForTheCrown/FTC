function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // BlockBreakEvent
  var block = event.getBlock();

  if (block != null && block.getType().name().contains("_ORE")) {
    let player = event.getPlayer();
    handle.givePoint(player);
  }
}
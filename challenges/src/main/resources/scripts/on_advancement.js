function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // PlayerAdvancementDoneEvent
  handle.givePoint(event.getPlayer());
}
function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // "Custom" event should be triggered when selling items in /shop
  // (points = Rhines earned)
  handle.givePoints(event.getUser(), event.getEarned());
}
function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  event.getWinners().forEach(player => {
    handle.givePoint(player);
  })
}
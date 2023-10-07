function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // EnchantItemEvent
  if (event.getExpLevelCost() >= 30) {
    handle.givePoint(event.getEnchanter());
  }
}
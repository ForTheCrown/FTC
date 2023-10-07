function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // PlayerItemConsumeEvent
  if (event.getItem().getType() == Material.POISONOUS_POTATO) {
    handle.givePoint(event.getPlayer());
  }
}
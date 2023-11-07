function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // PlayerItemConsumeEvent
  if (event.getItem().getType() == Material.MILK_BUCKET) {
    // Clear effect needed
    if (!event.getPlayer().getActivePotionEffects().isEmpty()) {
      handle.givePoint(event.getPlayer());
    }
  }
}
const TYPE = parseType();

function parseType() {
  if (args.length != 1) {
    return null;
  }

  return Material.matchMaterial(args[0]);
}

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // PlayerItemConsumeEvent
  if (TYPE == null || event.getItem().getType() == TYPE) {
    handle.givePoint(event.getPlayer());
  }
}
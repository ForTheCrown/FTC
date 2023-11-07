const TYPE = parseType();

function parseType() {
  logger.debug("args.length={}", args.length);
  logger.debug("args={}", args);

  if (args.length != 1) {
    throw "Expected entity type to be set in arguments, args: " + args;
  }

  return EntityType.valueOf(args[0].toUpperCase());
}

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // EntityDeathEvent
  var player = event.getEntity().getKiller();

  if (player != null && (TYPE == null || event.getEntity().getType() == TYPE)) {
    handle.givePoint(player);
  }
}
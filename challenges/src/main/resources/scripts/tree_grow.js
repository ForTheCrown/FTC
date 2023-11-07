import "org.bukkit.TreeType";

const TYPE = parseType();

function parseType() {
  if (args.length != 1) {
  throw "Expected at least 1 tree type, args: " + args;
  }

  return args[0].toUpperCase();
}

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

// StructureGrowEvent
function onEvent(event, handle) {
  let player = event.getPlayer();
  
  if (player == null || !event.getSpecies().name().contains(TYPE)) {
    return;
  }

  handle.givePoint(player);
}
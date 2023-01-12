const TreeType = Java.type("org.bukkit.TreeType");

const TYPE = parseType();

function parseType() {
  if (inputs.length != 1) {
    throw "Expected at least 1 tree type, args: " + inputs;
  }

  return inputs[0].toUpperCase();
}

function canComplete(user) {
    return user.getGuild() != null;
}

// StructureGrowEvent
function onEvent(event, handle) {
    let player = event.getPlayer();
    
    if (player == null || !event.getSpecies().name().contains(TYPE)) {
        return;
    }

    handle.givePoint(player);
}
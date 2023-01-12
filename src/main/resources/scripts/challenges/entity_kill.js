const TYPE = parseType();

function parseType() {
    if (inputs.length != 1) {
        throw "Expected entity type to be set in arguments, args: " + inputs;
    }

    return EntityType.valueOf(inputs[0].toUpperCase());
}

function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // EntityDeathEvent
    var player = event.getEntity().getKiller();

    if (player != null
          && (TYPE == null || event.getEntity().getType() == TYPE)
    ) {
        handle.givePoint(player);
    }
}
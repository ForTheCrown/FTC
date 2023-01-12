const TYPE = parseType();

function parseType() {
    if (inputs.length != 1) {
        return null;
    }

    return Material.matchMaterial(inputs[0]);
}

function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerItemConsumeEvent
    if (TYPE == null || event.getItem().getType() == TYPE) {
        handle.givePoint(event.getPlayer());
    }
}
function onEvent(event, handle) {
    if (!event.hasChangedPosition()) {
        return;
    }

    var from = event.getFrom();
    var to = event.getTo();

    if (!isValidWalkLocation(from) 
            || !isValidWalkLocation(to)
            || !event.getPlayer().isOnGround()
    ) {
        return;
    }

    var dist = from.distance(to);
    handle.givePoints(event.getPlayer(), dist);
}

function isValidWalkLocation(location) {
    var below = location.clone()
            .subtract(0, 1, 0)
            .getBlock();

    return !below.isEmpty() 
            && !below.isLiquid();
}

function canComplete(user) {
    return user.getGuild() != null;
}
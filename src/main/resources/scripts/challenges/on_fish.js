const FishingState = Java.type("org.bukkit.event.player.PlayerFishEvent.State");

function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    if (event.getState() != FishingState.CAUGHT_FISH) {
        return;
    }

    handle.givePoint(event.getPlayer());
}
var PlaytimeChallengeListener = Java.type("net.forthecrown.events.player.PlaytimeChallengeListener");

function onEvent(player, handle) {
    handle.givePoint(player);
}

function onActivate(handle) {
    PlaytimeChallengeListener.setActive(true);
}

function onReset(handle) {
    PlaytimeChallengeListener.setActive(false);
}
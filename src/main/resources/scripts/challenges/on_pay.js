function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // "Custom" event should only triggered when spending manually to
    // complete this challenge, basically a buy-guildExp-with-Rhines "challenge"
    handle.givePoint(event.getPlayer());
}
function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    event.getWinners().forEach(player => {
        handle.givePoint(player);
    })
}
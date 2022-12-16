function canComplete(user) {
    return user.getGuild() != null;
}

function onActivate(handle) {
    Users.getOnline().forEach(user => {
        handle.givePoint(user);
    });
}
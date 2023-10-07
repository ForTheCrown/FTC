function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onActivate(handle) {
  Users.getOnline().forEach(user => {
    handle.givePoint(user);
  });
}
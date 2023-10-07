function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(user, handle) {
  // "Custom" event should only triggered when spending manually to
  // complete this challenge, basically a buy-guildExp-with-Rhines "challenge"

  let goal = challenge.getGoal(user);

  handle.givePoints(user, goal);
  user.removeBalance(goal);
}
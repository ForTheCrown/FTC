import Guilds from "@ftc.guilds.Guilds";

function canComplete(user) {
  return Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  let ch = event.challenge;
  let category = ch.streakCategory;

  if (!category.name().equals("ITEMS")) {
    return;
  }

  handle.givePoint(event.user);
}
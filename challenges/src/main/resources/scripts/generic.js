import Guilds from "@ftc.guilds.Guilds";

function canComplete(user) {
  return Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  let player = event.getPlayer();

  if (player == null) {
    //logger.warn("Player in event {} is null", event.getClass());
    return;
  }

  handle.givePoint(player);
}
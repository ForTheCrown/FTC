import FishingState from "org.bukkit.event.player.PlayerFishEvent.State";

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  if (event.getState() != FishingState.CAUGHT_FISH) {
    return;
  }

  handle.givePoint(event.getPlayer());
}
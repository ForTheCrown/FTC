import PlayerQuitEvent from "org.bukkit.event.player.PlayerQuitEvent";

const TICK_DELAY = 20 * 60;
const map = new Map;

// PlayerJoin Event
function onEvent(event, handle) {
  startFor(event.player);
}

function onQuit(event) {
  let entry = map[event.player.uniqueId];

  if (entry == null) {
    return;
  }

  entry.stopTask();
  map.delete(event.player.uniqueId);
}

function onActivate(handle) {
  clear();
  events.register(PlayerQuitEvent, onQuit);

  Bukkit.getOnlinePlayers().forEach(startFor);
}

function onReset(handle) {
  clear();
  events.unregisterFrom(PlayerQuitEvent);
}

function clear() {
  map.forEach((key, value, map) => {
    value.stopTask();
  });
  map.clear();
}

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function startFor(player) {
  let entry = map[player.uniqueId];

  if (entry != null) {
    return;
  }

  entry = new PlayerEntry(player);
  map[player.uniqueId] = entry;

  entry.startTask();
}

function PlayerEntry(player) {

  this.user = Users.get(player);
  this.task = null;
  this.timer = 0;

  this.startTask = () => {
    this.task = scheduler.runTimer(1, 1, this.tick)
  };

  this.stopTask = () => {
    if (this.task == null) {
      return
    }

    this.task.cancel();
  };

  this.tick = () => {
    if (this.user.isAfk()) {
      return;
    }

    this.timer++;

    if (this.timer < TICK_DELAY) {
      return;
    }

    this.timer = 0;
    handle.givePoint(this.user);
  };
}
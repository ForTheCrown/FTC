const Playtime = Java.type("net.forthecrown.events.dynamic.PlayerPlaytimeListener");
const PlayerQuitEvent = Java.type("org.bukkit.event.player.PlayerQuitEvent");

const TICK_DELAY = 20 * 60;
var tracker;

// PlayerJoin Event
function onEvent(event, handle) {
    getTracker().startTask(event.getPlayer());
}

function onQuit(event) {
    getTracker().stopTask(event.getPlayer());
}

function onActivate(handle) {
    getTracker().clear();
    events.register("onQuit", PlayerQuitEvent);
}

function onReset(handle) {
    getTracker().clear();
    events.unregister("onQuit");
}

function canComplete(user) {
    return user.getGuild() != null;
}

function getTracker() {
    if (tracker != null) {
        return tracker;
    }

    tracker = new Playtime(_challengeHandle, TICK_DELAY);
    return tracker;
}
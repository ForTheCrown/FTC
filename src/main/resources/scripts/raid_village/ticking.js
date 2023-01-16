// input: main, from main.js

const TICK_INTERVAL = 1;

let task;
let tickCount = 0;

function beginTicking() {
  if (isTicking()) {
    throw `Already ticking, tickCount = ${tickCount}`;
  }

  tickCount = 0;

  this.task = scheduler.runTimer(TICK_INTERVAL, TICK_INTERVAL, task => {
    tickCount += TICK_INTERVAL;

    if (tickCount < main.settings.lootDespawnDelay) {
      main.tick(tickCount);
      return;
    }

    main.removeLootTables();
    stopTicking();
  });
}

function stopTicking() {
  if (!isTicking()) {
    return;
  }

  task.cancel();
  task = null;
  tickCount = 0;
}

function isTicking() {
  return task != null && !task.isCancelled();
}
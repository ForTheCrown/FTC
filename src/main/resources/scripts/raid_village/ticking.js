// input: main, from main.js

const TICK_INTERVAL = 1;

let task;
let tickCount = 0;

function beginTicking() {
  if (isTicking()) {
    throw `Already ticking, tickCount = ${tickCount}`;
  }

  tickCount = 0;
  logger.info("beginTicking: despawnDelay={}", settings.lootDespawnDelay);

  this.task = scheduler.runTimer(TICK_INTERVAL, TICK_INTERVAL, task => {
    tickCount += TICK_INTERVAL;

    if (tickCount < settings.lootDespawnDelay) {
      main.tick(tickCount);
      return;
    }

    main.removeLootTables();
    task.cancel();
  });
}

function stopTicking() {
  if (task == null) {
    return;
  }

  task.cancel();
  task = null;
  tickCount = 0;
}

function isTicking() {
  return task != null && !task.isCancelled();
}
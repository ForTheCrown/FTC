import "@ftc.utils.stand.DynamicArmorStand";
import "@ftc.core.config.ResourceWorldConfig";
import "@ftc.core.registry.Keys";

import "@jlang.System";
import "@jutil.concurrent.TimeUnit";

/* -------------------------------------------------------------------------- */

const X_POS = 200.50
const Z_POS = 188.00;
const Y_TOP =  75.50;
const Y_BOTTOM = Y_TOP - 0.50;

const topStand = new DynamicArmorStand(toLocation(Y_TOP));
const bottomStand = new DynamicArmorStand(toLocation(Y_BOTTOM));

topStand.setKey(Keys.forthecrown("rw_counter/top_display"));
bottomStand.setKey(Keys.forthecrown("rw_counter/bottom_display"));

update();

function toLocation(y) {
  return new Location(Worlds.overworld(), X_POS, y, Z_POS);
}

function update() {
  if (!ResourceWorldConfig.enabled) {
    killStands();
    return;
  }

  topStand.update(Component.text("Next Resource World reset in"));

  const lastReset = ResourceWorldConfig.lastReset;
  const interval = ResourceWorldConfig.resetInterval;
  const nextReset = lastReset + interval;
  const currentTime = System.currentTimeMillis();

  const untilReset = nextReset - currentTime;

  if (untilReset < TimeUnit.DAYS.toMillis(1)) { 
    bottomStand.update(Component.text("Less than 1 day"));
  } else {
    bottomStand.update(
      Text.format("{0, time, -biggest}", untilReset)
    );
  }
}

function killStands() {
  topStand.kill();
  bottomStand.kill();
}

function __onDayChange() {
  update();
}

function __onClose() {
  killStands();
}
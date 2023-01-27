import "joptsimple.OptionParser";
import "@bukkit.potion.PotionEffectType";
import "@bukkit.potion.PotionEffect";
import "@bukkit.entity.LivingEntity";
import "@ftc.inventory.weapon.ability.UpgradeCooldown";

import "@jlang.Integer";
import "@jlang.System";

let potionType = null;
let duration = 0;
let giveTarget = false;
let amplifier = 0;
let triggeredByRight = true;

parseArgs();

// I'm not even gonna lie, this is over engineered to hell lmao
// I did not need to use an OptionParser here lmao
function parseArgs() {
  const parser = new OptionParser();

  const type = parser.accepts("potion-type", "The potion effect type")
      .withRequiredArg();

  const durationArg = parser.accepts("duration", "Duration of a potion effect, in ticks. Defaults to 'baseCooldown'")
      .withOptionalArg();

  const amplifierArg = parser.accepts("amplifier", "Amplifier given to potion, if -1, uses ability's level")
      .withOptionalArg();

  const giveTargetArg = parser.accepts("give-target", "If set, gives the target the potion effect, instead of the player");
  const leftClickArg = parser.accepts("left-click", "If set, listens to left clicks instead of right clicks");

  const out = System.out;
  let set = null;

  try {
    set = parser.parse(args);
  } catch (err) {
    parser.printHelpOn(out);
    throw err;
  }

  if (!set.hasArgument(type)) {
    parser.printHelpOn(out);
    throw "No potion type set with '--potion-type=<type>'"
  }

  if (!set.hasArgument(durationArg)) {
    parser.printHelpOn(out);
    throw "No potion duration set with '--duration=<value>'"
  }

  if (set.has(giveTargetArg)) {
    giveTarget = true;
  }

  if (set.has(leftClickArg)) {
    triggeredByRight = false;
  }

  if (set.hasArgument(amplifierArg)) {
    amplifier = set.valueOf(amplifierArg);
  }

  if (set.hasArgument(durationArg)) {
    let durValue = set.valueOf(durationArg);
    duration = UpgradeCooldown.parseTicks(durValue);
  }

  let potionTypeString = set.valueOf(type);
  let typeValue = PotionEffectType.getByName(potionTypeString);

  if (typeValue == null) {
    parser.printHelpOn(out);
    throw `Unknown potion type ${potionTypeString}`;
  }

  potionType = typeValue;
  logger.debug("Set potion type={}", potionType);
}

function onRightClick(player, clicked) {
  if (!triggeredByRight) {
    return false;
  }

  return trigger(player, clicked);
}

function onLeftClick(player, clicked) {
  if (triggeredByRight) {
    return false;
  }

  return trigger(player, clicked);
}

function trigger(player, clicked) {
  if (giveTarget && !(clicked instanceof LivingEntity)) {
    return false;
  }

  let target = giveTarget ? clicked : player;
  let amp = 0;

  if (amplifier == "level") {
    amp = level - 1;
  } else if (amplifier != -1) {
    amp = Number(amplifier);
  }

  let effect = new PotionEffect(potionType, duration, amp);
  target.addPotionEffect(effect);
}

function getCooldown(rank) {
  return duration + 60;
}
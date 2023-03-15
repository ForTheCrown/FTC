// Imports
const Worlds = Java.type("net.forthecrown.core.Worlds");
const StreakIncreaseEvent = Java.type("net.forthecrown.core.challenge.StreakIncreaseEvent");
const C_Manager = Java.type("net.forthecrown.core.challenge.ChallengeManager");
const ArmorStand = Java.type("org.bukkit.entity.ArmorStand");
const UnitFormat = Java.type("net.forthecrown.utils.text.format.UnitFormat");
const StreakCategory = Java.type("net.forthecrown.core.challenge.StreakCategory");
const DynamicArmorStand = Java.type("net.forthecrown.utils.stand.DynamicArmorStand");
import "@ftc.core.registry.Keys";

// Constants
const STAND_POSITION = Vector3d.from(207.5, 73.15, 188.5);
const NO_STREAK = 0;

const dynamicStand = new DynamicArmorStand(
  new Location(Worlds.overworld(), 207.5, 73.15, 188.5),
  Keys.forthecrown("top_streak")
);

// Fields
const greatest = {
  id: null,
  streak: NO_STREAK
};

events.register(StreakIncreaseEvent.class, onStreakIncrease);
scanInitial();
updateStand();

function onStreakIncrease(/* StreakIncreaseEvent */ event) {
  if (event.getCategory() != StreakCategory.ITEMS) {
    return;
  }

  let highestStreak = event.getEntry()
    .getStreak(StreakCategory.ITEMS)
    .getHighest();

  if (greatest.id != null && greatest.streak >= highestStreak) {
    return;
  }

  updateStreak(event.getUser().getUniqueId(), highestStreak);
}

function updateStreak(playerId, streak) {
  greatest.id = playerId;
  greatest.streak = streak;

  updateStand();
}

function updateStand() {
  if (greatest.id == null || greatest.streak < 1) {
    dynamicStand.kill();
    return;
  }

  dynamicStand.update(createStandName(greatest.id, greatest.streak));
}

function scanInitial() {
  let entries = C_Manager.getInstance().getEntries();

  entries.forEach(e => {
    let streak = e.getStreak(StreakCategory.ITEMS).getHighest();

    if (streak <= NO_STREAK) {
      return;
    }

    if (greatest.id == null || greatest.streak < streak) {
      greatest.id = e.getId();
      greatest.streak = streak;
    }
  });
}

// Called when the script is closed
function __onClose() {
  dynamicStand.kill();
}

// Formats the armor stand's name
function createStandName(playerId, streak) {
  return Text.format("{0, user}: &e{1}", playerId, UnitFormat.unit(streak, "Day"));
}
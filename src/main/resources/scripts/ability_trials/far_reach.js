import "@bukkit.event.entity.EntityDeathEvent";
import "@bukkit.entity.Stray";
import "@bukkit.attribute.Attribute";
import "@bukkit.loot.LootTables";

import "@ftc.user.UserTeleport.Type";
import "@ftc.useables.Usables";

import "@jutil.LinkedList";

// -----------------------------------------------------------------------------

const FINISHED = -1;

const G_EXP_REWARD = 50;

const SCOREBOARD_TAG = "far_reach_trial_mob";
const OBJECTIVE_NAME = "trials_far_reach";
const TARGET = 3;
const UPDATE_INTERVAL_TICKS = 5 * 20;
const RANDOM = Util.RANDOM;
const CAGE_RADIUS = 1;

const cagePositions = [
  { x: -461, y: 24, z: -308 },
  { x: -457, y: 18, z: -299 },
  { x: -462, y: 22, z: -297 },
];
let brokenIndex = -1;
let nextIndex = 0;

const respawnStack = new LinkedList();

// -----------------------------------------------------------------------------

events.register(EntityDeathEvent.class, onDeath);
scheduler.runTimer(UPDATE_INTERVAL_TICKS, UPDATE_INTERVAL_TICKS, onUpdate);

function onDeath(event) {
  let died = event.getEntity();
  let killer = died.getKiller();

  if (killer == null) {
    return;
  }

  if (!died.getScoreboardTags().contains(SCOREBOARD_TAG)) {
    return;
  }

  let score = getScore(killer);

  let location = died.getLocation();
  respawnStack.push(location);

  if (score == null) {
    logger.warn("Couldn't find {} objective for trial ");
    return;
  }

  let scoreValue = score.getScore();
  let guild = Users.get(killer).getGuild();

  if (guild == null || scoreValue == FINISHED) {
    return;
  }

  score.setScore(scoreValue + 1);

  if (scoreValue < TARGET) {
    killer.sendMessage(
        Text.format("Kill &e{0, number}&r more to finish!",
            NamedTextColor.GRAY,
            TARGET - scoreValue
        )
    );

    return;
  }

  score.setScore(FINISHED);
  let member = guild.getMember(killer.getUniqueId());
  member.addExpEarned(G_EXP_REWARD);

  killer.sendMessage(
      Text.format("Received &e{0, number} Guild Exp&r.",
          NamedTextColor.GOLD,
          G_EXP_REWARD
      )
  );
}

function onUpdate(task) {
  let location = respawnStack.poll();

  if (location != null) {
    spawnEntity(location);
  }

  if (brokenIndex != -1) {
    let pos = cagePositions[brokenIndex];
    brokenIndex = -1;

    forEachBlock(pos, block => {
      block.setType(Material.LIME_STAINED_GLASS_PANE, true);
    });
  }

  let bounds = getBounds();
  if (bounds != null) {
    let players = bounds.getPlayers();

    if (players.isEmpty()) {
      return;
    }
  }

  brokenIndex = nextIndex;
  nextIndex = (nextIndex + 1) % cagePositions.length;

  forEachBlock(cagePositions[brokenIndex], block => {
    block.setType(Material.AIR, true);
  });
}

function getBounds() {
  let triggers = Usables.getInstance().getTriggers();
  let trigger = triggers.getNamed("abilities_trial/far_reach/enter");

  if (trigger == null) {
    return null;
  }

  return trigger.getBounds();
}

function spawnEntity(location) {
  let world = location.getWorld();

  world.spawn(location, Stray.class, stray => {
    stray.setRemoveWhenFarAway(false);
    stray.setShouldBurnInDay(false);
    stray.addScoreboardTag(SCOREBOARD_TAG);
    stray.setLootTable(LootTables.EMPTY.getLootTable());

    let attr = stray.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
    attr.setBaseValue(0);

    let knockbackResistance = stray.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
    knockbackResistance.setBaseValue(1000);

    let eq = stray.getEquipment();
    eq.setHelmet(null);
    eq.setChestplate(null);
    eq.setLeggings(null);
    eq.setBoots(null);
    eq.setItemInMainHandDropChance(0);
    eq.setItemInOffHandDropChance(0);
  });
}

function forEachBlock(position, consumer) {
  let minX = position.x - CAGE_RADIUS;
  let minY = position.y - CAGE_RADIUS;
  let minZ = position.z - CAGE_RADIUS;

  let maxX = position.x + CAGE_RADIUS;
  let maxY = position.y + CAGE_RADIUS;
  let maxZ = position.z + CAGE_RADIUS;

  let world = Worlds.voidWorld();

  let bounds = new WorldBounds3i(world, minX, minY, minZ, maxX, maxY, maxZ);
  bounds.forEach(block => {
    let bX = block.getX();
    let bZ = block.getZ();

    if (bX == position.x && bZ == position.z) {
      return;
    }

    consumer(block);
  });
}

function getScore(player) {
  let scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
  let obj = scoreboard.getObjective(OBJECTIVE_NAME);

  if (obj == null) {
    return null;
  }

  return obj.getScore(player);
}
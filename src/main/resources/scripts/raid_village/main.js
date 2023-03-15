/* -------------------------------- IMPORTS --------------------------------- */
// See import_placeholders.toml for why there are '@' symbols in the imports

import "@bukkit.event.raid.RaidTriggerEvent";
import "@bukkit.inventory.InventoryHolder";
import "@bukkit.block.Chest";
import "com.destroystokyo.paper.loottable.LootableInventory";
import "@ftc.utils.math.WorldBounds3i";
import "@ftc.core.Worlds";
import "@ftc.core.registry.Keys";
import "@worldguard.WorldGuard";
import "@worldedit.bukkit.BukkitAdapter";
import "@worldedit.math.BlockVector3";

/* -------------------------------------------------------------------------- */

const TICKS_PER_SECOND = 20;
const DEBUGGING = false;

let active = false;

// Settings
const settings = {
  // World guard region that determines the area
  // where chests are looked for
  wgRegion: "raid_village",

  // Tick delay for loot despawning
  lootDespawnDelay: (2 * 60 * TICKS_PER_SECOND), // 2 Minutes

  // Tick interval at which boss bar viewers are updated,
  // this means adding new players inside the wgRegion and
  // removing those that left
  viewerUpdateIntervalTicks: 10 * TICKS_PER_SECOND, // Every 10 seconds

  // Loot table given to all chests found in the wgRegion
  lootTableKey: "forthecrown:chests/raid_village_loot"
};

// Compile scripts and set members
// compile(): inbuilt function to load scripts with a file path
//  relative to this one
const bossbar = compile("bossbar.js");
const ticking = compile("ticking.js");
ticking.main = this;
ticking.settings = settings;
bossbar.main = this;

// Evaluate scripts
bossbar();
ticking();

/* ------------------------------- LISTENER --------------------------------- */

// Registers the raid trigger listener to spawn loot and then later despawn the
// same loot
events.register(RaidTriggerEvent.class, onRaidStart);

function onRaidStart(event) {
  let region = getWorldGuardRegion();

  if (region == null || region == undefined) {
    logger.warn("Couldn't find WG region '{}'", settings.wgRegion);
    return;
  }

  let min = region.getMinimumPoint();
  let max = region.getMaximumPoint();

  let raidLocation = event.getRaid().getLocation();

  if (!raidLocation.getWorld().equals(Worlds.overworld())) {
    return;
  }

  let raidX = raidLocation.getBlockX();
  let raidY = raidLocation.getBlockY();
  let raidZ = raidLocation.getBlockZ();

  let pos = BlockVector3.at(raidX, raidY, raidZ);

  // If raid is not happening inside the raid village, then stop here
  if (!pos.containedWithin(min, max)) {
    return;
  }

  // Log data about raid
  let raid = event.getRaid();
  logger.info("raid data:");
  logger.info("-omen level={}", raid.getBadOmenLevel());
  logger.info("-totalLevels={}", raid.getTotalLevels());
  logger.info("-totalGroups={}", raid.getTotalGroups());

  start(region, raidLocation.getWorld());

  // Cancel the event durring debugging so it doesn't take
  // 30 minutes to kill the raid that spawns
  if (DEBUGGING) {
    event.setCancelled(true);
  }
}

/* -------------------------------------------------------------------------- */

function start(wgRegion, world) {
  if (wgRegion == null || world == null) {
    logger.warn("Either wgRegion or world was null!");
    return
  }

  bossbar.createBossBar(wgRegion, world);
  ticking.beginTicking();
  active = true;

  // Set the next refill of each inventory inside the region to 10
  // This means the value will always be less than the current time
  // meaning, the inventory will always be refilled when opened.
  try {
    const namespacedKey = Keys.parse(settings.lootTableKey);
    const lootTable = Bukkit.getLootTable(namespacedKey);

    if (lootTable == null) {
      logger.warn("Cannot place loot in chests! No lootable under key {}", namespacedKey);
      return;
    }

    forEachInventory((chest, x, y, z) => {
      chest.setLootTable(lootTable);
      chest.setNextRefill(10);
      chest.update();
    });
  } catch (err) {
      logger.error("Error running forEachInventory: {}", err);
  }
}

function tick(tick) {
  // Every viewer update interval, update bossbar viewers
  if (tick % settings.viewerUpdateIntervalTicks == 0) {
    bossbar.updateViewers();
  }

  // Update bossbar progress to current delay ratio
  let progress = tick / settings.lootDespawnDelay;
  bossbar.setProgress(progress);
}

// Stops the current raid functionality
// Clears all chests, destroy the bossbar, and stop the tick counter
function removeLootTables() {
  logger.info("Removing all loot tables");

  bossbar.destroy();
  ticking.stopTicking();
  active = false;

  forEachInventory((chest, x, y, z) => {
    Util.consoleCommand("data merge block %s %s %s {Items:[],LootTable:'minecraft:empty'}", x, y, z);
  });
}

function getWorldGuardRegion() {
  let container = WorldGuard.getInstance().getPlatform().getRegionContainer();
  let world = BukkitAdapter.adapt(Worlds.overworld());
  return container.get(world).getRegion(settings.wgRegion);
}

function forEachInventory(action) {
  let wg = getWorldGuardRegion();
  let min = Vectors.from(wg.getMinimumPoint());
  let max = Vectors.from(wg.getMaximumPoint());

  let bounds = WorldBounds3i.of(Worlds.overworld(), min, max);

  bounds.forEach(block => {
    let state = block.getState();
    let x = block.getX();
    let y = block.getY();
    let z = block.getZ();

    if (!(state instanceof Chest)) {
      return;
    }

    action(state, block.getX(), block.getY(), block.getZ());
  });
}

function __onClose() {
  if (active) {
    removeLootTables();
  }
}
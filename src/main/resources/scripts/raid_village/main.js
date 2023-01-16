/* -------------------------------- IMPORTS --------------------------------- */
// See import_placeholders.toml for why there are '@' symbols in the imports

import "@bukkit.events.raid.RaidTriggerEvent";
import "@bukkit.inventory.InventoryHolder";
import "com.destroystokyo.paper.loottable.LootableInventory";
import "@ftc.utils.math.WorldBounds3i";
import "@worldguard.WorldGuard";
import "@worldedit.bukkit.BukkitAdapter";
import "@worldedit.math.BlockVector3";

/* -------------------------------------------------------------------------- */

// Settings
const settings = {
  wgRegion: "raid_village",
  defaultLootTable: "empty",
  lootDespawnDelay: 12_000,
  viewerUpdateIntervalTicks: 10 * 20 // Every 10 seconds
};

// Compile scripts and set members
const bossbar = compile("bossbar.js");
const ticking = compile("ticking.js");
ticking.main = this;

// Evaluate scripts
bossbar();
ticking();

/* ------------------------------- LISTENER --------------------------------- */

// Registers the raid trigger listener to spawn loot and then later despawn the
// same loot
events.register("onRaidStart", RaidTriggerEvent);
function onRaidStart(event) {
  let region = getWorldGuardRegion();
  let raidLocation = event.getRaid().getLocation();

  if (!raidLocation.getWorld().equals(Worlds.overworld())) {
    return;
  }

  let raidX = raidLocation.getBlockX();
  let raidY = raidLocation.getBlockY();
  let raidZ = raidLocation.getBlockZ();

  let min = region.getMinimumPoint();
  let max = region.getMaximumPoint();

  let pos = BlockVector3.at(raidX, raidY, raidZ);

  // If raid is not happening inside the raid village, then stop here
  if (!pos.containedWithin(min, max)) {
    return;
  }

  start(wgRegion, raidLocation.getWorld());
}

/* -------------------------------------------------------------------------- */

function start(wgRegion, world) {
  bossbar.createBossBar(wgRegion, world);
  ticking.startTicking();

  // Set the next refill of each inventory inside the region to 10
  // This means the value will always be less than the current time
  // meaning, the inventory will always be refilled when opened.
  forEachInventory(inv => inv.setNextRefill(10));
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
  bossbar.destroy();
  ticking.stopTicking();

  forEachInventory(inv => {
    // No pending refill, means someone has opened this inventory
    if (!inv.hasBeenFilled()) {
      return;
    }

    inv.clear();
  });
}

function getWorldGuardRegion() {
  let container = WorldGuard.getInstance().getPlatform().getRegionContainer();
  let world = BukkitAdapter.adapt(Worlds.overworld());
  return container.get(world).getRegion(settings.wgRegion);
}

function forEachInventory(action) {
  let bounds = WorldBounds3i.of(wgRegion);

  bounds.forEach(block => {
    let state = block.getState();

    if (!(state instanceof InventoryHolder)) {
      return;
    }

    let inv = state.getInventory();

    if (!(inv instanceof LootableInventory)) {
      return;
    }

    action(inv);
    state.update();
  });
}

function __onClose() {
  removeLootTables();
}
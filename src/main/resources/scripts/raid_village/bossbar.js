import "org.spongepowered.math.GenericMath";
import "@bukkit.boss.BarStyle";
import "@bukkit.boss.BarColor";
import "@bukkit.boss.BarFlag";
import "@bukkit.util.BoundingBox";
import "@bukkit.entity.Player";

let bossBar = null;

function ensureExists() {
  if (bossBar == null) {
    throw "No bossbar";
  }
}

function setProgress(progress) {
  ensureExists();

  if (progress < 0) {
    progress = 0;
  } else if (progress > 1) {
    progress = 1;
  }

  bossBar.setProgress(progress);
}

function createBossBar(region, world) {
  if (bossBar != null) {
    return;
  }

  if (region == null) {
      logger.warn("wgRegion == null");
      return;
  }

  bossBar = Bukkit.createBossBar(
          "Time until loot despawns",
          BarColor.GREEN,
          BarStyle.SEGMENTED_10
  );

  updateViewers();
}

function destroy() {
  if (bossBar == null) {
      return;
  }

  bossBar.setVisible(false);
  bossBar.removeAll();
  bossBar = null;
}

function updateViewers() {
  ensureExists();
  bossBar.removeAll();

  let wgRegion = main.getWorldGuardRegion();
  let world = Worlds.overworld();

  if (wgRegion == null) {
    logger.warn("Cannot update viewers, null region");
    return;
  }

  let min = wgRegion.getMinimumPoint();
  let max = wgRegion.getMaximumPoint();

  let bounds = new BoundingBox(
          min.getX(), min.getY(), min.getZ(),
          max.getX(), max.getY(), max.getZ()
  );

  let entities = world.getNearbyEntities(bounds, entity => entity instanceof Player);

  entities.forEach(plr => {
    bossBar.addPlayer(plr);
  });
}
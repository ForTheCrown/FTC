import "org.spongepowered.math.GenericMath";
import "@bukkit.boss.BarStyle";
import "@bukkit.boss.BarColor";
import "@bukkit.boss.BarFlag";
import "@bukkit.util.BoundingBox";
import "@bukkit.entity.Player";

let bossBar;
let wgRegion;

function ensureExists() {
  if (bossBar == null) {
    throw "No bossbar";
  }
}

function setProgress(progress) {
  ensureExists();
  bossBar.setProgress(GenericMath.clamp(progress, 0, 1));
}

function createBossbar(wgRegion, world) {
  if (bossBar != null) {
    return;
  }

  bossBar = Bukkit.createBossbar(
          "Time until loot despawns",
          BarColor.GREEN,
          BarStyle.SEGEMENTED_10
  );

  updateViewers();
}

function destroy() {
  ensureExists();

  bossBar.setVisible(false);
  bossBar.removeAll();
  bossBar = null;
}

function updateViewers() {
  ensureExists();
  bossBar.removeAll();

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
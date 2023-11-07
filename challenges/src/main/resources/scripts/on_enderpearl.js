import Player from "org.bukkit.entity.Player";
import EnderPearl from "org.bukkit.entity.EnderPearl";

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // ProjectileHitEvent
  var projectile = event.getEntity();
  if (projectile instanceof EnderPearl && projectile.getShooter() instanceof Player) {
    let player = projectile.getShooter();

    var hit = event.getHitEntity();
    if (hit != null && hit.getType() == EntityType.ENDERMAN) {
      handle.givePoint(player);
    }
  }
}
import Player from "org.bukkit.entity.Player";
import Snowball from "org.bukkit.entity.Snowball";

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // ProjectileHitEvent
  var projectile = event.getEntity();
  if (projectile instanceof Snowball && projectile.getShooter() instanceof Player) {
    let player = projectile.getShooter();

    var hit = event.getHitEntity();
    if (hit != null && hit.getType() == EntityType.PLAYER && hit != player) {
      handle.givePoint(player);
    }
  }
}
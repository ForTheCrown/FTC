const Player = Java.type("org.bukkit.entity.Player");
const Snowball = Java.type("org.bukkit.entity.Snowball");

function canComplete(user) {
    return user.getGuild() != null;
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
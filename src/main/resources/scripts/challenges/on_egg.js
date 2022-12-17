const Egg = Java.type("org.bukkit.entity.Egg");
const Player = Java.type("org.bukkit.entity.Player");

function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // ProjectileHitEvent
    var projectile = event.getEntity();
    if (projectile instanceof Egg && projectile.getShooter() instanceof Player) {
        let player = projectile.getShooter();

        var hit = event.getHitEntity;
        if (hit != null && hit.getType() == EntityType.Player && hit != player) {
            handle.givePoint(player);
        }
    }
}
const Player = Java.type("org.bukkit.entity.Player");
const EnderPearl = Java.type("org.bukkit.entity.EnderPearl");

function canComplete(user) {
    return user.getGuild() != null;
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
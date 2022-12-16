function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // ProjectileHitEvent
    var projectile = event.getEntity();
    if (projectile instanceof Snowball && projectile.getShooter() instanceof Player player) {

        var hit = event.getHitEntity;
        if (hit != null && hit.getType() == EntityType.Player && hit != player) {
            handle.givePoint(player);
        }
    }
}
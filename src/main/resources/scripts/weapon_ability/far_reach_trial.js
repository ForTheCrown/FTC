import "@bukkit.enitity.Player";
import "@bukkit.event.EntityDamageEvent";
import "@bukkit.event.EntityDamageByEnittyEvent";

const ENTITY_TAG = "far_reach_punching_bag";

events.register("onDamage", EntityDamageEvent);

function onDamage(event) {
  let entity = event.getEntity();

  if (!entity.getScoreboardTags().contains(ENTITY_TAG)) {
    return;
  }

  if (!event instanceof EntityDamageByEntityEvent || !(event.getDamager() instanceof Player)) {
    event.setCancelled(true);
  }


}
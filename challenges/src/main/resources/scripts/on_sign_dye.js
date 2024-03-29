import EquipmentSlot from "org.bukkit.inventory.EquipmentSlot";

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onEvent(event, handle) {
  // PlayerInteractEvent
  if (event.isCancelled() || event.getHand() != EquipmentSlot.HAND) {
    return;
  }

  var player = event.getPlayer();
  var item = player.getInventory().getItemInMainHand();
  var block = event.getClickedBlock();

  if (item == null || block == null) {
    return;
  }

  if (item.getType().name().contains("DYE") && block.getType().name().contains("SIGN")) {
    handle.givePoint(player);
  }
}
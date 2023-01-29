import "@ftc.inventory.ExtendedItems";
import "@ftc.inventory.weapon.ability.SwordAbilityManager";

const UNLIMITED_USES = -1;

function onUse(user) {
  let item = ExtendedItems.ROYAL_SWORD.createItem(user.getUniqueId());
  let sword = ExtendedItems.ROYAL_SWORD.get(item);

  if (args.length != 1) {
    logger.warn("No argument for ability type set! Cannot give");
    return;
  }

  let abilityName = args[0];

  let abilityType = SwordAbilityManager.getInstance()
      .getRegistry()
      .get(abilityName)
      .orElseThrow();

  let ability = abilityType.create();
  ability.setUses(UNLIMITED_USES);

  sword.setAbility(ability);
  sword.update(item);

  user.getInventory().addItem(item);

  let player = user.getPlayer();
  player.setCooldown(item.getType(), 0);
}
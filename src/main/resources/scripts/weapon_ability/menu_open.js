import "@ftc.inventory.weapon.ability.AbilityMenus";

function onUse(user) {
    let instance = AbilityMenus.getInstance();

    // _location is a variable placed into the script by the
    // usable action that handles this script, it's the Location
    // of the entity/block that this script is attached to
    instance.open(user, _location);
}

function test(user) {
  const manager = SwordAbilityManager.getInstance();

  if (manager.isEnabled()) {
    return true;
  }

  return user.hasPermission(Permissions.ADMIN);
}
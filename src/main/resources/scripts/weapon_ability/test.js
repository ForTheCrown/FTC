import "@ftc.inventory.weapon.ability.AbilityAnimation";
import "@ftc.inventory.weapon.ability.SwordAbilityManager";
import "@ftc.core.Permissions";

function test(user) {
  var instance = AbilityAnimation.getInstance();
  if (!instance.hasOngoing()) {
    return false;
  }

  return isEnabled(user);
}

function isEnabled(user) {
  const manager = SwordAbilityManager.getInstance();

  if (manager.isEnabled()) {
    return true;
  }

  return user.hasPermission(Permissions.ADMIN);
}

function getFailMessage(user) {
  if (!isEnabled(user)) {
    return null;
  }

  return Component.text(
      "Cannot open menu during ongoing animation",
      NamedTextColor.GRAY
  );
}
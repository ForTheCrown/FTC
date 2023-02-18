import "@ftc.inventory.weapon.ability.AbilityAnimation";
import "@ftc.inventory.weapon.ability.SwordAbilityManager";
import "@ftc.inventory.weapon.ability.AbilityMenus";
import "@ftc.core.Permissions";

function test(user) {
  var instance = AbilityAnimation.getInstance();
  if (instance.hasOngoing()) {
    return false;
  }

  if (isBeingUsed()) {
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

function isBeingUsed() {
  let menus = AbilityMenus.getInstance();
  return menus.isOpened();
}

function getFailMessage(user) {
  if (!isEnabled(user)) {
    return null;
  }

  if (isBeingUsed()) {
    return Component.text(
      "Only 1 person can use the menu at a time",
      NamedTextColor.GRAY
    );
  }

  return Component.text(
      "Cannot open menu during ongoing animation",
      NamedTextColor.GRAY
  );
}
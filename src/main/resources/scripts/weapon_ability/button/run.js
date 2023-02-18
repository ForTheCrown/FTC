import "@ftc.inventory.weapon.ability.SwordAbilityManager";

if (args.length != 1) {
  throw "Expected args[0] to be ability name";
}

const manager = SwordAbilityManager.getInstance();
const registry = manager.getRegistry();
const value = registry.orNull(args[0]);

if (value == null) {
  throw "Unknown ability: '" + args[0] + "'";
}

value.enterTrialArea(reader);
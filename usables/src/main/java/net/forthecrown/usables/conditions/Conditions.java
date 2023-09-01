package net.forthecrown.usables.conditions;

import net.forthecrown.registry.Registry;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.UsageType;
import net.forthecrown.usables.scripts.ScriptInstance;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;

public class Conditions {

  public static void registerAll(Registry<UsageType<? extends Condition>> r) {
    r.register("cooldown", TestCooldown.TYPE);
    r.register("in_world", TestWorld.TYPE);
    r.register("one_use",  TestOneUse.TYPE);
    r.register("never_used", TestNeverUsed.TYPE);
    r.register("test_script", ScriptInstance.TYPE);
    r.register("not_alt", TestNotAlt.TYPE);
    r.register("inventory_empty", TestEmptyInventory.TYPE);
    r.register("no_riders", NoRiderCondition.TYPE);
    r.register("has_score", TestScore.TYPE);
    r.register("has_items", ItemCondition.HAS_ITEMS);
    r.register("does_not_have_items", ItemCondition.MISSING_ITEMS);
    r.register("has_permission", TestPermission.TYPE);
    r.register("hand", HandCondition.TYPE);

    UserService service = Users.getService();
    Registry<Currency> currencies = service.getCurrencies();

    currencies.get("rhines").ifPresent(currency -> {
      r.register("rhines", new CurrencyConditionType(currency, "Rhines"));
    });

    currencies.get("gems").ifPresent(currency -> {
      r.register("gems", new CurrencyConditionType(currency, "Gems"));
    });
  }
}

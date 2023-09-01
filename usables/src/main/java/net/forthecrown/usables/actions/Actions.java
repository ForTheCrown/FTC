package net.forthecrown.usables.actions;

import net.forthecrown.registry.Registry;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.UsageType;
import net.forthecrown.usables.scripts.ScriptInstance;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;

public class Actions {

  public static void registerAll(Registry<UsageType<? extends Action>> r) {
    r.register("run_script", ScriptInstance.TYPE);
    r.register("console_command", CommandAction.AS_SELF);
    r.register("player_command", CommandAction.AS_PLAYER);
    r.register("show_text", TextAction.TYPE);
    r.register("give_item", ItemAction.GIVE_TYPE);
    r.register("remove_item", ItemAction.REMOVE_TYPE);
    r.register("teleport", TeleportAction.TYPE);

    UserService service = Users.getService();
    Registry<Currency> currencies = service.getCurrencies();

    currencies.get("rhines").ifPresent(currency -> {
      CurrencyActions.registerAll(r, "bal", currency);
    });

    currencies.get("gems").ifPresent(currency -> {
      CurrencyActions.registerAll(r, "gems", currency);
    });
  }
}

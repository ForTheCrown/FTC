package net.forthecrown.usables.actions;

import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registry;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.usables.scripts.ScriptInstance;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;

public class Actions {

  public static void registerAll(Registry<ObjectType<? extends Action>> r) {
    r.register("run_script", ScriptInstance.TYPE);
    r.register("console_command", CommandAction.AS_SELF);
    r.register("player_command", CommandAction.AS_PLAYER);
    r.register("show_text", TextAction.TYPE);
    r.register("give_item", ItemAction.GIVE_TYPE);
    r.register("remove_item", ItemAction.REMOVE_TYPE);
    r.register("teleport", TeleportAction.TYPE);
    r.register("play_sound", SoundAction.TYPE);

    UserService service = Users.getService();
    Registry<Currency> currencies = service.getCurrencies();

    for (Holder<Currency> entry : currencies.entries()) {
      CurrencyActions.registerAll(r, entry.getKey(), entry.getValue());
    }

    ScoreActions.registerAll(r);
  }
}

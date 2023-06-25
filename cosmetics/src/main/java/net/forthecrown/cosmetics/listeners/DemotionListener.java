package net.forthecrown.cosmetics.listeners;

import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.CosmeticData;
import net.forthecrown.cosmetics.LoginEffect;
import net.forthecrown.cosmetics.LoginEffects;
import net.forthecrown.titles.events.TierPostChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DemotionListener implements Listener {


  @EventHandler(ignoreCancelled = true)
  public void onTierChange(TierPostChangeEvent event) {
    if (!event.isDemotion()) {
      return;
    }

    CosmeticData data = event.getUser().getComponent(CosmeticData.class);
    Cosmetic<LoginEffect> login = data.get(LoginEffects.TYPE);

    if (login == null) {
      return;
    }

    if (!login.test(event.getUser())) {
      data.set(LoginEffects.TYPE, null);
    }
  }
}
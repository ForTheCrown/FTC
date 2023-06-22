package net.forthecrown.inventory;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.forthecrown.events.Events;
import net.forthecrown.inventory.commands.CommandRoyalSword;
import net.forthecrown.inventory.listeners.WeaponListener;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemsPlugin extends JavaPlugin {

  public static StateFlag SWORD_USE_ALLOWED = new StateFlag("sword-upgrade-usage", true);

  @Override
  public void onEnable() {
    Events.register(new WeaponListener());
    new CommandRoyalSword();
  }

  @Override
  public void onLoad() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    registry.register(SWORD_USE_ALLOWED);
  }

  @Override
  public void onDisable() {

  }
}
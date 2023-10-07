package net.forthecrown.inventory;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import lombok.Getter;
import net.forthecrown.ItemGraveService;
import net.forthecrown.events.Events;
import net.forthecrown.inventory.commands.CommandRoyalSword;
import net.forthecrown.inventory.listeners.ItemDropListener;
import net.forthecrown.inventory.listeners.PlayerJoinListener;
import net.forthecrown.inventory.listeners.SwordFireballListener;
import net.forthecrown.inventory.listeners.WeaponListener;
import net.forthecrown.inventory.weapon.ability.AbilityAnimation;
import net.forthecrown.inventory.weapon.ability.SwordAbilityManager;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemsPlugin extends JavaPlugin {

  public static StateFlag SWORD_USE_ALLOWED = new StateFlag("sword-upgrade-usage", true);

  @Getter
  private ItemsConfig itemsConfig;

  public static ItemsConfig config() {
    return JavaPlugin.getPlugin(ItemsPlugin.class).itemsConfig;
  }

  @Override
  public void onEnable() {
    ItemUserProperties.init();

    Events.register(new WeaponListener());
    Events.register(new PlayerJoinListener());
    Events.register(new ItemDropListener());
    Events.register(new SwordFireballListener());

    new CommandRoyalSword();

    reload();

    ItemPlaceholders.registerAll();

    ItemGraveService grave = ItemGraveService.grave();
    grave.addFilter("extended_items", (item, player) -> {
      return ExtendedItems.shouldRemainInInventory(item);
    });
  }

  @Override
  public void onLoad() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    registry.register(SWORD_USE_ALLOWED);
  }

  public void reload() {
    reloadConfig();
    SwordAbilityManager.getInstance().loadAbilities();
  }

  @Override
  public void reloadConfig() {
    this.itemsConfig = TomlConfigs.loadPluginConfig(this, ItemsConfig.class);
  }

  @Override
  public void onDisable() {
    AbilityAnimation.getInstance().onDisable();
    ItemPlaceholders.unregister();
    ItemGraveService.grave().removeFilter("extended_items");
  }
}
package net.forthecrown.dungeons;

import net.forthecrown.dungeons.enchantments.DungeonEnchantments;
import net.forthecrown.events.Events;
import net.forthecrown.ItemGraveService;
import net.forthecrown.command.Commands;
import net.forthecrown.dungeons.commands.CommandDungeons;
import net.forthecrown.dungeons.commands.CommandPunchingBag;
import net.forthecrown.dungeons.listeners.DungeonListeners;
import net.forthecrown.dungeons.listeners.EnchantListeners;
import net.forthecrown.dungeons.listeners.PunchingBags;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import org.bukkit.plugin.java.JavaPlugin;

public class DungeonsPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Bosses.init();
    DungeonEnchantments.init();

    Events.register(new DungeonListeners());
    Events.register(new EnchantListeners());
    Events.register(new PunchingBags());

    ItemGraveService grave = ItemGraveService.grave();
    grave.addFilter("ftc_enchants", new FtcEnchantGraveFilter());

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new CommandPunchingBag());
    ctx.registerCommand(new CommandDungeons());
  }

  @Override
  public void onDisable() {
    ItemGraveService grave = ItemGraveService.grave();
    grave.removeFilter("ftc_enchants");
  }
}
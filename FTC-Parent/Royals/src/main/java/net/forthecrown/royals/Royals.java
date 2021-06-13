package net.forthecrown.royals;

import net.forthecrown.grenadier.RoyalArguments;
import net.forthecrown.grenadier.VanillaArgumentType;
import net.forthecrown.royals.commands.BossArgument;
import net.forthecrown.royals.commands.CommandRoyal;
import net.forthecrown.royals.commands.RoyalEnchantType;
import net.forthecrown.royals.dungeons.DungeonEvents;
import net.forthecrown.royals.dungeons.bosses.Bosses;
import net.forthecrown.royals.enchantments.EnchantEvents;
import net.forthecrown.squire.enchantment.RoyalEnchants;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Royals extends JavaPlugin implements Listener {

    public static Royals inst;

    public RoyalEnchants enchantments;
    public Bosses bosses;

    public void onEnable() {
        inst = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        bosses = Bosses.init();

        getServer().getPluginManager().registerEvents(new EnchantEvents(), this);
        getServer().getPluginManager().registerEvents(new DungeonEvents(this), this);

        RoyalArguments.register(BossArgument.class, VanillaArgumentType.WORD);
        RoyalArguments.register(RoyalEnchantType.class, VanillaArgumentType.WORD);
        new CommandRoyal();
    }

    @Override
    public void onDisable() {
        bosses.killAllBosses();
    }
}
package net.forthecrown.vikings;

import net.forthecrown.vikings.blessings.FastRunner;
import net.forthecrown.vikings.blessings.HeadChoppingBlessing;
import net.forthecrown.vikings.blessings.VikingBlessing;
import net.forthecrown.vikings.commands.CommandViking;
import net.forthecrown.vikings.commands.CommandVikingFunction;
import net.forthecrown.vikings.raids.MonasteryRaid;
import net.forthecrown.vikings.raids.RaidManager;
import net.forthecrown.vikings.raids.VikingRaid;
import org.bukkit.plugin.java.JavaPlugin;

public final class Vikings extends JavaPlugin {

    private static Vikings instance;
    private static RaidManager handler;

    @Override
    public void onEnable() {
        instance = this;
        handler = new RaidManager(getServer());

        getServer().getPluginManager().registerEvents(new VikingListener(), this);

        handler.registerRaid(new MonasteryRaid());

        new FastRunner();
        new HeadChoppingBlessing();

        new CommandViking();
        new CommandVikingFunction();
    }

    @Override
    public void onDisable() {
        for(VikingRaid r : handler.getRaids()){
            if(r.getCurrentParty() == null) continue;
            r.end();
        }

        for (VikingBlessing b: VikingBlessing.getBlessings()){
            b.save();
            b.clearTempUsers();
        }
    }

    public static void reloadVikings() {
        getInstance().reloadConfig();

        for (VikingBlessing b: VikingBlessing.getBlessings()){
            b.reload();
        }
    }

    public static void saveVikings(){
        getInstance().saveConfig();

        for (VikingBlessing b: VikingBlessing.getBlessings()){
            b.save();
        }
    }

    public static Vikings getInstance(){
        return instance;
    }

    public static RaidManager getRaidHandler(){
        return handler;
    }
}

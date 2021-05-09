package net.forthecrown.vikings;

import net.forthecrown.vikings.blessings.FastRunner;
import net.forthecrown.vikings.blessings.HeadChoppingBlessing;
import net.forthecrown.vikings.blessings.VikingBlessing;
import net.forthecrown.vikings.commands.CommandViking;
import net.forthecrown.vikings.valhalla.RaidManager;
import net.forthecrown.vikings.valhalla.VikingRaid;
import org.bukkit.plugin.java.JavaPlugin;

public final class Vikings extends JavaPlugin {

    private static Vikings instance;
    private static RaidManager handler;

    @Override
    public void onEnable() {
        instance = this;
        handler = RaidManager.init();

        getServer().getPluginManager().registerEvents(new VikingListener(), this);

        new FastRunner();
        new HeadChoppingBlessing();

        new CommandViking();
    }

    @Override
    public void onDisable() {
        for(VikingRaid r : handler.getRaids()){
            if(!r.isActive()) continue;
            r.end(VikingRaid.EndCause.PLUGIN);
        }

        saveVikings();
    }

    public static void reloadVikings() {
        inst().reloadConfig();

        for (VikingBlessing b: VikingBlessing.getBlessings()){
            b.reload();
        }
    }

    public static void saveVikings(){
        inst().saveConfig();

        handler.serializeAll();

        for (VikingBlessing b: VikingBlessing.getBlessings()){
            b.save();
        }
    }

    public static Vikings inst(){
        return instance;
    }

    public static RaidManager getRaidManager(){
        return handler;
    }
}

package net.forthecrown.vikings;

import net.forthecrown.vikings.commands.VikingsCommand;
import net.forthecrown.vikings.raids.MonasteryRaid;
import net.forthecrown.vikings.raids.managers.RaidHandler;
import net.forthecrown.vikings.raids.managers.VikingRaid;
import org.bukkit.plugin.java.JavaPlugin;

public final class Vikings extends JavaPlugin {

    private static Vikings instance;
    private static RaidHandler handler;

    @Override
    public void onEnable() {
        instance = this;
        handler = new RaidHandler(getServer());

        handler.registerRaid(new MonasteryRaid());

        new VikingsCommand();
    }

    @Override
    public void onDisable() {
        for(VikingRaid r : handler.getRaids()){
            if(r.getUsingPlayer() == null) continue;
            r.onRaidEnd();
        }
    }

    public static Vikings getInstance(){
        return instance;
    }

    public static RaidHandler getRaidHandler(){
        return handler;
    }
}

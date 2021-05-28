package net.forthecrown.emperor;

import net.forthecrown.emperor.comvars.ComVar;
import net.forthecrown.emperor.comvars.ComVars;
import net.forthecrown.emperor.comvars.types.ComVarType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Class which saves the FTC-Core in the interval given in the core_autoSaveIntervalMins comvar
 */
public class PeriodicalSaver extends BukkitRunnable {
    private final CrownCore core;
    private static final ComVar<Long> interval;

    static {
        interval = ComVars.set("core_autoSaveIntervalMins", ComVarType.LONG, CrownCore.inst().getConfig().getLong("System.save-interval-mins"));
    }

    PeriodicalSaver(CrownCore core){
        this.core = core;
    }

    public void start(){
        final long inter = interval.getValue() * 60 * 20;

        runTaskTimerAsynchronously(core, inter, inter);
    }

    @Override
    public void run() {
        CrownCore.saveFTC();
    }
}

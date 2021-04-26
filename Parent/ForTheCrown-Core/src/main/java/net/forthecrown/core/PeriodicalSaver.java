package net.forthecrown.core;

import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.core.comvars.types.ComVarType;
import org.bukkit.scheduler.BukkitRunnable;

public class PeriodicalSaver extends BukkitRunnable {

    private final FtcCore core;
    private static final ComVar<Long> interval;

    static {
        interval = ComVars.set("sv_autoSaveIntervalMins", ComVarType.LONG, FtcCore.getInstance().getConfig().getLong("System.save-interval-mins"));
    }

    PeriodicalSaver(FtcCore core){
        this.core = core;
    }

    public void start(){
        final long inter = interval.getValue() * 60 * 20;

        runTaskTimerAsynchronously(core, inter, inter);
    }

    @Override
    public void run() {
        FtcCore.saveFTC();
    }
}

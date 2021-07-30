package net.forthecrown.core;

import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Class which saves the FTC-Core in the interval given in the core_autoSaveIntervalMins comvar
 */
public final class PeriodicalSaver extends BukkitRunnable {
    private final ForTheCrown core;
    private static final ComVar<Long> interval;

    static {
        interval = ComVarRegistry.set("core_autoSaveIntervalMins", ComVarType.LONG, ForTheCrown.config().getLong("System.save-interval-mins"));
    }

    PeriodicalSaver(ForTheCrown core){
        this.core = core;
    }

    public void start(){
        final long inter = interval.getValue(60L) * 60 * 20;

        runTaskTimerAsynchronously(core, inter, inter);
    }

    @Override
    public void run() {
        ForTheCrown.saveFTC();
    }
}

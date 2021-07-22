package net.forthecrown.august;

import net.forthecrown.august.event.PinataEvent;
import net.forthecrown.august.usables.ActionEnterEvent;
import net.forthecrown.august.usables.CheckCanEnter;
import net.forthecrown.commands.CommandLeave;
import net.forthecrown.crownevents.reporters.EventReporter;
import net.forthecrown.crownevents.reporters.ReporterFactory;
import net.forthecrown.registry.Registries;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public class AugustPlugin extends JavaPlugin {

    public static AugustPlugin inst;

    public static PinataEvent event;

    public static Logger logger;
    public static EventReporter reporter;

    @Override
    public void onEnable() {
        inst = this;
        logger = getLogger();

        event = new PinataEvent();
        reporter = ReporterFactory.of(this, event);

        CommandLeave.add(EventConstants.ARENA_REGION, EventConstants.EXIT,
                player -> {
                    if(PinataEvent.currentEntry != null && PinataEvent.currentEntry.player().equals(player)) {
                        return true;
                    }

                    return PinataEvent.currentStarter != null && PinataEvent.currentStarter.getPlayer().equals(player);
                }
        );
    }

    @Override
    public void onLoad() {
        Registries.USAGE_ACTIONS.register(ActionEnterEvent.KEY, new ActionEnterEvent());
        Registries.USAGE_CHECKS.register(CheckCanEnter.KEY, new CheckCanEnter());
    }

    @Override
    public void onDisable() {
        if(PinataEvent.currentEntry != null) event.end(PinataEvent.currentEntry);

        try {
            reporter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

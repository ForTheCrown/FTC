package net.forthecrown.marchevent;

import net.forthecrown.core.crownevents.CrownEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MarchCrownEvent extends CrownEvent {

    protected MarchCrownEvent(Plugin plugin, Location startLocation, Location exitLocation) {
        super(plugin, startLocation, exitLocation);
    }

    @Override
    public void onEventComplete(Player player) {

    }
}

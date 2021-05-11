package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public interface InteractableSign extends CrownSerializer<FtcCore>, Deleteable, Interactable {
    Location getLocation();
    Sign getSign();
}

package net.forthecrown.vikings;

import net.forthecrown.core.ComponentUtils;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class VikingListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!(event.getRightClicked() instanceof Villager)) return;
        if(event.getRightClicked().customName() == null) return;

        String name = ComponentUtils.getString(event.getRightClicked().customName());

        switch (name){
            case "Brynjulf": //info dud
                //TODO make him give info to player
                //TODO With clickable info
                break;
            case "cobett": //priest dud -.-
                //TODO make him bring up the BlessingSelector
                break;
            case "Eirikr": //Raid dude
                //TODO implement the raid system talked about in Senate, described below
                break;

            default:
                return;
        }
    }
}
/*
 * Once every X hours a raid call gets put out, people can join in groups, raids will be harder the more people there are
 */

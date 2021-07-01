package net.forthecrown.july.listener;

import net.forthecrown.core.CrownException;
import net.forthecrown.core.commands.clickevent.ClickEventManager;
import net.forthecrown.core.commands.clickevent.ClickEventTask;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.july.EventConstants;
import net.forthecrown.july.JulyMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class GeneralListener implements Listener, ClickEventTask {

    private final String npcID;

    public GeneralListener(){
        this.npcID = ClickEventManager.registerClickEvent(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(!event.getLocation().getWorld().equals(EventConstants.EVENT_WORLD)) return;
        Entity ent = event.getEntity();

        if(ent instanceof Item || ent instanceof ArmorStand || ent instanceof Villager) {
            event.setCancelled(false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!event.getRightClicked().getPersistentDataContainer().has(EventConstants.NPC_KEY, PersistentDataType.BYTE)) return;
        Player player = event.getPlayer();
        event.setCancelled(true);

        if(Cooldown.contains(player, getClass().getSimpleName())) return;
        Cooldown.add(player, getClass().getSimpleName(), 5);

        ClickEventManager.allowCommandUsage(player, true);

        player.sendMessage(
                Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(Component.text("Wanna take part in the event? You've read the info?"))
                        .append(Component.newline())
                        .append(Component.text("[Practice] ")
                                .color(NamedTextColor.AQUA)
                                .hoverEvent(Component.text("Enter practice mode"))
                                .clickEvent(ClickEventManager.getClickEvent(npcID, "practise"))
                        )
                        .append(Component.text("[Enter event]")
                                .color(NamedTextColor.AQUA)
                                .hoverEvent(Component.text("Enter the event"))
                                .clickEvent(ClickEventManager.getClickEvent(npcID, "real"))
                        )
                        .build()
        );
    }

    @Override
    public void run(Player player, String[] args) throws CrownException, RoyalCommandException {
        if(!player.getWorld().equals(EventConstants.EVENT_WORLD)) throw new CrownException(player, "&7Cannot enter event from here");

        if(args[1].contains("real")) JulyMain.event.start(player);
        else JulyMain.event.startPractise(player);
    }
}

package net.forthecrown.dummyevent.events;

import net.forthecrown.core.clickevent.ClickEventHandler;
import net.forthecrown.core.clickevent.ClickEventTask;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.dummyevent.SprintEvent;
import net.forthecrown.dummyevent.SprintMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class EventListener implements Listener, ClickEventTask {

    private final String npcID;
    public EventListener(){
        this.npcID = ClickEventHandler.registerClickEvent(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!(event.getRightClicked() instanceof Villager)) return;
        Villager villager = (Villager) event.getRightClicked();
        if(villager.customName() == null || !villager.isInvulnerable()) return;
        if(!SprintEvent.RACE_AREA.contains(event.getRightClicked().getLocation())) return;

        ClickEventHandler.allowCommandUsage(event.getPlayer(), true, false);
        Component enterQuestionMark = Component.text("Hello there, young fellow!")
                .color(NamedTextColor.GRAY)
                .append(Component.newline())
                .append(Component.text("It's time for your yearly physical checkup "))
                .append(Component.text("[Enter event]")
                        .color(NamedTextColor.AQUA)
                        .hoverEvent(HoverEvent.showText(Component.text("Enter the race and run 1 lap")))
                        .clickEvent(ClickEvent.runCommand(ClickEventHandler.getCommand(npcID)))
                );

        event.getPlayer().sendMessage(enterQuestionMark);
    }

    @Override
    public void run(Player player, String[] strings) {
        if(!SprintEvent.RACE_AREA.contains(player.getLocation())) throw new CrownException(player, "&7You have to be at the event area to use this command!");
        if(!player.getInventory().isEmpty()) throw new CrownException(player, "&7You must have an empty inventory");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        SprintMain.event.start(player);
    }
}

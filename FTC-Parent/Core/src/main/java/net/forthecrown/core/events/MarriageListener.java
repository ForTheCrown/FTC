package net.forthecrown.core.events;

import net.forthecrown.core.CrownException;
import net.forthecrown.core.clickevent.ClickEventManager;
import net.forthecrown.core.clickevent.ClickEventTask;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.commands.marriage.CommandMarry;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserInteractions;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class MarriageListener implements Listener, ClickEventTask {

    private final String npcID;
    private final Set<UUID> awaitingFinishSet = new HashSet<>();

    public MarriageListener() {
        this.npcID = ClickEventManager.registerClickEvent(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Villager){
            if(!event.getRightClicked().getPersistentDataContainer().has(CommandMarry.KEY, PersistentDataType.BYTE)) return;
            if(Cooldown.contains(event.getPlayer(), "Core_Marriage_Priest")) return;

            Cooldown.add(event.getPlayer(), "Core_marriage_Priest", 20);
            ClickEventManager.allowCommandUsage(event.getPlayer(), true);

            Component message = Component.text()
                    .append(Component.translatable("marriage.priestText"))
                    .color(NamedTextColor.YELLOW)
                    .append(Component.space())
                    .append(
                            Component.translatable("marriage.priestText.option")
                                    .color(NamedTextColor.AQUA)
                                    .clickEvent(ClickEventManager.getClickEvent(npcID, "marry"))
                                    .hoverEvent(Component.text("Click me!"))
                    )
                    .build();

            event.getPlayer().sendMessage(message);
            event.setCancelled(true);

        } else if(event.getRightClicked() instanceof Player){ //This shit is dumb, I love it
            if(Cooldown.contains(event.getPlayer(), "Core_Marriage_Smooch")) return;
            Cooldown.add(event.getPlayer(), "Core_Marriage_Smooch", 2);

            CrownUser user = UserManager.getUser(event.getPlayer());
            UserInteractions inter = user.getInteractions();
            if(inter.getMarriedTo() == null) return;

            CrownUser target = UserManager.getUser(event.getRightClicked().getUniqueId());
            if(!inter.getMarriedTo().equals(target.getUniqueId())) return;
            if(!user.getPlayer().isSneaking() || !target.getPlayer().isSneaking()) return;

            user.sendMessage(
                    Component.text()
                            .append(Component.text("❤").color(NamedTextColor.RED))
                            .append(Component.space())
                            .append(Component.text("Smooched "))
                            .append(target.nickDisplayName().color(NamedTextColor.YELLOW))
                            .append(Component.space())
                            .append(Component.text("❤").color(NamedTextColor.RED))
                            .build()
            );

            target.sendMessage(
                    Component.text()
                            .append(Component.text("❤").color(NamedTextColor.RED))
                            .append(Component.space())
                            .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                            .append(Component.text(" smooched you!"))
                            .append(Component.space())
                            .append(Component.text("❤").color(NamedTextColor.RED))
                            .build()
            );

            Location loc = user.getLocation();
            Location targetLoc = target.getLocation();

            targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
            targetLoc.getWorld().spawnParticle(Particle.HEART, targetLoc.getX(), targetLoc.getY()+1, targetLoc.getZ(), 5, 0.5, 0.5, 0.5);

            loc.getWorld().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
            loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        }
    }

    @Override
    public void run(Player player, String[] args) throws CrownException, RoyalCommandException {
        String firstArg = args[1];
        CrownUser user = UserManager.getUser(player);
        UserInteractions inter = user.getInteractions();

        if(inter.getMarriedTo() != null) throw FtcExceptionProvider.senderAlreadyMarried();
        if(inter.getWaitingFinish() == null) throw FtcExceptionProvider.translatable("marriage.nooneWaiting");

        CrownUser target = UserManager.getUser(inter.getWaitingFinish());

        if(firstArg.contains("marry")){
            Component message = Component.translatable("marriage.priestText.confirm",
                    user.nickDisplayName().color(NamedTextColor.YELLOW),
                    target.nickDisplayName().color(NamedTextColor.YELLOW)
            )
                    .append(Component.space())
                    .append(Component.translatable("marriage.priestText.confirm.button")
                            .hoverEvent(Component.text("Click me!"))
                            .clickEvent(ClickEventManager.getClickEvent(npcID, "marriageConfirm"))
                            .color(NamedTextColor.AQUA)
                    );

            user.sendMessage(message);
        } else if(firstArg.contains("marriageConfirm")){
            if(awaitingFinishSet.contains(user.getUniqueId())) throw FtcExceptionProvider.translatable("marriage.priestText.alreadyAccepted");
            if(awaitingFinishSet.contains(target.getUniqueId())){
                inter.setMarriedTo(target.getUniqueId());
                inter.setWaitingFinish(null);
                inter.setLastMarriageStatusChange(System.currentTimeMillis());

                UserInteractions tInter = target.getInteractions();
                tInter.setMarriedTo(user.getUniqueId());
                tInter.setWaitingFinish(null);
                tInter.setLastMarriageStatusChange(System.currentTimeMillis());

                target.sendMessage(
                        Component.translatable("marriage.priestText.married", user.nickDisplayName().color(NamedTextColor.YELLOW)).color(NamedTextColor.GOLD)
                );
                user.sendMessage(
                        Component.translatable("marriage.priestText.married", target.nickDisplayName().color(NamedTextColor.YELLOW)).color(NamedTextColor.GOLD)
                );

                awaitingFinishSet.remove(target.getUniqueId());
                return;
            }

            awaitingFinishSet.add(user.getUniqueId());
            user.sendMessage(
                    Component.translatable("marriage.priestText.waiting").color(NamedTextColor.YELLOW)
            );
        }
    }
}

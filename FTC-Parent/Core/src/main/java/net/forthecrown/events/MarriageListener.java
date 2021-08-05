package net.forthecrown.events;

import net.forthecrown.commands.clickevent.ClickEventManager;
import net.forthecrown.commands.clickevent.ClickEventTask;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.marriage.CommandMarry;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
            if(inter.getSpouse() == null) return;
            if(!user.getPlayer().isSneaking()) return;

            CrownUser target = UserManager.getUser(event.getRightClicked().getUniqueId());
            if(!inter.getSpouse().equals(target.getUniqueId())) return;

            CosmeticEmotes.SMOOCH.getCommand().execute(user, target);
        }
    }

    @Override
    public void run(Player player, String[] args) throws RoyalCommandException {
        String firstArg = args[1];
        CrownUser user = UserManager.getUser(player);
        UserInteractions inter = user.getInteractions();

        if(inter.getSpouse() != null) throw FtcExceptionProvider.senderAlreadyMarried();
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
                inter.setSpouse(target.getUniqueId());
                inter.setWaitingFinish(null);
                inter.setLastMarriageChange(System.currentTimeMillis());

                UserInteractions tInter = target.getInteractions();
                tInter.setSpouse(user.getUniqueId());
                tInter.setWaitingFinish(null);
                tInter.setLastMarriageChange(System.currentTimeMillis());

                target.sendMessage(
                        Component.translatable("marriage.priestText.married", user.nickDisplayName().color(NamedTextColor.YELLOW)).color(NamedTextColor.GOLD)
                );
                user.sendMessage(
                        Component.translatable("marriage.priestText.married", target.nickDisplayName().color(NamedTextColor.YELLOW)).color(NamedTextColor.GOLD)
                );

                ForTheCrown.getAnnouncer().announceToAll(
                        Component.text()
                                .append(user.nickDisplayName())
                                .append(Component.text(" is now married to "))
                                .append(target.nickDisplayName())
                                .append(Component.text("!"))
                                .append(giveItAWeek())
                                .build()
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

    private Component giveItAWeek(){
        return FtcUtils.randomInRange(0, 1000) != 1 ? Component.empty() :
                Component.text(" I give it a week").color(NamedTextColor.GRAY);
    }
}

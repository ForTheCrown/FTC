package net.forthecrown.core.events.npc;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.clickevent.ClickEventHandler;
import net.forthecrown.core.clickevent.ClickEventTask;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class JeromeEvent implements Listener, ClickEventTask {

    private final String npcID;

    public JeromeEvent(){
        npcID = ClickEventHandler.registerClickEvent(this);
    }


    //The event
    @EventHandler
    public void onJeromeSpeakTo(PlayerInteractEntityEvent event){
        if(event.getRightClicked().getType() != EntityType.VILLAGER) return;
        if(event.getHand() != EquipmentSlot.HAND) return;
        Villager villie = (Villager) event.getRightClicked();
        if(villie.getCustomName() == null || villie.getCustomName().equals("") || !villie.getCustomName().contains(ChatColor.YELLOW + "Jerome")) return;
        if(!villie.isInvulnerable()) return;
        event.setCancelled(true);

        if(Cooldown.contains(event.getPlayer())) return;
        Cooldown.add(event.getPlayer(), 20);

        Player player = event.getPlayer();
        ClickEventHandler.allowCommandUsage(player, true);

        TextComponent message1 = Component.text("[Info about Knight]")
                .color(NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.runCommand(ClickEventHandler.getCommand(npcID, "info")))
                .hoverEvent(HoverEvent.showText(Component.text("Click me!")));
        TextComponent message2 = Component.text("[Join Knights]")
                .color(NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.runCommand(ClickEventHandler.getCommand(npcID, "join")))
                .hoverEvent(HoverEvent.showText(Component.text("Click me!")));

        player.sendMessage(ChatColor.GOLD + "--" + ChatColor.WHITE + " Hi, what can I do for you? " + ChatColor.GOLD + "--");
        player.sendMessage(message1);
        player.sendMessage(message2);
    }

    //Chat option execution
    @Override
    public void run(Player player, String[] args) {
        //args[0] is the ID of the npc

        if(args[1].contains("join")){
            CrownUser user = FtcCore.getUser(player);
            Branch royals = Branch.ROYALS;

            if(user.getBranch() == Branch.ROYALS){
                player.sendMessage(ChatColor.GRAY + "You are already a part of the Royals!");
                return;
            }

            if(!user.performBranchSwappingCheck()) return;

            if(user.getRank() != Rank.DEFAULT){
                player.sendMessage(ChatColor.GRAY + "You must have the default rank to join the " + royals.getName());
                return;
            }

            user.setBranch(royals);
            user.setCanSwapBranch(false, true);
            player.sendMessage(ChatColor.GRAY + "You are now part of the " + ChatColor.YELLOW + royals.getName() + "!");

        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"\",{\"text\":\"The Knights\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n The noble knights of Hazelguard, a faction of honor most loyal to the crown\\n\\n\"},{\"text\":\"The Dungeons\",\"color\":\"gold\"},{\"text\":\"\\n To get the Knight rank, you must beat the first 3 bosses in The Dungeons and trade-in the golden apples with \"},{\"text\":\"Diego\",\"color\":\"yellow\"},{\"text\":\". This will make you a knight and give you the \"},{\"text\":\"Royal Sword\",\"color\":\"yellow\"},{\"text\":\", an unbreakable golden sword that can be leveled up with mob kills.\\n\\n\"},{\"text\":\"Player Shops\",\"color\":\"gold\"},{\"text\":\"\\n Players of the Knight branch can own shops in Hazelguard. There they can sell or do whatever they like, even start up a casino or create a betting racket.\\n\\n\"},{\"text\":\"Knight Ranks\",\"color\":\"gold\"},{\"text\":\"\\n The knights have the ranks of \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Knight\",\"color\":\"gray\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Baron\",\"color\":\"gray\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Lord\",\"color\":\"gold\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"gray\"},{\"text\":\"Duke\",\"color\":\"gold\"},{\"text\":\"]\",\"color\":\"gray\"},{\"text\":\" and [\"},{\"text\":\"Prince\",\"color\":\"yellow\"},{\"text\":\"], and their female variants.\\n\\n \"}]");
        }
    }
}

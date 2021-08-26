package net.forthecrown.events;

import net.forthecrown.commands.clickevent.ClickEventManager;
import net.forthecrown.commands.clickevent.ClickEventTask;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.CrownWeapons;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.enums.Faction;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class JeromeListener implements Listener, ClickEventTask {

    private final String npcID;

    public JeromeListener(){
        npcID = ClickEventManager.registerClickEvent(this);
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
        ClickEventManager.allowCommandUsage(player, true);

        HoverEvent<Component> clickMe = HoverEvent.showText(Component.text("Click me!"));

        Component message = Component.text()
                .append(Component.text("-- ").color(NamedTextColor.GOLD))
                .append(Component.text("Hi, what can I do for you?"))
                .append(Component.text(" --").color(NamedTextColor.GOLD))

                .append(Component.newline())
                .append(Component.text("[Info about Knight]")
                        .color(NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand(ClickEventManager.getCommand(npcID, "info")))
                        .hoverEvent(clickMe)
                )

                .append(Component.newline())
                .append(Component.text("[Join Knights]")
                        .color(NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand(ClickEventManager.getCommand(npcID, "join")))
                        .hoverEvent(clickMe)
                )

                .append(Component.newline())
                .append(Component.text("[Convert Cutlass]")
                        .color(NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand(ClickEventManager.getCommand(npcID, "sword")))
                        .hoverEvent(clickMe)
                )

                .build();

        player.sendMessage(message);
    }

    //Chat option execution
    @Override
    public void run(Player player, String[] args) {
        //args[0] is the ID of the npc

        if(args[1].contains("join")){
            CrownUser user = UserManager.getUser(player);
            Faction royals = Faction.ROYALS;

            if(user.getFaction() == Faction.ROYALS){
                player.sendMessage(ChatColor.GRAY + "You are already a part of the Royals!");
                return;
            }

            if(!user.performBranchSwappingCheck()) return;

            if(user.getRank() != Rank.DEFAULT){
                player.sendMessage(ChatColor.GRAY + "You must have the default rank to join the " + royals.getName());
                return;
            }

            user.setFaction(royals);
            user.setCanSwapBranch(false, true);
            player.sendMessage(ChatColor.GRAY + "You are now part of the " + ChatColor.YELLOW + royals.getName() + "!");

        } else if(args[1].contains("sword")){
            ItemStack sword = null;
            for(ItemStack stack : player.getInventory()){
                if(!(CrownWeapons.isCrownWeapon(stack) || CrownWeapons.isLegacyWeapon(stack))) continue;

                sword = stack;
                break;
            }

            if(sword == null) {
                player.sendMessage(
                        Component.translatable("pirates.edward.missingSword",
                                NamedTextColor.GRAY,
                                FtcFormatter.itemDisplayName(CrownItems.cutlass())
                        )
                );

                return;
            }

            sword.setType(Material.GOLDEN_SWORD);

            ItemMeta royalSwordMeta = CrownItems.royalSword().getItemMeta();
            ItemMeta meta = sword.getItemMeta();
            meta.displayName(royalSwordMeta.displayName());

            List<Component> lore = meta.lore();
            lore.set(2, royalSwordMeta.lore().get(2));
            lore.set(3, royalSwordMeta.lore().get(3));

            meta.lore(lore);
            sword.setItemMeta(meta);

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            player.sendMessage(Component.text("Sword converted").color(NamedTextColor.GRAY));

        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"\",{\"text\":\"The Knights\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n The noble knights of Hazelguard, a faction of honor most loyal to the crown\\n\\n\"},{\"text\":\"The Dungeons\",\"color\":\"gold\"},{\"text\":\"\\n To get the Knight rank, you must beat the first 3 bosses in The Dungeons and trade-in the golden apples with \"},{\"text\":\"Diego\",\"color\":\"yellow\"},{\"text\":\". This will make you a knight and give you the \"},{\"text\":\"Royal Sword\",\"color\":\"yellow\"},{\"text\":\", an unbreakable golden sword that can be leveled up with mob kills.\\n\\n\"},{\"text\":\"Player Shops\",\"color\":\"gold\"},{\"text\":\"\\n Players of the Knight branch can own shops in Hazelguard. There they can sell or do whatever they like, even start up a casino or create a betting racket.\\n\\n\"},{\"text\":\"Knight Ranks\",\"color\":\"gold\"},{\"text\":\"\\n The knights have the ranks of \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Knight\",\"color\":\"gray\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Baron\",\"color\":\"gray\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Lord\",\"color\":\"gold\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"gray\"},{\"text\":\"Duke\",\"color\":\"gold\"},{\"text\":\"]\",\"color\":\"gray\"},{\"text\":\" and [\"},{\"text\":\"Prince\",\"color\":\"yellow\"},{\"text\":\"], and their female variants.\\n\\n \"}]");
        }
    }
}

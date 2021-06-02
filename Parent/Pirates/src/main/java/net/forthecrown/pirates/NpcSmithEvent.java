package net.forthecrown.pirates;

import net.forthecrown.emperor.clickevent.ClickEventManager;
import net.forthecrown.emperor.clickevent.ClickEventTask;
import net.forthecrown.emperor.inventory.CrownItems;
import net.forthecrown.emperor.inventory.CrownWeapons;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.user.enums.Branch;
import net.forthecrown.emperor.user.enums.Rank;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.emperor.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

public class NpcSmithEvent implements ClickEventTask, Listener {

    private final String clickID;

    public NpcSmithEvent(){
        clickID = ClickEventManager.registerClickEvent(this);
    }

    @EventHandler
    public void onSmithInteract(PlayerInteractEntityEvent event){
        if(event.getRightClicked().getType() != EntityType.VILLAGER) return;
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(Cooldown.contains(event.getPlayer())) return;
        Villager villie = (Villager) event.getRightClicked();
        if(villie.getCustomName() == null || !villie.getCustomName().contains(ChatColor.YELLOW + "Smith")) return;
        if(!villie.isInvulnerable()) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        Cooldown.add(player, 20);
        ClickEventManager.allowCommandUsage(player, true);

        TextComponent message3 = Component.text("[Captain's Cutlass]")
                .color(NamedTextColor.GOLD)
                .clickEvent(ClickEvent.runCommand(ClickEventManager.getCommand(clickID, "cutlass")))
                .hoverEvent(HoverEvent.showText(Component.text("Click me!")));

        TextComponent message2 = Component.text("[Join Pirates]")
                .color(NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.runCommand(ClickEventManager.getCommand(clickID, "join")))
                .hoverEvent(HoverEvent.showText(Component.text("Click me!")));

        TextComponent message1 = Component.text("[Info about Pirates]")
                .color(NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.runCommand(ClickEventManager.getCommand(clickID, "info")))
                .hoverEvent(HoverEvent.showText(Component.text("Click me!")));

        player.sendMessage(ChatColor.GOLD + "--" + ChatColor.WHITE + " Aye mate, what can I do for ya? " + ChatColor.GOLD + "--");
        player.sendMessage(message1);
        player.sendMessage(message2);
        player.sendMessage(message3);
    }

    @Override
    public void run(Player player, String[] args) {

        CrownUser user = UserManager.getUser(player.getUniqueId());

        if(args[1].contains("cutlass")){
            if(!player.hasPermission("ftc.donator2") && user.getBranch() != Branch.PIRATES){
                user.sendMessage("&7Only pirate captains can get this weapon!");
                return;
            }

            ItemStack sword = null;
            for(ItemStack stack : player.getInventory()){
                if(!(CrownWeapons.isCrownWeapon(stack) || CrownWeapons.isLegacyWeapon(stack))) continue;

                sword = stack;
                break;
            }
            if(sword == null){
                user.sendMessage("&7You need to have a Royal Sword in your inventory");
                return;
            }

            sword.setType(Material.NETHERITE_SWORD);

            ItemMeta meta = sword.getItemMeta();
            meta.displayName(CrownItems.BASE_CUTLASS.getItemMeta().displayName());
            List<String> lores = meta.getLore();
            lores.set(2, ChatFormatter.translateHexCodes("&#917558The bearer of this cutlass bows to no laws, to no king,"));
            lores.set(3, ChatFormatter.translateHexCodes("&#917558its wielder leads their crew towards everlasting riches."));
            meta.setLore(lores);
            sword.setItemMeta(meta);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            player.sendMessage(Component.text("Sword converted").color(NamedTextColor.GRAY));

        } else if(args[1].contains("join")){
            if(user.getBranch() == Branch.PIRATES){
                user.sendMessage("&7You are already a pirate.");
                return;
            }
            if(!user.performBranchSwappingCheck()) return;
            if(user.getRank() != Rank.DEFAULT){
                user.sendMessage("&7You need to be the default rank to join the Pirates.");
                return;
            }

            user.setBranch(Branch.PIRATES);
            user.setCanSwapBranch(false, true);
            user.sendMessage("You're now a pirate!");
        } else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + "  [\"\",{\"text\":\"The Pirates\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n\\n\"},{\"text\":\"The Pirates\",\"color\":\"gold\"},{\"text\":\" are all about keeping their business on the down low. Trading \",\"color\":\"#DCDDDE\"},{\"text\":\"contraband\",\"color\":\"red\"},{\"text\":\" items is, after all, punished by death in \",\"color\":\"#DCDDDE\"},{\"text\":\"Hazelguard\",\"color\":\"gold\"},{\"text\":\". \",\"color\":\"#DCDDDE\"},{\"text\":\"\\n\\n\"},{\"text\":\"Joining the Pirates\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n\\n\"},{\"text\":\"To join \",\"color\":\"#DCDDDE\"},{\"text\":\"The Pirates\",\"color\":\"gold\"},{\"text\":\", you must convince \",\"color\":\"#DCDDDE\"},{\"text\":\"Smith\",\"color\":\"yellow\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Founder of Questmoor\"}},{\"text\":\" to accept you in to their ranks. If \",\"color\":\"#DCDDDE\"},{\"text\":\"Smith\",\"color\":\"yellow\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Founder of Questmoor\"}},{\"text\":\" accepts you, you must gather Pirate Points to prove yourself worthy of the \",\"color\":\"#DCDDDE\"},{\"text\":\"[Sailor]\",\"color\":\"gray\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Earned by obtaining 10PP\"}},{\"text\":\" or \",\"color\":\"#DCDDDE\"},{\"text\":\"[Pirate]\",\"color\":\"gray\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Earned by obtaining 50PP\"}},{\"text\":\" rank, However, \",\"color\":\"#DCDDDE\"},{\"text\":\"Captain\",\"color\":\"gold\"},{\"text\":\" and \",\"color\":\"#DCDDDE\"},{\"text\":\"Admiral\",\"color\":\"yellow\"},{\"text\":\" must be bought from the \",\"color\":\"#DCDDDE\"},{\"text\":\"[Webstore]\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/shop web\"}},{\"text\":\" (Tier 2 and Tier 3). To collect PP (Pirate Points) you must either finish levels in the Grappling Hook Parkour or find treasure chests in the \",\"color\":\"#DCDDDE\"},{\"text\":\"RW\",\"color\":\"green\"},{\"text\":\". In addition, you can earn Rhines and PP by selling heads to the dreaded \",\"color\":\"#DCDDDE\"},{\"text\":\"Captain\",\"color\":\"gold\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Oddly fond of heads\"}},{\"text\":\" \",\"color\":\"#DCDDDE\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Oddly fond of heads\"}},{\"text\":\"Wilhelm\",\"color\":\"yellow\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Oddly fond of heads\"}},{\"text\":\" aboard the Blue Rift.\",\"color\":\"#DCDDDE\"},{\"text\":\"\\n\\n\"},{\"text\":\"Black Market\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n\\n\"},{\"text\":\"The Pirates\",\"color\":\"gold\"},{\"text\":\" have a \",\"color\":\"#DCDDDE\"},{\"text\":\"Black Market\",\"color\":\"gold\"},{\"text\":\", where they can sell a certain amount of goods. In addition, you can buy parrots that never leave you, alongside slaves from \",\"color\":\"#DCDDDE\"},{\"text\":\"Ramun the Slave-trader\",\"color\":\"yellow\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Highly respected citizen of Questmoor\"}},{\"text\":\". In the center, inbetween \",\"color\":\"#DCDDDE\"},{\"text\":\"Ramun\",\"color\":\"yellow\"},{\"text\":\" and the 3 Tricky Traders, we have \",\"color\":\"#DCDDDE\"},{\"text\":\"Edward\",\"color\":\"yellow\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"The Scum of Questmoor\"}},{\"text\":\". He sells ancient tomes that can no longer be obtained, for a price that will make even the worst pirate's bones shake! \",\"color\":\"#DCDDDE\"},{\"text\":\"\\n\\n\"},{\"text\":\"The Never Ending Battle\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n\\n\"},{\"text\":\"\\\"For centuries, the royal scum of \",\"color\":\"#DCDDDE\"},{\"text\":\"Hazelguard\",\"color\":\"gold\"},{\"text\":\" has opposed our kin. They have banished us, punished us, for simply living out our legacy. All Pirate business in \",\"color\":\"#DCDDDE\"},{\"text\":\"Hazelguard\",\"color\":\"gold\"},{\"text\":\" has been closed down, and they have even banned us from using their shops! It's their loss, they couldn't handle the competition. Since these events unfolded, we have kept our trading on boats around the waters of \",\"color\":\"#DCDDDE\"},{\"text\":\"Questmoor\",\"color\":\"gold\"},{\"text\":\".\\\" \",\"color\":\"#DCDDDE\"},{\"text\":\"-Smith\",\"color\":\"yellow\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Founder of Questmoor\"}}]");
    }
}

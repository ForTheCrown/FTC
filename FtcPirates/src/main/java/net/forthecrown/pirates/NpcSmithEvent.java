package net.forthecrown.pirates;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.clickevent.ClickEventHandler;
import net.forthecrown.core.clickevent.ClickEventTask;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
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
        clickID = ClickEventHandler.registerClickEvent(this);
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
        ClickEventHandler.allowCommandUsage(player, true);

        TextComponent message1 = new TextComponent(ChatColor.YELLOW + "[Info about Pirates]");
        message1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ClickEventHandler.getCommand(clickID, "info")));
        message1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click me!")));

        TextComponent message2 = new TextComponent(ChatColor.YELLOW + "[Join Pirates]");
        message2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ClickEventHandler.getCommand(clickID, "join")));
        message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click me!")));

        TextComponent message3 = new TextComponent(ChatColor.GOLD + "[Captain's Cutlass]");
        message3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ClickEventHandler.getCommand(clickID, "cutlass")));
        message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click me!")));

        player.sendMessage(ChatColor.GOLD + "--" + ChatColor.WHITE + " Aye mate, what can I do for ya? " + ChatColor.GOLD + "--");
        player.spigot().sendMessage(message1);
        player.spigot().sendMessage(message2);
        player.spigot().sendMessage(message3);
    }

    @Override
    public void run(Player player, String[] args) {

        CrownUser user = FtcCore.getUser(player.getUniqueId());

        if(args[1].contains("cutlass")){
            if(!player.hasPermission("ftc.donator2") && user.getBranch() != Branch.PIRATES){
                user.sendMessage("&7Only pirate captains can get this weapon!");
                return;
            }

            ItemStack sword = null;
            for(ItemStack stack : player.getInventory()){
                if(stack == null) continue;

                if(stack.getType() != Material.GOLDEN_SWORD && !stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) continue;
                if(!stack.getItemMeta().getDisplayName().contains(CrownUtils.translateHexCodes("&6-&e&lRoyal Sword&6-"))) continue;

                sword = stack;
                break;
            }
            if(sword == null){
                user.sendMessage("&7You need to have a Royal Sword in your inventory");
                return;
            }

            sword.setType(Material.NETHERITE_SWORD);

            ItemMeta meta = sword.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + CrownUtils.translateHexCodes("&#917558-&#D1C8BA&lCaptain's Cutlass&#917558-"));
            List<String> lores = meta.getLore();
            lores.set(2, CrownUtils.translateHexCodes("&#917558The brearer of this cutlass bows to no laws, to no king,"));
            lores.set(3, CrownUtils.translateHexCodes("&#917558its wielder leads their crew towards everlasting riches."));
            meta.setLore(lores);
            sword.setItemMeta(meta);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        } else if(args[1].contains("join")){
            if(user.getBranch() == Branch.PIRATES){
                user.sendMessage("&7You are already a pirate.");
                return;
            }
            if(user.getRank() != Rank.DEFAULT){
                user.sendMessage("&7You need to be the default rank to join the Pirates.");
                return;
            }

            user.setBranch(Branch.PIRATES);
            user.sendMessage("You're now a pirate!");
        } else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"\",{\"text\":\"The Pirates\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n The pirates are a new faction, which is all about illegal merchandise!\\n\\n\"},{\"text\":\"Joining the Pirates\",\"color\":\"gold\"},{\"text\":\"\\n To join the pirates you'll need 10 PP, Pirate Points. To get PP you must either finish levels in the Grappling Hook Parkour or find treasure chests in the RW. In addition, you can earn money and PP by selling heads to the dreaded \"},{\"text\":\"Captain Willhelm\",\"color\":\"yellow\"},{\"text\":\".\\n\\n\"},{\"text\":\"Pirate Ranks\",\"color\":\"gold\"},{\"text\":\"\\n The pirates have their own ranks: Sailor, Pirate, Captain and Admiral.\\n Sailor can be gotten with 10 PP, but Pirate will require you to have 50 PP. Captain and Admiral must be bought from the webstore (Tier 2 and Tier 3)\\n\\n\"},{\"text\":\"Black Market\",\"color\":\"gold\"},{\"text\":\"\\n The pirates have a black market. where they can sell goods without a price decline. In addition, you can buy parrots, that never leave you, alongside slaves from \"},{\"text\":\"Ramun the Slave-trader\",\"color\":\"yellow\"},{\"text\":\".\\n\\n However, Hazelguard will not allow people guilty of piracy to own shops in town, so pirates will have their shops seized and they won't be able to open new ones. The pirates do however have their own shops in the Black market.\"}]");
    }
}

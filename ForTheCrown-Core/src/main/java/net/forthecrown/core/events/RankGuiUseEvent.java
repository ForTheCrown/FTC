package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.inventories.RankInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

public class RankGuiUseEvent implements Listener {

    //Rank GUI event
    @EventHandler
    public void onInvClick(InventoryClickEvent event){
        if(!event.getView().getTitle().contains("Pirates") && !event.getView().getTitle().contains("Royals") && !event.getView().getTitle().contains("Vikings")) return;
        if(event.isShiftClick()){ event.setCancelled(true); return; }
        if(event.getClickedInventory() instanceof PlayerInventory) return;
        if(!(event.getClickedInventory() instanceof PlayerInventory)) event.setCancelled(true);
        if(event.getCurrentItem() == null) return;

        event.setCancelled(true);

        if(event.getCurrentItem().getType() != Material.PAPER && event.getCurrentItem().getType() != Material.MAP && event.getCurrentItem().getType() != Material.GLOBE_BANNER_PATTERN) return;
        if(!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        Player player = (Player) event.getWhoClicked();
        CrownUser user = FtcCore.getUser(player.getUniqueId());
        RankInventory rankInv = new RankInventory(user);
        String currentInvTitle = event.getView().getTitle();
        String clickedRankString = event.getCurrentItem().getItemMeta().getDisplayName();

        if(clickedRankString.contains("Next page")){
            switch (currentInvTitle){
                case "Pirates":
                    //player.openInventory(rankInv.getVikingsGUI());
                    player.openInventory(rankInv.getRoyalsGUI());
                    return;
                case "Royals":
                    player.openInventory(rankInv.getPiratesGUI());
                    return;
                case "Vikings":
                    player.openInventory(rankInv.getRoyalsGUI());
                    return;
            }
        }

        Rank clickedRank = Rank.fromPrefix(clickedRankString);
        if(clickedRank == null) return;

        if(!user.hasRank(clickedRank)){
            user.sendMessage("&7You don't have this rank");
            return;
        }

        if(event.getCurrentItem().getEnchantments().size() > 0){
            user.sendMessage("&7This is already your rank!");
            return;
        }

        if(clickedRank.getRankBranch() != Branch.DEFAULT){
            if(user.getBranch() != clickedRank.getRankBranch()) throw new InvalidCommandExecution(user, "&cThis rank is not in your branch!");
        }

        if(clickedRank == Rank.DEFAULT){
            user.setRank(clickedRank, false);
            user.clearTabPrefix();
            user.sendMessage("&7Your rank is now the default rank.");
        } else{
            user.setRank(clickedRank);
            user.sendMessage(clickedRank.getPrefix() + "&7has become your new rank! (Branch: " + user.getBranch().getName() + ")");
        }

        //reload inventory
        switch (currentInvTitle){
            case "Pirates":
                player.openInventory(rankInv.getPiratesGUI());
                break;
            case "Royals":
                player.openInventory(rankInv.getRoyalsGUI());
                break;
            case "Vikings":
                player.openInventory(rankInv.getVikingsGUI());
                break;
        }
    }
}

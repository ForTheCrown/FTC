package net.forthecrown.events.dynamic;

import net.forthecrown.core.CrownException;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.inventory.RankInventory;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Faction;
import net.forthecrown.user.data.Rank;
import net.forthecrown.user.manager.UserManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.PlayerInventory;

public class RankGuiUseEvent implements Listener {

    private final Player player;

    public RankGuiUseEvent(Player player){
        this.player = player;
    }

    //Rank GUI event
    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if(!event.getWhoClicked().equals(player)) return;
        if(event.isShiftClick()){ event.setCancelled(true); return; }
        if(event.getClickedInventory() instanceof PlayerInventory) return;
        event.setCancelled(true);
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getType() != Material.PAPER && event.getCurrentItem().getType() != Material.MAP && event.getCurrentItem().getType() != Material.GLOBE_BANNER_PATTERN) return;
        if(!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        Player player = (Player) event.getWhoClicked();
        CrownUser user = UserManager.getUser(player.getUniqueId());
        RankInventory rankInv = new RankInventory(user);
        String currentInvTitle = ChatColor.stripColor(ChatUtils.getString(event.getView().title()));
        String clickedRankString = ChatUtils.getString(event.getCurrentItem().getItemMeta().displayName());

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

        if(clickedRank.getFaction() != Faction.DEFAULT){
            if(user.getFaction() != clickedRank.getFaction()) throw new CrownException(user, "&cThis rank is not in your branch!");
        }

        if(clickedRank == Rank.DEFAULT){
            user.setRank(clickedRank, false);
            if(!user.isKing()) user.setCurrentPrefix(null);
            user.sendMessage("&7Your rank is now the default rank.");
        }
        else{
            if(!user.isKing()) user.setRank(clickedRank);
            else user.sendMessage("&7You are currently the king, you will see your rank prefix once another player becomes king");
            user.sendMessage(clickedRank.getPrefix() + "&7has become your new rank! (Branch: " + user.getFaction().getName() + ")");
        }

        //reload inventory
        switch (currentInvTitle) {
            case "Pirates" -> player.openInventory(rankInv.getPiratesGUI());
            case "Royals" -> player.openInventory(rankInv.getRoyalsGUI());
            case "Vikings" -> player.openInventory(rankInv.getVikingsGUI());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!event.getPlayer().equals(player)) return;
        if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

        HandlerList.unregisterAll(this);
    }
}

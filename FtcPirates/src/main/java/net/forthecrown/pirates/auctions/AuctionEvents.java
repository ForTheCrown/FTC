package net.forthecrown.pirates.auctions;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class AuctionEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!(event.getBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) event.getBlock().getState();
        if(!sign.getPersistentDataContainer().has(AuctionManager.AUCTION_KEY, PersistentDataType.BYTE)) return;

        Auction auction = AuctionManager.getAuction(sign.getLocation());
        Validate.notNull(auction, "Auction is null :(");
        if(!event.getPlayer().hasPermission("ftc.admin")) return;

        auction.delete();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(!(event.getClickedBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) event.getClickedBlock().getState();
        if(!sign.getPersistentDataContainer().has(AuctionManager.AUCTION_KEY, PersistentDataType.BYTE)) return;

        String line3 = CrownUtils.getFullStringComponents((TextComponent) sign.line(3));

        Player p = event.getPlayer();
        Auction auction = AuctionManager.getAuction(sign.getLocation());
        Validate.notNull(auction, "Auction is null :(");

        if(auction.isWaitingForItemClaim() || !auction.performExpiryCheck()) { //Checks if the auction has expired and is waiting for the highest bidder to pick up their item
            auction.attemptItemClaim(FtcCore.getUser(p)); //Runs a check to see if the player is the right user and if they have the balance n stuffs
            return;
        }

        if(line3.contains("NONE")){ //Unclaimed auction
            p.sendMessage(ChatColor.GRAY + "This auction is currently not in use!");

            //claim shop option
            TextComponent claimOption = CrownUtils.makeComponent("[Click here and enter a minimum bid to claim]", NamedTextColor.AQUA,
                    ClickEvent.suggestCommand("/au " + auction.getName() + " claim "),
                    HoverEvent.showText(Component.text("Enter the starting price for the auction")));
            p.sendMessage(claimOption);

            return;
        }

        if(auction.getHighestBid() == 1000000){
            p.sendMessage("This auction has reached it's bidding limit");
            return;
        }

        p.sendMessage(ChatColor.GRAY + "The current top bid is: " + ChatColor.YELLOW + auction.getHighestBid() + " Rhines." + ChatColor.GRAY + " Bidder: " + ChatColor.YELLOW + auction.getHighestBidder().getName());
        p.sendMessage(ChatColor.GRAY + "The auction will expire in " + ChatColor.YELLOW + CrownUtils.convertMillisIntoTime(auction.getExpiresAt() - System.currentTimeMillis()));

        //bidding option
        TextComponent text = CrownUtils.makeComponent("[Bid]", NamedTextColor.AQUA,
                ClickEvent.suggestCommand("/au " + auction.getName() + " bid " + (auction.getHighestBid() + 1)),
                HoverEvent.showText(Component.text("Bid on this auction.")));

        TextComponent message = Component.text("Would you like to bid on this? ").color(NamedTextColor.GRAY);
        message = message.append(text);

        p.sendMessage(message);
    }
}

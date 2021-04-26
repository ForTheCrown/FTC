package net.forthecrown.pirates.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.auctions.Auction;
import net.forthecrown.pirates.auctions.AuctionManager;
import net.forthecrown.pirates.auctions.PirateAuction;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CommandAuction extends CrownCommandBuilder {

    public CommandAuction(){
        super("auction", Pirates.inst);

        setDescription("The primary command used for interacting with AuctionSigns");
        setAliases("au");
        setPermission("ftc.pirates.auctions");
        register();
    }

    private static final String AUCTION_ARG = "auction";
    public final Set<String> claimedClaiming = new HashSet<>();

    @Override
    protected void registerCommand(BrigadierCommand command) {
        Balances bals = FtcCore.getBalances();

        command
                .then(argument("save")
                        .requires(c -> c.getBukkitSender().hasPermission(getPermission() + ".admin"))

                        .executes(c -> {
                            Pirates.getAuctionManager().saveAuctions();
                            broadcastAdmin(c.getSource(), "All auctions saved");
                            return 0;
                        })
                )
                .then(argument("reload")
                        .requires(c -> c.getBukkitSender().hasPermission(getPermission() + ".admin"))

                        .executes(c -> {
                            Pirates.getAuctionManager().reloadAuctions();
                            broadcastAdmin(c.getSource(), "All auctions reloaded");
                            return 0;
                        })
                )

                .then(argument("create")
                        .requires(c -> c.getBukkitSender().hasPermission(getPermission() + ".admin"))

                        .then(argument("name", StringArgumentType.word())
                                .executes(c -> {
                                    Player p = getPlayerSender(c);

                                    Block b = p.getTargetBlock(5);
                                    if(b == null || !(b.getState() instanceof Sign)) throw new CrownCommandException("&7You need to be looking at a sign!");

                                    Sign sign = (Sign) b.getState();

                                    String name = c.getArgument("name", String.class);
                                    if(AuctionManager.getAuctionNames().contains(name)) throw new CrownCommandException("&7An auction by this name already exists");

                                    Auction auction = new PirateAuction(sign.getLocation(), name);
                                    auction.unClaim();

                                    broadcastAdmin(c.getSource(), auction.getName() + " created!");
                                    return 0;
                                })
                        )
        )
        .then(argument(AUCTION_ARG, StringArgumentType.word())
                .suggests(this::listAuctions)

                .then(argument("expire")
                        .requires(c -> c.getBukkitSender().hasPermission(getPermission() + ".admin"))

                        .executes(c -> {
                            Auction auction = auctionFromArg(c);

                            if(!auction.isClaimed()) throw new CrownCommandException("&7The auction is not claimed!");
                            if(auction.isWaitingForItemClaim()) throw new CrownCommandException("&7The auction is already expired");

                            auction.setExpiresAt(100L);
                            auction.performExpiryCheck();
                            auction.updateSign();
                            broadcastAdmin(c.getSource(), auction.getName() + " set as expired.");
                            return 0;
                        })
                )
                .then(argument("delete")
                        .requires(c -> c.getBukkitSender().hasPermission(getPermission() + ".admin"))

                        .executes(c ->{
                            Auction auction = auctionFromArg(c);
                            auction.delete();
                            broadcastAdmin(c.getSource(), auction.getName() + " deleted");
                            return 0;
                        })
                )

                .then(argument("bid")
                        .then(argument("amount", IntegerArgumentType.integer(1, 1000000))
                                .executes(c -> {
                                    CrownUser user = getUserExecutor(c);
                                    Auction auction = auctionFromArg(c);

                                    if(!auction.isClaimed()) throw new CrownCommandException("&7The auction is not claimed!");
                                    if(auction.isWaitingForItemClaim()) throw new CrownCommandException("&7You can't bid on an auction that's expired");

                                    Integer amount = c.getArgument("amount", Integer.class);
                                    if(amount <= auction.getHighestBid()) throw new CrownCommandException("&7You cannot bid less than the current amount");
                                    if(amount > bals.get(user.getUniqueId())) throw new CrownCommandException("&7You cannot afford that");

                                    auction.bidOn(user, amount);
                                    user.sendMessage("&7You have bid!");
                                    return 0;
                                })
                        )
                )
                .then(argument("claim")
                        .requires(this::isAllowedToOwn)

                        .then(argument("startingBid", IntegerArgumentType.integer(1, 500000))
                                .executes(c -> {
                                    CrownUser user = getUserExecutor(c);
                                    Auction auction = auctionFromArg(c);

                                    if(user.getBranch() != Branch.PIRATES) throw new CrownCommandException("&7You must be a pirate to claim an auction");
                                    if(!isAllowedToOwn(c.getSource())) throw new CrownCommandException("&7You aren't allowed to own more than one auction at a time");
                                    if(claimedClaiming.contains(auction.getName())) throw new CrownCommandException("&7Someone is already claiming this Auction");
                                    if(auction.isClaimed()) throw new CrownCommandException("&7" + auction.getName() + " is already claimed by " + auction.getOwner().getName());

                                    Pirates.inst.getServer().getPluginManager().registerEvents(
                                            new AuctionClaiming(user, auction, c.getArgument("startingBid", Integer.class)),
                                            Pirates.inst);
                                    return 0;
                                })
                        )
                )
        );
    }

    protected CrownUser getUserExecutor(CommandContext<CommandListenerWrapper> c) throws CommandSyntaxException {
        CrownUser toReturn = super.getUserSender(c);
        if(!AuctionManager.AUCTION_AREA.getPlayers().contains(toReturn.getPlayer())) throw new CrownCommandException("&7You must be near the auction area to interact with it!");
        return toReturn;
    }

    private boolean isAllowedToOwn(CommandListenerWrapper source){
        UUID id = source.getBukkitEntity().getUniqueId();

        for (Auction a: AuctionManager.getAuctions().values()){
            if(a.getOwner().getUniqueId().equals(id)) return false;
        }
        return true;
    }

    private Auction auctionFromArg(CommandContext<CommandListenerWrapper> c) throws CrownCommandException {
        String name = c.getArgument(AUCTION_ARG, String.class);
        Auction toReturn = AuctionManager.getAuction(name);
        if(toReturn == null) throw new CrownCommandException(name + " is not a valid auction name");
        return toReturn;
    }

    private CompletableFuture<Suggestions> listAuctions(CommandContext<CommandListenerWrapper> c, SuggestionsBuilder b){
        return suggestMatching(b, AuctionManager.getAuctionNames());
    }

    private class AuctionClaiming implements Listener, InventoryHolder {

        private final CrownUser owner;
        private final Auction auction;
        private final int startingBid;

        public AuctionClaiming(CrownUser owner, Auction auction, int startingBid) {
            this.owner = owner;
            this.auction = auction;
            this.startingBid = startingBid;

            claimedClaiming.add(auction.getName());
            owner.getPlayer().openInventory(getInventory());
        }

        @Override
        public @NotNull Inventory getInventory() {
            Inventory toReturn = Bukkit.createInventory(this, InventoryType.HOPPER, Component.text("Specify what and how much"));

            final ItemStack barrier = new ItemStack(Material.BARRIER);
            toReturn.setItem(0, barrier);
            toReturn.setItem(1, barrier);
            toReturn.setItem(3, barrier);
            toReturn.setItem(4, barrier);

            return toReturn;
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            if(!event.getWhoClicked().equals(owner.getPlayer())) return;
            if(event.getClickedInventory() instanceof PlayerInventory) return;
            if(event.getSlot() != 2) event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClose(InventoryCloseEvent event) throws CrownException {
            if(!event.getPlayer().equals(owner.getPlayer())) return;
            if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            HandlerList.unregisterAll(this);

            claimedClaiming.remove(auction.getName());

            ItemStack item = event.getInventory().getItem(2);
            if(item == null) throw new CrownException(event.getPlayer(), "Auction claiming failed, no item in slot");

            if(auction.isClaimed()){
                event.getPlayer().getInventory().addItem(item);
                return;
            }

            auction.setClaimed(owner, startingBid, item, false);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Auction claimed!");
            FtcCore.getRoyalBrigadier().resendCommandPackets((Player) event.getPlayer());
        }
    }
}
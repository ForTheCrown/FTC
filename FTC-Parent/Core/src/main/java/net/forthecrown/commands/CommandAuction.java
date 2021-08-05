package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.AuctionArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.economy.Balances;
import net.forthecrown.economy.auctions.Auction;
import net.forthecrown.economy.auctions.CrownAuction;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.pirates.AuctionManager;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.Branch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class CommandAuction extends FtcCommand {

    public CommandAuction(){
        super("auction");

        setDescription("The primary command used for interacting with AuctionSigns");
        setAliases("au");
        setPermission("ftc.pirates.auctions");
        register();
    }

    private static final String AUCTION_ARG = "auction";
    public final Set<String> claimedClaiming = new HashSet<>();

    @Override
    protected void createCommand(BrigadierCommand command) {
        Balances bals = ForTheCrown.getBalances();

        command
                .then(literal("save")
                        .requires(c -> c.hasPermission(getPermission() + ".admin"))

                        .executes(c -> {
                            Pirates.getAuctions().saveAuctions();
                            broadcastAdmin(c.getSource(), "All auctions saved");
                            return 0;
                        })
                )
                .then(literal("reload")
                        .requires(c -> c.hasPermission(getPermission() + ".admin"))

                        .executes(c -> {
                            Pirates.getAuctions().reloadAuctions();
                            broadcastAdmin(c.getSource(), "All auctions reloaded");
                            return 0;
                        })
                )

                .then(literal("create")
                        .requires(c -> c.hasPermission(getPermission() + ".admin"))

                        .then(argument("name", StringArgumentType.word())
                                .executes(c -> {
                                    Player p = getPlayerSender(c);

                                    Block b = p.getTargetBlock(5);
                                    if(b == null || !(b.getState() instanceof Sign)) throw FtcExceptionProvider.create("&7You need to be looking at a sign!");

                                    Sign sign = (Sign) b.getState();

                                    String name = c.getArgument("name", String.class);
                                    if(AuctionManager.getAuctionNames().contains(name)) throw FtcExceptionProvider.create("&7An auction by this name already exists");

                                    Auction auction = new CrownAuction(sign.getLocation(), name);
                                    auction.unClaim();

                                    broadcastAdmin(c.getSource(), auction.getName() + " created!");
                                    return 0;
                                })
                        )
        )
        .then(argument(AUCTION_ARG, AuctionArgument.auction())
                .suggests(suggestMatching(AuctionManager.getAuctionNames()))

                .then(literal("expire")
                        .requires(c -> c.hasPermission(getPermission() + ".admin"))

                        .executes(c -> {
                            Auction auction = auctionFromArg(c);

                            if(!auction.isClaimed()) throw FtcExceptionProvider.create("&7The auction is not claimed!");
                            if(auction.isWaitingForItemClaim()) throw FtcExceptionProvider.create("&7The auction is already expired");

                            auction.setExpiresAt(100L);
                            auction.performExpiryCheck();
                            auction.updateSign();
                            broadcastAdmin(c.getSource(), auction.getName() + " set as expired.");
                            return 0;
                        })
                )

                .then(literal("force_free_for_all")
                        .requires(c -> c.hasPermission(getPermission() + ".admin"))

                        .executes(c -> {
                            Auction auction = auctionFromArg(c);

                            if(!auction.isWaitingForItemClaim()) throw FtcExceptionProvider.create("The auction is not expired");

                            auction.setFreeForAll(System.currentTimeMillis() - 100);
                            auction.performExpiryCheck();

                            c.getSource().sendAdmin(auction.getName() + " set as free for all");
                            return 0;
                        })
                )

                .then(literal("delete")
                        .requires(c -> c.hasPermission(getPermission() + ".admin"))

                        .executes(c ->{
                            Auction auction = auctionFromArg(c);
                            auction.delete();
                            broadcastAdmin(c.getSource(), auction.getName() + " deleted");
                            return 0;
                        })
                )

                .then(literal("bid")
                        .then(argument("amount", IntegerArgumentType.integer(1, 1000000))
                                .executes(c -> {
                                    CrownUser user = getUserExecutor(c);
                                    Auction auction = auctionFromArg(c);

                                    if(!auction.isClaimed()) throw FtcExceptionProvider.create("&7The auction is not claimed!");
                                    if(auction.isWaitingForItemClaim()) throw FtcExceptionProvider.create("&7You can't bid on an auction that's expired");

                                    Integer amount = c.getArgument("amount", Integer.class);
                                    if(amount <= auction.getHighestBid()) throw FtcExceptionProvider.create("&7You cannot bid less than the current amount");
                                    if(amount > bals.get(user.getUniqueId())) throw FtcExceptionProvider.create("&7You cannot afford that");
                                    if(auction.getOwner().equals(user)) throw FtcExceptionProvider.create("&7You cannot bid on your own auction");

                                    auction.bidOn(user, amount);
                                    user.sendMessage("&7You have bid!");
                                    return 0;
                                })
                        )
                )
                .then(literal("claim")
                        .then(argument("startingBid", IntegerArgumentType.integer(1, 500000))
                                .executes(c -> {
                                    CrownUser user = getUserExecutor(c);
                                    Auction auction = auctionFromArg(c);

                                    if(user.getBranch() != Branch.PIRATES) throw FtcExceptionProvider.notPirate();
                                    if(!isAllowedToOwn(c.getSource())) throw FtcExceptionProvider.create("&7You aren't allowed to own more than one auction at a time");
                                    if(claimedClaiming.contains(auction.getName())) throw FtcExceptionProvider.create("&7Someone is already claiming this Auction");
                                    if(auction.isClaimed()) throw FtcExceptionProvider.create("&7" + auction.getName() + " is already claimed by " + auction.getOwner().getName());

                                    Bukkit.getPluginManager().registerEvents(
                                            new AuctionClaiming(user, auction, c.getArgument("startingBid", Integer.class)),
                                            ForTheCrown.inst());
                                    return 0;
                                })
                        )
                )
        );
    }

    protected CrownUser getUserExecutor(CommandContext<CommandSource> c) throws CommandSyntaxException {
        CrownUser toReturn = super.getUserSender(c);
        if(!AuctionManager.AUCTION_AREA.getPlayers().contains(toReturn.getPlayer())) throw FtcExceptionProvider.create("&7You must be near the auction area to interact with it!");
        return toReturn;
    }

    private boolean isAllowedToOwn(CommandSource source){
        try {
            UUID id = source.asPlayer().getUniqueId();

            for (Auction a: AuctionManager.getAuctions().values()){
                if(a == null || !a.isClaimed() || a.getOwner() == null) continue;
                if(a.getOwner().getUniqueId().equals(id)) return false;
            }
            return true;
        } catch (CommandSyntaxException e){
            return false;
        }
    }

    private Auction auctionFromArg(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return c.getArgument(AUCTION_ARG, Auction.class);
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
        public void onInventoryClose(InventoryCloseEvent event) {
            if(!event.getPlayer().equals(owner.getPlayer())) return;
            if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            HandlerList.unregisterAll(this);

            claimedClaiming.remove(auction.getName());

            ItemStack item = event.getInventory().getItem(2);
            if(item == null) {
                owner.sendMessage(Component.translatable("auction.claimingFailed", NamedTextColor.GRAY));
                return;
            }

            if(auction.isClaimed()){
                event.getPlayer().getInventory().addItem(item);
                return;
            }

            auction.setClaimed(owner, startingBid, item, false);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Auction claimed!");
        }
    }
}
package net.forthecrown.core.events;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.BlackMarket;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.clickevent.ClickEventHandler;
import net.forthecrown.core.clickevent.ClickEventTask;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.exceptions.CannotAffordTransaction;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.NoRankException;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BlackMarketEvents implements Listener, ClickEventTask {

    private final String npcID;

    public BlackMarketEvents(){
        this.npcID = ClickEventHandler.registerClickEvent(this);
    }

    @EventHandler
    public void onBlackMarketUse(PlayerInteractEntityEvent event) {
        if(event.getRightClicked().getType() != EntityType.VILLAGER) return;
        if(event.getHand() != EquipmentSlot.HAND) return;
        Villager villie = (Villager) event.getRightClicked();
        if(!villie.isInvulnerable()) return;
        if(villie.getCustomName() == null) return;
        if(!villie.getCustomName().contains("George") && !villie.getCustomName().contains("Otto") && !villie.getCustomName().contains("Herbert") &&
                !villie.getCustomName().contains("Edward") && !villie.getCustomName().contains("Ramun")) return;

        event.setCancelled(true);

        if(Cooldown.contains(event.getPlayer())) return;
        Cooldown.add(event.getPlayer(), 20);

        Player player = event.getPlayer();
        BlackMarket bm = FtcCore.getBlackMarket();
        CrownUser user = FtcCore.getUser(player.getUniqueId());

        if(user.getBranch() != Branch.PIRATES) throw new CrownException(player, "&e" + villie.getCustomName() + " only trusts real pirates");

        String customName = villie.getCustomName();

        if(customName.contains("George")) player.openInventory(bm.getMiningInventory(user));
        else if(customName.contains("Otto")) player.openInventory(bm.getDropInventory(user));
        else if(customName.contains("Herbert")) player.openInventory(bm.getFarmingInventory(user));
        else if(customName.contains("Edward")){
            if(!bm.isAllowedToBuyEnchant(player)) throw new CrownException(player, "-&eYou've already purchased from me today, scram!&r-");
            if(!bm.enchantAvailable()) throw new CrownException(player, "-&eUnfortunately, my good sir, I don't have anything to sell you at this moment&r-");
            doEdwardStuff(user, bm);
        }
        else if(customName.contains("Ramun")) player.openInventory(bm.getParrotInventory());

        if(!customName.contains("Edward")){
            FtcCore.getInstance().getServer().getPluginManager().registerEvents(new BmSubClass1(player), FtcCore.getInstance());
        }
    }

    private void doEdwardStuff(CrownUser user, BlackMarket bm){
        user.sendMessage("&eEdward &7is currently selling &e" +
                CrownUtils.capitalizeWords(bm.getDailyEnchantment().getKey().toString().replaceAll("minecraft:", "").replaceAll("_", " "))
                + " " + CrownUtils.arabicToRoman(bm.getDailyEnchantLevel()) + " &7for &6"
                + bm.getEnchantPrice(bm.getDailyEnchantment()) + " Rhines");

        ClickEventHandler.allowCommandUsage(user.getPlayer(), true);

        TextComponent text = new TextComponent(ChatColor.AQUA + "[Open Shop]");
        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ClickEventHandler.getCommand(npcID)));
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Opens Edward's inventory and allows you to apply his enchantment to one of your items")));

        TextComponent text1 = new TextComponent(ChatColor.GRAY + "Would you like to purchase it? ");
        text1.addExtra(text);

        user.getPlayer().spigot().sendMessage(text1);
    }


    private boolean sellItem(CrownUser user, Balances bals, BlackMarket bm, Material toSell){
        int sellAmount = user.getSellAmount().getInt();
        int finalSell = sellAmount;
        if(finalSell == -1) finalSell++;

        Player player = user.getPlayer();
        PlayerInventory playerInventory = player.getInventory();

        for(ItemStack stack : playerInventory){
            if(stack == null) continue;
            if(stack.getType() != toSell) continue;

            if(user.getSellAmount() == SellAmount.ALL){ //remove the itemstack and add it to the finalSell variable
                finalSell += stack.getAmount();
                sellAmount = 0;
                playerInventory.removeItem(stack);
                continue;
            }

            if(stack.getAmount() >= sellAmount){ //if the stack is larger than the remaining sellAmount
                stack.setAmount(stack.getAmount() - sellAmount);
                if(stack.getAmount() < 0) playerInventory.removeItem(stack);
                sellAmount = 0;
                break;
            }

            if(stack.getAmount() < sellAmount){ //if the stack is smaller than the remaining sellAmount
                sellAmount -= stack.getAmount(); //lessens the sellAmount so the next item requires only the amount of items still needed
                playerInventory.removeItem(stack);
            }
        }
        if(sellAmount != 0) return false; //if there's not enough items and you aren't selling all

        String s = toSell.toString().toLowerCase().replaceAll("_", " ");
        int toPay = bm.getItemPrice(toSell) * finalSell;

        bals.addBalance(user.getBase(), toPay, false);
        bm.setAmountEarned(toSell, bm.getAmountEarned(toSell) + toPay);
        user.sendMessage("&7You sold &e" + finalSell + " " + s + " &7for &6" + toPay + " Rhines");

        System.out.println(user.getName() + " sold " + finalSell + " " + s + " for " + toPay);
        return true;
    }

    @Override
    public void run(Player player, String[] args) {
        if(FtcCore.getBlackMarket().isAllowedToBuyEnchant(player)) {
            player.openInventory(FtcCore.getBlackMarket().getEnchantInventory(null, true));

            FtcCore.getInstance().getServer().getPluginManager().registerEvents(new BmSubClass1(player), FtcCore.getInstance());
        }
    }

    public class BmSubClass1 implements Listener{

        private final Player player;
        public BmSubClass1(Player p){
            player = p;
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if(!event.getPlayer().equals(player)) return;
            if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

            Player player = (Player) event.getPlayer();
            HandlerList.unregisterAll(this);

            if(event.getView().getTitle().contains("Enchants") && event.getReason() != InventoryCloseEvent.Reason.PLUGIN){
                if(event.getInventory().getItem(11) == null) return;
                ItemStack toReturn = event.getInventory().getItem(11).clone();
                try {
                    player.getInventory().addItem(toReturn);
                } catch (Exception e){
                    player.getWorld().dropItemNaturally(event.getPlayer().getLocation(), toReturn);
                }
            }
        }

        @EventHandler
        public void onBmInvUse(InventoryClickEvent event) {
            if(!event.getWhoClicked().equals(player)) return;
            if(event.getView().getTitle().contains("Black Market: Enchants")) return;
            if(event.isShiftClick()){ event.setCancelled(true); }
            if(event.getClickedInventory() instanceof PlayerInventory) return;
            else event.setCancelled(true);
            if(event.getCurrentItem() == null) return;

            event.setCancelled(true);
            Material toSell = event.getCurrentItem().getType();

            switch (toSell){
                case GRAY_STAINED_GLASS_PANE:
                case PURPLE_STAINED_GLASS_PANE:
                case END_ROD:
                case IRON_PICKAXE:
                case ROTTEN_FLESH:
                case OAK_SAPLING:
                case RED_STAINED_GLASS_PANE:
                    return;
                default:
            }

            if(event.getCurrentItem().hasItemMeta() && !event.getCurrentItem().getItemMeta().getLore().get(0).contains("Value: ")) return;

            Player player = (Player) event.getWhoClicked();
            BlackMarket bm = FtcCore.getBlackMarket();
            CrownUser user = FtcCore.getUser(event.getWhoClicked().getUniqueId());
            Balances bals = FtcCore.getBalances();

            if(event.getView().getTitle().contains("Parrot Shop")){
                String color = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).replaceAll(" Parrot", "").toLowerCase();
                List<String> pets = user.getPets();

                if(pets.contains(color + "_parrot")){
                    user.sendMessage("&7You already own this color!");
                    return;
                }

                if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("only for Admirals.")){
                    if(!user.hasRank(Rank.ADMIRAL)) throw new NoRankException(player, "You need to be an Admiral to get this parrot!");
                } else if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("only for Captains.")){
                    if(!user.hasRank(Rank.CAPTAIN)) throw new NoRankException(player, "You need to be a Captain to get this parrot!");
                } else {
                    int cost = Integer.parseInt(ChatColor.stripColor(event.getCurrentItem().getLore().get(0)).replaceAll("[\\D]", ""));

                    if(bals.getBalance(user.getBase()) < cost) throw new CannotAffordTransaction(player);
                    bals.setBalance(user.getBase(), bals.getBalance(user.getBase())  - cost);
                }

                if(pets.contains(color + "_parrot")){
                    user.sendMessage("&7You already own this color!");
                    return;
                }

                pets.add(color + "_parrot");
                user.setPets(pets);
                user.sendMessage("&7You bought a " + event.getCurrentItem().getItemMeta().getDisplayName() + "&7. Use /parrot " + color);
                return;
            }

            if(bm.getItemPrice(toSell) == null && toSell != Material.ENCHANTED_BOOK) return;
            if(!sellItem(user, bals, bm, toSell)) user.sendMessage("&7Couldn't sell items!");

            switch (event.getView().getTitle().toLowerCase().replaceAll("black market: ", "")){
                case "drops":
                    player.openInventory(bm.getDropInventory(user));
                    break;
                case "mining":
                    player.openInventory(bm.getMiningInventory(user));
                    break;
                case "crops":
                    player.openInventory(bm.getFarmingInventory(user));
                    break;
                case "enchants":
                    player.closeInventory();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + event.getView().getTitle().toLowerCase().replaceAll("black market: ", ""));
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onEnchantInvUse(InventoryClickEvent event){
            if(!event.getWhoClicked().equals(player)) return;
            if(event.getClickedInventory() instanceof PlayerInventory) return;
            if(!event.getView().getTitle().contains("Black Market: Enchants")) return;
            if(event.getSlot() != 11) event.setCancelled(true);
            if(event.getSlot() == 11 && (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.SWAP_WITH_CURSOR)) return;
            if(event.getCurrentItem() == null) return;

            Player player = (Player) event.getWhoClicked();
            Balances balances = FtcCore.getBalances();
            BlackMarket bm = FtcCore.getBlackMarket();

            ItemStack toCheck = event.getClickedInventory().getItem(11);
            if(toCheck == null) return;

            Enchantment enchantment = bm.getDailyEnchantment();
            if(!enchantment.canEnchantItem(toCheck)){
                player.openInventory(bm.getEnchantInventory(toCheck, false));
                return;
            } else player.openInventory(bm.getEnchantInventory(toCheck, true));

            if(event.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)){
                if(event.getClickedInventory().getItem(11) == null) throw new CrownException(player, "&7Place an item to enchant in the empty slot");

                if(balances.getBalance(player.getUniqueId()) < bm.getEnchantPrice(bm.getDailyEnchantment())) throw new CannotAffordTransaction(player);
                balances.addBalance(player.getUniqueId(), -bm.getEnchantPrice(bm.getDailyEnchantment()));

                ItemStack toEnchant = event.getClickedInventory().getItem(11).clone();

                ItemMeta meta = toEnchant.getItemMeta();
                meta.addEnchant(enchantment, bm.getDailyEnchantLevel(), true);
                toEnchant.setItemMeta(meta);

                try {
                    player.getInventory().addItem(toEnchant);
                } catch (Exception e){
                    player.getWorld().dropItemNaturally(player.getLocation(), toEnchant);
                }
                event.getClickedInventory().setItem(11, null);

                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1.0f, 1.0f);
                bm.setAllowedToBuyEnchant(player, false);
            }
        }
    }
}

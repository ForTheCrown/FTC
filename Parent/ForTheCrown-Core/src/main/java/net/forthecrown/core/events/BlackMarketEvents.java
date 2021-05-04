package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.*;
import net.forthecrown.core.clickevent.ClickEventHandler;
import net.forthecrown.core.clickevent.ClickEventTask;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Pet;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.exceptions.CannotAffordTransaction;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
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
    public void onBlackMarketUse(PlayerInteractEntityEvent event) throws CrownException {
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
        CrownUser user = UserManager.getUser(player.getUniqueId());

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
        user.sendMessage(
                Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(Component.text("Edward").color(NamedTextColor.YELLOW))
                        .append(Component.text(" is currently selling "))
                        .append(bm.getEnchantment().getEnchantment().displayName(bm.getEnchantment().getLevel()).color(NamedTextColor.YELLOW))
                        .append(Component.text(" for "))
                        .append(Balances.formatted(bm.getEnchantment().getPrice()).color(NamedTextColor.GOLD))
                        .build()
        );

        ClickEventHandler.allowCommandUsage(user.getPlayer(), true);

        TextComponent text = Component.text("[Open Shop]").color(NamedTextColor.AQUA)
                .clickEvent(ClickEvent.runCommand(ClickEventHandler.getCommand(npcID)))
                .hoverEvent(HoverEvent.showText(Component.text("Opens Edward's inventory and allows you to apply his enchantment to one of your items")));

        TextComponent text1 = Component.text("Would you like to purchase it? ").color(NamedTextColor.GRAY).append(text);

        user.sendMessage(text1);
    }


    private void sellItem(CrownUser user, Balances bals, BlackMarket bm, Material toSell){
        int sellAmount = user.getSellAmount().getValue();
        int finalSell = sellAmount;

        Player player = user.getPlayer();
        PlayerInventory playerInventory = player.getInventory();

        ItemStack toSellItem = new ItemStack(toSell, sellAmount);

        if(!playerInventory.contains(toSell, sellAmount)){
            user.sendMessage("&7You don't have enough items to sell");
            return;
        }

        if(user.getSellAmount() == SellAmount.ALL){
            finalSell = 0;
            for (ItemStack i: playerInventory){
                if(i == null) continue;
                if(i.getType() != toSell) continue;

                finalSell += i.getAmount();
                playerInventory.removeItemAnySlot(i);
            }
        } else playerInventory.removeItemAnySlot(toSellItem);

        String s = toSell.toString().toLowerCase().replaceAll("_", " ");
        int toPay = bm.getItemPrice(toSell) * finalSell;

        bals.add(user.getUniqueId(), toPay, false);
        bm.setAmountEarned(toSell, bm.getAmountEarned(toSell) + toPay);
        user.sendMessage("&7You sold &e" + finalSell + " " + s + " &7for &6" + toPay + " Rhines");

        System.out.println(user.getName() + " sold " + finalSell + " " + s + " for " + toPay);
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
        public void onBmInvUse(InventoryClickEvent event) throws CrownException {
            if(!event.getWhoClicked().equals(player)) return;
            if(event.getView().getTitle().contains("Black Market: Enchants")) return;
            if(event.isShiftClick()){ event.setCancelled(true); }
            if(event.getClickedInventory() instanceof PlayerInventory) return;
            else event.setCancelled(true);
            if(event.getCurrentItem() == null) return;

            event.setCancelled(true);

            if(Cooldown.contains(player)) return;
            Cooldown.add(player, 20);

            Material toSell = event.getCurrentItem().getType();

            switch (toSell){
                case GRAY_STAINED_GLASS_PANE:
                case PURPLE_STAINED_GLASS_PANE:
                case END_ROD:
                case IRON_PICKAXE:
                case OAK_SAPLING:
                case RED_STAINED_GLASS_PANE:
                    return;
                default:
            }

            if(ListUtils.isNullOrEmpty(event.getCurrentItem().getItemMeta().lore())) return;
            if(!event.getCurrentItem().getItemMeta().getLore().get(0).contains("Value: ")) return;

            Player player = (Player) event.getWhoClicked();
            BlackMarket bm = FtcCore.getBlackMarket();
            CrownUser user = UserManager.getUser(event.getWhoClicked().getUniqueId());
            Balances bals = FtcCore.getBalances();

            if(event.getView().getTitle().contains("Parrot Shop")){
                Pet pet = Pet.valueOf(
                        PlainComponentSerializer.plain().serialize(event.getCurrentItem().getItemMeta().displayName())
                                .toUpperCase().replaceAll(" ", "_")
                );
                List<Pet> pets = user.getPets();

                if(pets.contains(pet)){
                    user.sendMessage("&7You already own this color!");
                    return;
                }

                if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("only for Admirals.")){
                    if(!user.hasRank(Rank.ADMIRAL)) throw new CrownException(player, "&eYou need to be an Admiral to get this parrot!");
                } else if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("only for Captains.")){
                    if(!user.hasRank(Rank.CAPTAIN)) throw new CrownException(player, "&eYou need to be a Captain to get this parrot!");
                } else {
                    int cost = Integer.parseInt(ChatColor.stripColor(event.getCurrentItem().getLore().get(0)).replaceAll("[\\D]", ""));

                    if(bals.get(user.getUniqueId()) < cost) throw new CannotAffordTransaction(player);
                    bals.set(user.getUniqueId(), bals.get(user.getUniqueId())  - cost);
                }

                pets.add(pet);
                user.setPets(pets);
                user.sendMessage(
                        Component.text("You bought a ")
                                .color(NamedTextColor.GREEN)
                                .append(pet.getName())
                                .append(Component.text(". Use /parrot " + pet.toString().toLowerCase().replaceAll("_parrot", "")))
                );
                return;
            }

            if(bm.getItemPrice(toSell) == null && toSell != Material.ENCHANTED_BOOK) return;
            sellItem(user, bals, bm, toSell);

            String title = ChatColor.stripColor(ComponentUtils.getString(event.getView().title())).toLowerCase().replaceAll("black market: ", "");
            switch (title){
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
        public void onEnchantInvUse(InventoryClickEvent event) throws CannotAffordTransaction {
            if(!event.getWhoClicked().equals(player)) return;
            if(event.getClickedInventory() instanceof PlayerInventory) return;
            if(!event.getView().getTitle().contains("Black Market: Enchants")) return;
            if(event.getSlot() != 11) event.setCancelled(true);
            if(event.getSlot() == 11 && (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.SWAP_WITH_CURSOR)) return;
            if(event.getCurrentItem() == null) return;

            ItemStack toCheck = event.getClickedInventory().getItem(11);
            if(toCheck == null) return;

            Player player = (Player) event.getWhoClicked();
            Balances balances = FtcCore.getBalances();
            BlackMarket bm = FtcCore.getBlackMarket();

            DailyEnchantment daily = bm.getEnchantment();
            Enchantment enchantment = daily.getEnchantment();

            if(!canEnchantItem(toCheck, enchantment)){
                player.openInventory(bm.getEnchantInventory(toCheck, false));
                return;
            } else player.openInventory(bm.getEnchantInventory(toCheck, true));

            if(event.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)){
                if(balances.get(player.getUniqueId()) < daily.getPrice()) throw new CannotAffordTransaction(player);
                balances.add(player.getUniqueId(), -daily.getPrice());

                ItemStack toEnchant = event.getClickedInventory().getItem(11).clone();

                ItemMeta meta = toEnchant.getItemMeta();
                meta.addEnchant(enchantment, daily.getLevel(), true);
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

                //Send message
                player.sendMessage(
                        Component.text("You've bought ")
                                .color(NamedTextColor.GRAY)
                                .append(daily.getEnchantment().displayName(daily.getLevel()).color(NamedTextColor.GOLD))
                                .append(Component.text(" for "))
                                .append(Balances.formatted(daily.getPrice()).color(NamedTextColor.YELLOW))
                );
            }
        }

        private boolean canEnchantItem(ItemStack toEnchant, Enchantment enchantment){
            for (Enchantment e: toEnchant.getEnchantments().keySet()){
                if(e.getKey().equals(enchantment.getKey())) continue;
                if(enchantment.conflictsWith(e)) return false;
            }
            return enchantment.canEnchantItem(toEnchant);
        }
    }
}

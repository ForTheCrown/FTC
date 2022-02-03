package net.forthecrown.events;

import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.market.ShopEntrance;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class MarketListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        bookLogic(event.getRightClicked(), UserManager.getUser(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        BlockState state = event.getClickedBlock().getState();
        if(!(state instanceof PersistentDataHolder)) return;

        bookLogic(((PersistentDataHolder) state), UserManager.getUser(event.getPlayer()));
    }

    private void bookLogic(PersistentDataHolder holder, CrownUser user) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        if(!container.has(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING)) return;

        String shopName = container.get(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING);
        Markets markets = Crown.getMarkets();
        MarketShop shop = markets.get(shopName);

        openPurchaseBook(markets, shop, user);
    }

    private void openPurchaseBook(Markets markets, MarketShop shop, CrownUser user) {
        String name = "purchase_" + shop.getName();
        ClickableTextNode node = new ClickableTextNode(name)
                .setExecutor(u -> {
                    ClickableTexts.unregister(name);
                    markets.attemptPurchase(shop, u);
                })
                .setPrompt(u -> {
                    Component purchase = Component.text("[Purchase]")
                            .color(NamedTextColor.GREEN);

                    if(!u.hasBalance(shop.getPrice())) {
                        purchase = purchase.color(NamedTextColor.GRAY)
                                .hoverEvent(Component.text("Cannot afford ").append(FtcFormatter.rhinesNonTrans(shop.getPrice())));
                    }

                    if(!u.getMarketData().canChangeStatus()) {
                        purchase = purchase.color(NamedTextColor.GOLD)
                                .hoverEvent(Component.text("Cannot currently purchase shop"));
                    }

                    return purchase;
                });

        ClickableTexts.register(node);
        user.openBook(markets.getPurchaseBook(shop, user, node));
    }
}

package net.forthecrown.events;

import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.market.ShopEntrance;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class MarketListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if(!entity.getPersistentDataContainer().has(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING)) return;

        String shopName = entity.getPersistentDataContainer().get(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING);
        Markets markets = Crown.getMarkets();
        MarketShop shop = markets.get(shopName);

        openPurchaseBook(markets, shop, UserManager.getUser(event.getPlayer()));
    }

    private void openPurchaseBook(Markets markets, MarketShop shop, CrownUser user) {
        String name = "purchase_" + shop.getName();
        ClickableTextNode node = new ClickableTextNode("purchase_" + shop.getName())
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

                    return purchase;
                });

        ClickableTexts.register(node);
        user.openBook(markets.getPurchaseBook(shop, user, node));
    }
}

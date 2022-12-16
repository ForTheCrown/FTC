package net.forthecrown.events.economy;

import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.market.ShopEntrance;
import net.forthecrown.utils.text.format.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Util;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.spongepowered.math.vector.Vector3i;

public class MarketListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        bookLogic(event.getRightClicked(), Users.get(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        BlockState state = event.getClickedBlock().getState();

        if (!(state instanceof PersistentDataHolder)) {
            return;
        }

        bookLogic(((PersistentDataHolder) state), Users.get(event.getPlayer()));
    }

    private void bookLogic(PersistentDataHolder holder, User user) {
        PersistentDataContainer container = holder.getPersistentDataContainer();

        if (!container.has(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING)) {
            return;
        }

        String shopName = container.get(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING);
        MarketManager markets = Economy.get().getMarkets();
        MarketShop shop = markets.get(shopName);

        openPurchaseBook(markets, shop, user);
    }

    private void openPurchaseBook(MarketManager markets, MarketShop shop, User user) {
        String name = "purchase_" + shop.getName();
        ClickableTextNode node = new ClickableTextNode(name)
                .setExecutor(u -> {
                    ClickableTexts.unregister(name);
                    shop.attemptPurchase(u);
                })
                .setPrompt(u -> {
                    Component purchase = Component.text("[Purchase]")
                            .color(NamedTextColor.GREEN);

                    if (!u.hasBalance(shop.getPrice())) {
                        purchase = purchase.color(NamedTextColor.GRAY)
                                .hoverEvent(Component.text("Cannot afford ").append(UnitFormat.rhines(shop.getPrice())));
                    }

                    if (!Markets.canChangeStatus(u)) {
                        purchase = purchase.color(NamedTextColor.GOLD)
                                .hoverEvent(Component.text("Cannot currently purchase shop"));
                    }

                    return purchase;
                });

        ClickableTexts.register(node);
        Book.Builder builder = Book.builder()
                .title(Component.text("Purchase shop?"));

        Component newLine = Component.newline();
        Component tripleNew = Component.text("\n\n\n");

        TextComponent.Builder textBuilder = Component.text()
                .content("Purchase shop?")

                .append(tripleNew)
                .append(Component.text("Price: ").append(UnitFormat.rhines(shop.getPrice())))
                .append(tripleNew);

        int entranceAmount = shop.getEntrances().size();

        textBuilder.append(
                Component.text(entranceAmount + " entrance" + Util.conditionalPlural(entranceAmount))
        );

        Vector3i size = shop.getReset().getSize();
        String dimensions = size.x() + "x" + size.y() + "x" + size.z() + " blocks";

        textBuilder
                .append(newLine)
                .append(Component.text(dimensions));

        textBuilder
                .append(tripleNew)
                .append(newLine)
                .append(newLine);

        textBuilder.append(node.prompt(user));

        builder.addPage(textBuilder.build());

        user.openBook(builder.build());
    }
}
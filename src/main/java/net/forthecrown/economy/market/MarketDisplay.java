package net.forthecrown.economy.market;

import com.google.common.base.Joiner;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.writer.PrefixedWriter;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

/**
 * A utility class for display names and info texts for shops and markets
 */
public final class MarketDisplay {
    private MarketDisplay() {}

    /**
     * Gets a display name for a shop
     * <p>
     * Note: this shop will have the shop's info text as a hover event
     * and it's /market MARKET_NAME as the click event
     * @param shop The shop to get the display name for
     * @return The shop's display name
     */
    public static Component displayName(MarketShop shop) {
        return Component.text('[' + shop.getName() + ']')
                .hoverEvent(asHoverEvent(shop))
                .clickEvent(ClickEvent.runCommand("/market " + shop.getName()));
    }

    /**
     * Gets a display name for the shop entrance
     * @param e The entrance
     * @return The entrance's display
     */
    public static Component entranceDisplay(ShopEntrance e) {
        return Text.format(
                "sign={0}, purchasePearl={1}, direction={2}",
                e.getDoorSign(),
                e.getPurchasePearl(),
                e.getDirection().name().toLowerCase()
        );
    }

    /**
     * Gets the info text for the given shop
     * @param shop The shop to get the text of
     * @return The shop's info text
     */
    public static Component infoText(MarketShop shop) {
        return infoText(shop, true);
    }

    public static Component infoText(MarketShop shop, boolean displayMerged) {
        TextWriter writer = TextWriters.newWriter();
        writer.setFieldStyle(Style.style(NamedTextColor.YELLOW));

        writer.field("Region", shop.wgDisplayName());
        writer.field("Price", shop.getPrice());

        if (shop.hasOwner()) {
            var owner = shop.ownerUser();
            owner.unloadIfOffline();

            writer.field("Owner", owner.displayName());
            writer.field("Purchase date", Text.formatDate(shop.getPurchaseDate()));

            if (shop.markedForEviction()) {
                MarketEviction evict = shop.getEviction();

                writer.field("Eviction", "{");
                PrefixedWriter evictWriter = writer.withIndent();

                evictWriter.field("Date", Text.formatDate(evict.getEvictionTime()));
                evictWriter.field("Reason", evict.getReason());
                evictWriter.field("Source", evict.getSource());

                writer.line("}");
            }

            if (!shop.getMembers().isEmpty()) {
                writer.field("Co-Owners",
                        TextJoiner.onComma()
                                .add(
                                        shop.getMembers()
                                                .stream()
                                                .map(uuid -> {
                                                    var user = Users.get(uuid);
                                                    user.unloadIfOffline();
                                                    return user.displayName();
                                                })
                                )
                                .asComponent()
                );
            }

            if (shop.isMerged()) {
                if (displayMerged) {
                    writer.field("Merged", displayName(shop.getMerged()));
                } else {
                    writer.field("Merged", shop.getMerged().getName());
                }
            }
        }

        if (!shop.getConnected().isEmpty()) {
            writer.field("Connected", Joiner.on(", ").join(shop.getConnected()));
        }

        if (!shop.getEntrances().isEmpty()) {
            writer.field("Entrances", "{");

            PrefixedWriter eWriter = writer.withIndent();
            var it = shop.getEntrances().listIterator();

            while (it.hasNext()) {
                var next = it.next();
                var index = it.nextIndex();

                eWriter.line(index + ") ");
                eWriter.write(entranceDisplay(next));
            }

            writer.line("}");
        }

        return writer.asComponent();
    }

    /**
     * Gets the hover event for the shop
     * @param shop The shop to get the hover event for
     * @return The shop's hover event
     */
    public static HoverEvent<Component> asHoverEvent(MarketShop shop) {
        return HoverEvent.showText(infoText(shop, false));
    }
}
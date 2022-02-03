package net.forthecrown.economy.market;

import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.Iterator;
import java.util.UUID;

/**
 * A utility class for display names and info texts for shops and markets
 */
public interface MarketDisplay {

    /**
     * Gets a display name for a shop
     * <p></p>
     * Note: this shop will have the shop's info text as a hover event
     * and it's /market MARKET_NAME as the click event
     * @param shop The shop to get the display name for
     * @return The shop's display name
     */
    static Component displayName(MarketShop shop) {
        return Component.text('[' + shop.getName() + ']')
                .hoverEvent(asHoverEvent(shop))
                .clickEvent(ClickEvent.runCommand("/market " + shop.getName()));
    }

    /**
     * Gets a display name for the shop entrance
     * @param e The entrance
     * @return The entrance's display
     */
    static Component displayName(ShopEntrance e) {
        return Component.text()
                .append(Component.text("sign={" + e.doorSign.x + ", " + e.doorSign.y + ", " + e.doorSign.z + "}, "))
                .append(Component.text("notice={" + e.notice.x + ", " + e.notice.y + ", " + e.notice.z + "}, "))
                .append(Component.text("direction=" + e.direction.name().toLowerCase()))
                .build();
    }

    /**
     * Gets the info text for the given shop
     * @param shop The shop to get the text of
     * @return The shop's info text
     */
    static Component infoText(MarketShop shop) {
        return infoText(shop, true);
    }

    private static Component infoText(MarketShop shop, boolean displayMerged) {
        TextComponent.Builder builder = Component.text();

        builder
                .append(Component.text("Region: ").append(shop.wgDisplayName()))
                .append(Component.newline())
                .append(Component.text("Price: " + shop.getPrice()));

        if(shop.hasOwner()) {
            CrownUser owner = shop.ownerUser();
            owner.unloadIfOffline();

            builder
                    .append(Component.newline())
                    .append(Component.text("Owner: ").append(owner.nickDisplayName()))
                    .append(Component.newline())
                    .append(Component.text("dateOfPurchase: ").append(FtcFormatter.formatDate(shop.getDateOfPurchase())));

            if(shop.markedForEviction()) {
                builder
                        .append(Component.newline())
                        .append(Component.text("evictionDate: ").append(FtcFormatter.formatDate(shop.getEvictionDate())));
            }

            if(!shop.getCoOwners().isEmpty()) {
                TextComponent.Builder coOwnerBuilder = Component.text()
                        .append(Component.newline())
                        .append(Component.text("Co-Owners: "));

                Iterator<UUID> iterator = shop.getCoOwners().iterator();

                while (iterator.hasNext()) {
                    CrownUser coOwner = UserManager.getUser(iterator.next());

                    coOwnerBuilder.append(coOwner.nickDisplayName());

                    coOwner.unloadIfOffline();

                    if(iterator.hasNext()) coOwnerBuilder.append(Component.text(", "));
                }

                builder.append(coOwnerBuilder.build());
            }
        }

        if(shop.isMerged()) {
            builder.append(Component.newline());

            if(displayMerged) builder.append(Component.text("Merged: ").append(displayName(shop.getMerged())));
            else builder.append(Component.text("Merged: " + shop.getMerged().getName()));
        }

        if(!shop.getConnectedNames().isEmpty()) {
            TextComponent.Builder connected = Component.text()
                    .content("Connected: ");

            Iterator<String> iterator = shop.getConnectedNames().iterator();

            while (iterator.hasNext()) {
                String s = iterator.next();

                connected.append(Component.text(s));

                if(iterator.hasNext()) connected.append(Component.text(", "));
            }

            builder
                    .append(Component.newline())
                    .append(connected.build());
        }

        if(!shop.getEntrances().isEmpty()) {
            TextComponent.Builder entranceBuilder = Component.text()
                    .append(Component.newline())
                    .append(Component.text("Entrances: "))
                    .append(Component.newline());

            Iterator<ShopEntrance> iterator = shop.getEntrances().iterator();
            int index = 0;

            while (iterator.hasNext()) {
                ShopEntrance entrance = iterator.next();
                entranceBuilder
                        .append(Component.text(index +") "))
                        .append(displayName(entrance));

                index++;

                if(iterator.hasNext()) {
                    entranceBuilder
                            .append(Component.text(","))
                            .append(Component.newline());
                }
            }

            builder.append(entranceBuilder);
        }

        return builder.build();
    }

    /**
     * Gets the hover event for the shop
     * @param shop The shop to get the hover event for
     * @return The shop's hover event
     */
    static HoverEvent<Component> asHoverEvent(MarketShop shop) {
        return HoverEvent.showText(infoText(shop, false));
    }
}

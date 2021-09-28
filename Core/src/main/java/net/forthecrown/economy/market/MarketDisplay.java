package net.forthecrown.economy.market;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.Iterator;
import java.util.UUID;

public interface MarketDisplay {
    static Component displayName(MarketShop shop) {
        return Component.text('[' + shop.getName() + ']')
                .hoverEvent(asHoverEvent(shop))
                .clickEvent(ClickEvent.runCommand("/market " + shop.getName()));
    }

    static Component displayName(ShopEntrance e) {
        return Component.text()
                .append(Component.text("sign={" + e.doorSign.x + ", " + e.doorSign.y + ", " + e.doorSign.z + "}, "))
                .append(Component.text("notice={" + e.notice.x + ", " + e.notice.y + ", " + e.notice.z + "}, "))
                .append(Component.text("direction=" + e.direction.name().toLowerCase()))
                .build();
    }

    static Component infoText(MarketShop shop) {
        return infoText(shop, true);
    }

    private static Component infoText(MarketShop shop, boolean displayMerged) {
        TextComponent.Builder builder = Component.text();

        builder
                .append(Component.text("Region: " + shop.getName()))
                .append(Component.newline())
                .append(Component.text("Price: " + shop.getPrice()))
                .append(Component.newline())
                .append(Component.text("voidExample: " + shop.getVoidExample()))
                .append(Component.newline())
                .append(Component.text("resetPos: ").append(
                        FtcFormatter.clickableLocationMessage(
                                shop.getResetPos().toLoc(Crown.getMarkets().getWorld()), false)
                        )
                );

        if(shop.hasOwner()) {
            CrownUser owner = shop.ownerUser();
            owner.unloadIfOffline();

            builder
                    .append(Component.newline())
                    .append(Component.text("Owner: ").append(owner.nickDisplayName()))
                    .append(Component.newline())
                    .append(Component.text("dateOfPurchase: " + shop.getDateOfPurchase()));

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
                    .append(Component.text("Entrances: "));

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

    static HoverEvent<Component> asHoverEvent(MarketShop shop) {
        return HoverEvent.showText(infoText(shop, false));
    }
}

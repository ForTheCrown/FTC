package net.forthecrown.commands.markets;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.market.MarketEviction;
import net.forthecrown.economy.market.MarketScan;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.util.Mth;

public class CommandMarketAppeal extends FtcCommand {

    public CommandMarketAppeal() {
        super("MarketAppeal");

        setPermission(Permissions.MARKETS);
        setDescription("Appeals an automated market eviction");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /MarketAppeal
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);

                    Markets markets = Crown.getMarkets();
                    MarketShop shop = markets.get(user.getUniqueId());

                    if (shop == null) {
                        throw FtcExceptionProvider.noShopOwned();
                    }

                    if (!shop.markedForEviction()) {
                        throw FtcExceptionProvider.translatable("market.evict.notMarked");
                    }

                    if (shop.getEviction().getCause() != MarketEviction.CAUSE_AUTOMATED) {
                        throw FtcExceptionProvider.translatable("market.evict.notAutomated");
                    }

                    if (!Crown.inDebugMode() && Cooldown.containsOrAdd(c.getSource().asBukkit(), "command_" + getName(), 20 * 60 * 10)) {
                        throw FtcExceptionProvider.create("This command can only be used once every 10 minutes");
                    }

                    MarketScan scan = MarketScan.scanArea(
                            WorldBounds3i.of(
                                    markets.getWorld(),
                                    shop.getWorldGuard().getMinimumPoint(),
                                    shop.getWorldGuard().getMaximumPoint()
                            )
                    );

                    int result = scan.getResult();

                    if (result == MarketScan.RES_NOT_STOCKED) {
                        throw FtcExceptionProvider.translatable(
                                "market.evict.appeal.error",
                                Component.text(Mth.clamp(MarketScan.REQUIRED_RATIO.get(), 0, 100))
                        );
                    }

                    if (result == MarketScan.RES_NOT_ENOUGH_SHOPS) {
                        throw FtcExceptionProvider.translatable(
                                "market.evict.appeal.error.shopAmount",
                                Component.text(MarketScan.MIN_SHOP_AMOUNT.get())
                        );
                    }

                    if (result == MarketScan.RES_INACTIVE) {
                        user.sendMessage(
                                Component.translatable("market.evict.inactiveWarn", NamedTextColor.YELLOW)
                        );
                    }

                    markets.stopEviction(shop);
                    return 0;
                });
    }
}
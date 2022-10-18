package net.forthecrown.economy;

import lombok.Getter;
import net.forthecrown.core.DayChange;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.sell.SellShop;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.utils.io.PathUtil;

import java.nio.file.Files;
import java.nio.file.Path;

public class Economy {
    @Getter
    private final SellShop sellShop;

    @Getter
    private final ShopManager shops;

    @Getter
    private final MarketManager markets;

    @Getter
    private final Path directory;

    public Economy() {
        this.directory = PathUtil.getPluginDirectory("economy");

        this.sellShop = new SellShop(directory);
        this.markets = new MarketManager(directory);

        this.shops = new ShopManager();

        if (!Files.exists(sellShop.getPath())) {
            sellShop.createDefaults();
        }

        DayChange.get().addListener(markets);
    }

    public void save() {
        shops.save();
        markets.save();
    }

    public void reload() {
        shops.reload();
        markets.load();
        sellShop.load();
    }
}
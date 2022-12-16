package net.forthecrown.economy.sell;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.challenge.Challenges;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;

import java.io.IOException;
import java.nio.file.Path;

@RequiredArgsConstructor
public class SellShop {
    /** Registry of sell shop menus */
    @Getter
    private final Registry<SellShopMenu> menus = Registries.newRegistry();

    /**
     * Directory the sellshops are in, this directory
     * must include the <code>shops.json</code> file
     * from which the shop data is read
     */
    private final Path directory;

    /** Main sellshop menu */
    @Getter
    private Menu mainMenu;

    /** Global item price map */
    @Getter
    private final ItemPriceMap priceMap = new ItemPriceMap();

    public void load() {
        createDefaults();
        SerializationHelper.readJsonFile(getPath(), this::load);
    }

    private void load(JsonWrapper json) {
        this.menus.clear();
        this.priceMap.clear();

        var builder = Menus.builder(Menus.sizeFromRows(4), "FTC Shop")
                .addBorder()
                .add(4, 1, SellShopNodes.WEBSTORE);

        for (var e: json.entrySet()) {
            var name = e.getKey();
            var element = e.getValue();
            var menuJson = JsonWrapper.wrap(element.getAsJsonObject());

            var reader = new MenuReader(directory, menuJson);
            var menu = reader.read(this);

            priceMap.addAll(menu.getPriceMap());
            builder.add(reader.getSlot(), Menus.createOpenNode(menu.getInventory(), menu.getButton()));

            menus.register(name, menu);
        }

        var challengeMenu = ChallengeManager.getInstance()
                .getItemChallengeMenu();

        if (challengeMenu != null) {
            builder.add(Slot.of(4, 2),
                    Menus.createOpenNode(
                            challengeMenu,
                            Challenges.createMenuHeader()
                    )
            );
        }

        this.mainMenu = builder.build();
    }

    public Path getPath() {
        return directory.resolve("shops.json");
    }

    public void createDefaults() {
        try {
            FtcJar.saveResources("economy");
        } catch (IOException exc) {
            throw new IllegalArgumentException(exc);
        }
    }
}
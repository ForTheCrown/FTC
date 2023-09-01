package net.forthecrown.economy.market.listeners;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.economy.market.MarketEviction;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.ShopEntrance;
import net.forthecrown.text.Text;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.spongepowered.math.vector.Vector3i;

public class MarketListener implements Listener {

  private final MarketManager manager;

  public MarketListener(MarketManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onServerLoad(ServerLoadEvent event) {
    for (MarketEviction marketEviction : manager.getAwaitingExecution()) {
      marketEviction.run();
    }
    manager.getAwaitingExecution().clear();
  }

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
    MarketShop shop = manager.get(shopName);

    openPurchaseBook(shop, user);
  }

  private void openPurchaseBook(MarketShop shop, User user) {
    Book.Builder builder = Book.builder()
        .title(text("Purchase shop?"));

    Component nl = newline();
    Component nl3 = text("\n\n\n");

    TextComponent.Builder textBuilder = text()
        .content("Purchase shop?")

        .append(nl3)
        .append(text("Price: ").append(UnitFormat.rhines(shop.getPrice())))
        .append(nl3);

    int entranceAmount = shop.getEntrances().size();

    textBuilder.append(
        text(entranceAmount + " entrance" + Text.conditionalPlural(entranceAmount))
    );

    Vector3i size = shop.getReset().getSize();
    String dimensions = size.x() + "x" + size.y() + "x" + size.z() + " blocks";

    textBuilder
        .append(nl, text(dimensions))
        .append(nl3, nl, nl)
        .append(shop.purchaseButton(user));

    builder.addPage(textBuilder.build());

    user.openBook(builder.build());
  }
}

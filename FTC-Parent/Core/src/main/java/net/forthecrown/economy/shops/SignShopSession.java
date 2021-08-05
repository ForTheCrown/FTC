package net.forthecrown.economy.shops;

import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SignShopSession {
    private final Material material;
    private final SignShop shop;
    private final ShopType type;
    private final ShopInventory inventory;

    private final CrownUser user;
    private final Player player;
    private final PlayerInventory playerInventory;

    private final CrownUser owner;

    private int amount = 0;
    private Runnable onSessionExpire;

    public SignShopSession(SignShop shop, CrownUser user) {
        this.material = shop.getInventory().getExampleItem().getType();
        this.shop = shop;
        this.type = shop.getType();
        this.inventory = shop.getInventory();

        this.user = user;
        this.player = user.getPlayer();
        this.playerInventory = player.getInventory();

        this.owner = UserManager.getUser(shop.getOwner());
    }

    public CrownUser getOwner() {
        return owner;
    }

    public ShopInventory getInventory() {
        return inventory;
    }

    public ItemStack getExampleItem() {
        return getInventory().getExampleItem();
    }

    public ShopType getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public int getPrice() {
        return shop.getPrice();
    }

    public SignShop getShop() {
        return shop;
    }

    public boolean userHasSpace() {
        return getPlayerInventory().firstEmpty() != -1;
    }

    public boolean shopHasSpace() {
        return !getInventory().isFull();
    }

    public CrownUser getUser() {
        return user;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void growAmount(int amount) {
        setAmount(getAmount() + amount);
    }

    public Runnable getOnSessionExpire() {
        return onSessionExpire;
    }

    public void onSessionExpire(Runnable onSessionExpire) {
        this.onSessionExpire = onSessionExpire;
    }
}

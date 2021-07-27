package net.forthecrown.economy.shops.interactions;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.WgFlags;
import net.forthecrown.economy.Balances;
import net.forthecrown.economy.shops.ShopInventory;
import net.forthecrown.economy.shops.ShopType;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class ShopInteraction {

    protected final SignShop shop;
    protected final ShopInventory inventory;
    protected final ShopType type;

    protected final Balances bals;

    protected final CrownUser owner;
    protected final CrownUser customer;

    public ShopInteraction(SignShop shop, CrownUser customer) {
        this.shop = shop;
        this.inventory = shop.getInventory();
        this.type = shop.getType();

        this.customer = customer;
        this.owner = UserManager.getUser(shop.getOwner());
        this.bals = CrownCore.getBalances();
    }

    public void run() {
        if(!testFlags() || !checkConditions()) return;

        complete();
    }

    protected boolean testFlags() {
        Branch allowedOwner = WgFlags.query(shop.getLocation(), WgFlags.SHOP_OWNERSHIP_FLAG);
        Branch allowedUser = WgFlags.query(shop.getLocation(), WgFlags.SHOP_USAGE_FLAG);

        if(allowedOwner != null && owner.getBranch() != Branch.DEFAULT && !shop.getType().isAdmin() && allowedOwner != owner.getBranch()){
            customer.sendMessage(
                    Component.translatable("shops.wrongOwner",
                            NamedTextColor.GRAY,
                            Component.text(allowedOwner.getName())
                    )
            );
            return false;
        }

        if(allowedUser != null && customer.getBranch() != Branch.DEFAULT && allowedUser != customer.getBranch()){
            customer.sendMessage(
                    Component.translatable("shops.wrongUser",
                            NamedTextColor.GRAY,
                            Component.text(allowedUser.getName())
                    )
            );
            return false;
        }

        return true;
    }

    protected abstract boolean checkConditions();
    protected abstract void complete();

    public SignShop getShop() {
        return shop;
    }

    public ShopInventory getInventory() {
        return inventory;
    }

    public ShopType getType() {
        return type;
    }

    public Balances getBalances() {
        return bals;
    }

    public CrownUser getCustomer() {
        return customer;
    }
}

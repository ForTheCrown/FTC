package net.forthecrown.economy.shops.template;

import net.forthecrown.core.Crown;
import net.forthecrown.economy.shops.ShopInventory;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShopSession;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Range;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GenericShopTemplate implements ShopTemplate {
    private byte amount;
    private int price;
    private Material material;
    private final Key key;
    private final Range<Byte> amountRange;

    public GenericShopTemplate(byte amount, int price, Material material, Key key) {
        this.amount = amount;
        this.price = price;
        this.material = material;
        this.key = key;

        this.amountRange = Range.between((byte) 1, (byte) material.getMaxStackSize());
    }

    public Range<Byte> getAmountRange() {
        return amountRange;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(byte amount) {
        Validate.isTrue(amountRange.contains(amount));
        this.amount = amount;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public void onApply(SignShop shop) {
        shop.setPrice(price);

        ItemStack item = new ItemStack(material, amount);

        ShopInventory inv = shop.getInventory();
        inv.setExampleItem(item);
        inv.clear();
        inv.addItem(item.clone());

        shop.update();
    }

    @Override
    public void onSignUpdate(SignShop shop, Sign sign) {
        sign.line(1, Component.text(amount));
        sign.line(2, shop.getInventory().getExampleItem().displayName());
        sign.line(3, Crown.getShopManager().getPriceLine(getPrice()));
    }

    @Override
    public void onShopUse(SignShopSession session) {

    }

    @Override
    public ShopTemplateType<? extends ShopTemplate> getType() {
        return ShopTemplates.ADMIN_SHOP;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}

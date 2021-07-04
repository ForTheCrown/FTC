package net.forthecrown.cosmetics.effects.arrow;

import net.forthecrown.cosmetics.effects.CosmeticConstants;
import net.forthecrown.cosmetics.effects.CosmeticMenu;
import net.forthecrown.cosmetics.effects.MainCosmeticsMenu;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.custom.CustomInventoryBuilder;
import net.forthecrown.inventory.custom.CustomInventory;
import net.forthecrown.inventory.custom.borders.GenericBorder;
import net.forthecrown.inventory.custom.options.ClickableOption;
import net.forthecrown.inventory.custom.options.Option;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Map;

public class ArrowParticleMenu implements CosmeticMenu {

    private final CustomInventory inv;
    private final Map<Integer, Option> arrowEffectSlots;

    public ArrowParticleMenu(CrownUser user) {
        this.arrowEffectSlots = Map.of(
                10, CosmeticConstants.FLAME.getClickableOption(user),
                11, CosmeticConstants.SNOWBALL.getClickableOption(user),
                12, CosmeticConstants.SNEEZE.getClickableOption(user),
                13, CosmeticConstants.HEART.getClickableOption(user),
                14, CosmeticConstants.DAMAGE_INDICATOR.getClickableOption(user),
                15, CosmeticConstants.DRIPPING_HONEY.getClickableOption(user),
                16, CosmeticConstants.CAMPFIRE_COZY_SMOKE.getClickableOption(user),
                19, CosmeticConstants.ARROW_SOUL.getClickableOption(user),
                20, CosmeticConstants.FIREWORKS_SPARK.getClickableOption(user),
                31, CosmeticConstants.ARROW_NONE.getClickableOption(user));
        this.inv = buildInventory(user);
    }

    private Option getReturnOption(CrownUser user) {
        ClickableOption returnOption = new ClickableOption();
        returnOption.setCooldown(0);
        returnOption.setItem(CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back"));
        returnOption.setActionOnClick(() -> CosmeticMenu.open(new MainCosmeticsMenu(user), user));
        return returnOption;
    }

    @Override
    public CustomInventory buildInventory(CrownUser user) {
        CustomInventoryBuilder invBuilder = new CustomInventoryBuilder();
        return invBuilder
                .setUser(user)
                .setSize(this.getSize())
                .setTitle(this.getTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(arrowEffectSlots)
                .addOption(4, getReturnOption(user))
                .build();
    }

    @Override
    public CustomInventory getCustomInv() { return this.inv; }

    @Override
    public TextComponent getTitle() { return Component.text(" Arrow Effects"); }

    @Override
    public int getSize() { return 36; }
}

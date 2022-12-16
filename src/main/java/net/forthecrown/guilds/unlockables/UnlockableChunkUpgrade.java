package net.forthecrown.guilds.unlockables;

import lombok.Getter;
import net.forthecrown.events.PotionEffectListener;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.forthecrown.utils.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

public enum UnlockableChunkUpgrade implements Unlockable {
                            // Ah yes, raw inventory indexes, because that's
                            // not hard to understand at all
    FEATHER_FALL            (13, 2000,  getItemFrom(Material.FEATHER, "Reduced Fall Damage", "Reduce fall damage in guild chunks by 2 hearts.")),
    SPEED                   (20, 2000,  getItemFrom(new PotionData(PotionType.SPEED),"Speed", "Increase speed in guild chunks by 1 level.")),
    HASTE                   (21, 5000,  getHastePotion()),
    NO_SLIME                (22, 5000,  getItemFrom(Material.SLIME_BALL, "Deny Slime Spawning", "Stop slimes from spawning in guild chunks.")),
    MORE_MOB_EXP            (23, 10000, getItemFrom(Material.EXPERIENCE_BOTTLE, "More Mob Exp", "Mobs drop 50% more experience in guild chunks.")),
    SATURATION              (24, 10000, getItemFrom(Material.CAKE, "Saturation", "Reduce hunger in guild chunks.")),
    RESISTANCE              (29, 2000,  getResistancePotion()),
    LEAPING                 (30, 2000,  getItemFrom(new PotionData(PotionType.JUMP),"Jump Boost", "Increase jump boost in guild chunks by 1 level.")),
    ENDERPEARL_REPLENISH    (31, 5000,  getItemFrom(Material.ENDER_PEARL, "Infinite Enderpearl", "Throwing enderpearls in guild chunks won't consume them.")),
    STRONG_TOOLS            (32, 5000,  getItemFrom(Material.IRON_PICKAXE, "Tool Durability", "Reduce damage on tools in guild chunks by 50%")),
    STRONG_ARMOR            (33, 5000,  getItemFrom(Material.IRON_CHESTPLATE, "Armor Durability", "Reduce damage on armor in guild chunks by 50%")),
    STRENGTH                (38, 2000,  getItemFrom(new PotionData(PotionType.STRENGTH),"Strength", "Increase strength in guild chunks by 1 level.")),
    REGENERATION            (39, 2000,  getItemFrom(new PotionData(PotionType.REGEN),"Regeneration", "Increase regeneration in guild chunks by 1 level.")),
    VILLAGERS               (40, 5000,  getItemFrom(Material.LEAD, "Easier Villagers", "Villagers can be leashed in player chunks.")),
    KEEPINV                 (41, 10000, getItemFrom(Material.TOTEM_OF_UNDYING, "Keep Inventory", "Keep your inventory when dying in guild chunks.")),
    KEEPEXP                 (42, 10000, getItemFrom(Material.TOTEM_OF_UNDYING, "Keep Levels", "Keep your xp levels when dying in guild chunks.")),
    ;

    @Getter
    private final int slot, expRequired;
    private final ItemStack item;

    UnlockableChunkUpgrade(int slot, int expRequired, ItemStack item) {
        this.slot = slot;
        this.expRequired = expRequired;
        this.item = item;
    }

    @Override
    public GuildPermission getPerm() {
        return GuildPermission.CAN_CHANGE_EFFECT;
    }

    @Override
    public String getKey() {
        return name().toLowerCase();
    }

    @Override
    public Component getName() {
        return item.getItemMeta().displayName();
    }

    public PotionEffectType getPotionEffectType() {
        if (!(item.getItemMeta() instanceof PotionMeta meta)) {
            return null;
        } else if (meta.hasCustomEffects()) {
            return meta.getCustomEffects().get(0).getType();
        } else {
            return meta.getBasePotionData().getType().getEffectType();
        }
    }

    @Override
    public MenuNode toInvOption() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    Guild guild = context.getOrThrow(GUILD);

                    var item = this.item.clone();
                    ItemMeta meta = item.getItemMeta();
                    List<Component> lore = meta.lore();

                    if (lore == null) {
                        lore = new ArrayList<>();
                    }

                    // Upgrade is active, available or not yet unlocked
                    if (isUnlocked(guild)) {
                        // Add active or inactive line
                        lore.add(text("Effect is currently: ")
                                .color(NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                                .append(guild.hasActiveEffect(this) ?
                                        text("Active", NamedTextColor.GREEN) :
                                        text("Deactivated", NamedTextColor.GOLD)));
                    } else {
                        var style = nonItalic(NamedTextColor.GRAY);

                        // Add progress lore lines
                        lore.add(getProgressComponent(guild).style(style));
                        lore.add(Component.empty());
                        lore.add(getClickComponent().style(style));
                        lore.add(getShiftClickComponent().style(style));
                    }

                    meta.lore(lore);
                    item.setItemMeta(meta);

                    return item;
                })

                .setRunnable((user, context, c) -> onClick(user, c, context, () -> {
                    Guild guild = context.getOrThrow(GUILD);

                    // If active
                    if (guild.hasActiveEffect(this)) {
                        guild.deactivateEffect(this);

                        PotionEffectListener.removeEffectSafe(getPotionEffectType(), user.getPlayer());

                        guild.sendMessage(
                                Text.format("{0, user} has deactivated {1, item, -!amount}.",
                                        NamedTextColor.GRAY,
                                        user, item
                                )
                        );
                    }
                    // If deactive
                    else {
                        if (guild.activeEffectCount() >= Upgradable.MAX_EFFECT_AMOUNT.currentLimit(guild)) {
                            user.sendMessage(text("Your guild can not activate more effects.", NamedTextColor.GRAY));
                        } else {
                            guild.activateEffect(this);
                            PotionEffectListener.giveEffectSafe(getPotionEffectType(), user.getPlayer());

                            guild.sendMessage(
                                    Text.format("{0, user} has activated &f{1, item, -!amount}&r.",
                                            NamedTextColor.GRAY,
                                            user, item
                                    )
                            );
                        }
                    }
                }))

                .build();
    }


    // Items for chunk upgrades in InventoryMenu
    private static ItemStack getItemFrom(Material material, String name, String description) {
        return ItemStacks.builder(material)
                .setName(text(name, NamedTextColor.YELLOW))
                .addLore(text(description, NamedTextColor.GRAY))
                .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
    }

    private static ItemStack getItemFrom(PotionData data, String name, String description) {
        return ItemStacks.potionBuilder(Material.POTION)
                .setBaseEffect(data)
                .setName(text(name, NamedTextColor.YELLOW))
                .addLore(text(description, NamedTextColor.GRAY))
                .setFlags(ItemFlag.HIDE_POTION_EFFECTS)
                .build();
    }

    private static ItemStack getHastePotion() {
        return ItemStacks.potionBuilder(Material.POTION)
                .addEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 3600, 0))
                .setColor(Color.fromRGB(255, 213, 0))
                .setName(text("Haste", NamedTextColor.YELLOW))
                .addLore(text("Increase mining speed in guild chunks by 1 level.", NamedTextColor.GRAY))
                .setFlags(ItemFlag.HIDE_POTION_EFFECTS)
                .build();
    }

    private static ItemStack getResistancePotion() {
        return ItemStacks.potionBuilder(Material.POTION)
                .addEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3600, 0))
                .setColor(Color.fromRGB(50, 50, 50))
                .setName(text("Resistance", NamedTextColor.YELLOW))
                .addLore(text("Reduce damage taken in guild chunks by 1 level.", NamedTextColor.GRAY))
                .setFlags(ItemFlag.HIDE_POTION_EFFECTS)
                .build();
    }
}
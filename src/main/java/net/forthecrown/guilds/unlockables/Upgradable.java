package net.forthecrown.guilds.unlockables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.user.User;
import net.forthecrown.utils.ThrowingRunnable;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

public enum Upgradable implements Unlockable {
    MAX_EFFECT_AMOUNT(
            30,
            IntList.of(1, 2, 3, 4, 5),
            IntList.of(1_000, 5_000, 20_000, 50_000),
            Material.GLASS,
            "Max #chunk-effects",
            "The limit of effects in guild chunks.",
            Guild::activeEffectCount
    ),

    MAX_CHUNKS(
            31,
            IntList.of(16, 64, 150, 250, 500, 1000),
            IntList.of(500, 2000, 5000, 20_000, 50_000),
            Material.STONE,
            "Max #chunks",
            "The limit of guild chunks.",
            GuildManager.get()::getGuildChunkAmount
    ),

    MAX_MEMBERS(
            32,
            IntList.of(3, 5, 10, 20, 50, 100),
            IntList.of(500, 2000, 5000, 20_000, 50_000),
            Material.PLAYER_HEAD,
            "Max #members",
            "The limit of guild members.",
            Guild::getMemberSize
    ),

    GUILD_CHEST_SIZE(
            33,
            IntList.of(9, 18, 27, 36, 45, 54),
            IntList.of(500, 1500, 3000, 5000, 10_000),
            Material.CHEST,
            "Guild Chest Size",
            "The inventory size of the guild chest.",
            Guild::getGuildChestSize, Guild::refreshGuildChest
    ),
    ;

    private final int slot;
    private final IntList limitPerLvl, expRequiredPerLvl;
    private final Material material;
    private final String name, desc;
    private final Function<Guild, Integer> getCurrentAmount;
    private final Consumer<Guild> onUpgrade;

    Upgradable(int slot, IntList limitPerLvl, IntList expRequiredPerLvl, Material mat, String name, String desc, Function<Guild, Integer> f) {
        this(slot, limitPerLvl, expRequiredPerLvl, mat, name, desc, f, g -> {});
    }
    Upgradable(int slot, IntList limitPerLvl, IntList expRequiredPerLvl, Material mat, String name, String desc, Function<Guild, Integer> f, Consumer<Guild> r) {
        this.slot = slot;
        this.limitPerLvl = new IntImmutableList(limitPerLvl);
        this.expRequiredPerLvl = new IntImmutableList(expRequiredPerLvl);
        this.material = mat;
        this.name = name;
        this.desc = desc;
        this.getCurrentAmount = f;
        this.onUpgrade = r;
    }

    @Override
    public GuildPermission getPerm() {
        return null;
    }

    @Override
    public String getKey() {
        return name().toLowerCase().replace("max_", "");
    }

    @Override
    public Component getName() {
        return Component.text(name, NamedTextColor.YELLOW);
    }

    @Override
    public boolean isUnlocked(Guild guild) {
        return getExpProgress(guild) == expRequiredPerLvl.getInt(expRequiredPerLvl.size() - 1);
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public int getExpRequired() {
        return 0; // Depends on guild progress
    }

    public int getExpRequired(Guild guild) {
        int progress = getExpProgress(guild);
        for (int i = 0; i < expRequiredPerLvl.size(); i++) {
            if (progress < expRequiredPerLvl.getInt(i)) {
                return expRequiredPerLvl.getInt(i);
            }
        }
        return expRequiredPerLvl.getInt(expRequiredPerLvl.size()-1);
    }

    @Override
    public int progress(Guild guild, int amount) {
        int newProgress = getExpProgress(guild) + amount;

        // If over required, return left over
        int leftOver = newProgress - getExpRequired(guild);

        // Add progress until full
        setExpProgress(guild, Math.min(getExpRequired(guild), newProgress));

        return Math.max(leftOver, 0);
    }

    public int currentLimit(Guild guild) {
        int index = expRequiredPerLvl.indexOf(getExpRequired(guild));

        if (index == limitPerLvl.size() - 2 && getExpProgress(guild) == getExpRequired(guild)) {
            return limitPerLvl.getInt(limitPerLvl.size() - 1);
        }

        return limitPerLvl.getInt(index);
    }

    @Override
    public MenuNode toInvOption() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    var guild = context.getOrThrow(GUILD);
                    var item = ItemStacks.builder(material)
                            .setName(Component.text(name, NamedTextColor.YELLOW))
                            .addLore(Component.text(desc, NamedTextColor.GRAY)
                                             .decoration(TextDecoration.ITALIC, false))
                            .build();

                    ItemMeta meta = item.getItemMeta();
                    List<Component> lore = meta.lore();
                    lore.add(Component.text((this == GUILD_CHEST_SIZE ?
                                    "Current size: " + getCurrentAmount.apply(guild) :
                                    "Current amount: " + getCurrentAmount.apply(guild) + "/" + currentLimit(guild)))
                                     .color(NamedTextColor.GRAY)
                                     .decoration(TextDecoration.ITALIC, false));

                    if (!this.isUnlocked(guild)) {
                        lore.add(2, Component.text("Progress: " + getExpProgress(guild) + "/" + getExpRequired(guild))
                                .color(NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.empty());
                        lore.add(getClickComponent());
                        lore.add(getShiftClickComponent());
                    }
                    
                    meta.lore(lore);
                    item.setItemMeta(meta);
                    return item;
                })

                .setRunnable((u, context, c) -> onClick(u, c, context, () -> {}))
                .build();
    }


    @Override
    public void onClick(User user,
                        ClickContext context,
                        InventoryContext c,
                        ThrowingRunnable<CommandSyntaxException> r
    ) {
        Guild guild = c.get(GUILD);

        if (guild == null) {
            return;
        }

        GuildMember member = guild.getMember(user.getUniqueId());
        if (member == null) {
            return;
        }

        context.shouldReloadMenu(true);

        if (this.isUnlocked(guild)) {
            return;
        }

        int currentLimit = currentLimit(guild);
        int spentAmount;

        // Spend all available when shift clicking
        if (context.getClickType().isShiftClick()) {
            spentAmount = member.spendExp(this, member.getExpAvailable());
        }
        // Otherwise, spend EXP_COST
        else {
            spentAmount = member.spendExp(this, EXP_COST);
        }

        // Send user message
        if (spentAmount == 0) {
            user.sendMessage(
                    Component.text("You don't have any Guild Exp.", NamedTextColor.RED)
                        .append(Component.newline())
                        .append(Component.text("Complete Guild Challenges to earn some.", NamedTextColor.GRAY))
            );
        } else {
            user.sendMessage(
                    Component.text("You've spent ", NamedTextColor.GOLD)
                        .append(Component.text(spentAmount, NamedTextColor.YELLOW))
                        .append(Component.text(" Guild Exp on "))
                        .append(getName())
            );
        }

        // Check limit changed
        if (currentLimit != currentLimit(guild)) {
            guild.sendMessage(getName()
                    .append(Component.text(" has been upgraded by "))
                    .color(NamedTextColor.YELLOW)
                    .append(user.displayName())
                    .append(Component.text("!")));
            onUpgrade.accept(guild);
        }

    }
}
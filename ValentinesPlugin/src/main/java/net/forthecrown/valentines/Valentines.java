package net.forthecrown.valentines;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.npc.InteractableNPC;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Valentines extends JavaPlugin implements InteractableNPC {
    public static final Set<UUID> ALREADY_USED = new ObjectOpenHashSet<>();

    public static final Vector3i CHEST_POS = new Vector3i(267, 80, 195);

    public static NamespacedKey NPC_KEY;

    public static final ItemStack[] HAROLD_ITEMS = {
            new ItemStack(Material.RED_BED, 2),
            new ItemStack(Material.PUMPKIN_PIE, 32),
            new ItemStack(Material.ROSE_BUSH, 5),
            new ItemStack(Material.LANTERN, 2),
            new ItemStack(Material.CARROT, 10),
            new ItemStack(Material.BAKED_POTATO, 16)
    };

    @Override
    public void onEnable() {
        NPC_KEY = Keys.key("valentines", "npc");
        Registries.NPCS.register(NPC_KEY, this);

        reloadConfig();
        loadUsed();
    }

    @Override
    public void onDisable() {
        saveUsed();
        saveConfig();
    }

    private void loadUsed() {
        ALREADY_USED.clear();

        reloadConfig();
        List<String> used = getConfig().getStringList("used");
        if(ListUtils.isNullOrEmpty(used)) return;

        for (String s: used) {
            ALREADY_USED.add(UUID.fromString(s));
        }
    }

    private void saveUsed() {
        if(ALREADY_USED.isEmpty()) return;

        List<String> used = new ObjectArrayList<>();

        for (UUID u: ALREADY_USED) {
            used.add(u.toString());
        }

        getConfig().set("used", used);
    }

    boolean hasItems(Inventory inventory) {
        for (ItemStack i : HAROLD_ITEMS) {
            if (!inventory.containsAtLeast(i, i.getAmount())) return false;
        }

        return true;
    }

    Component listRequiredItems(Inventory holder) {
        TextComponent.Builder builder = Component.text()
                .content("Hey, can ya help me out? I need some help to go on a date:")
                .color(NamedTextColor.GOLD);

        for (ItemStack i : HAROLD_ITEMS) {
            TextColor color = holder.containsAtLeast(i, i.getAmount()) ? NamedTextColor.YELLOW : NamedTextColor.GRAY;

            builder
                    .append(Component.newline())
                    .append(Component.text("- ").color(NamedTextColor.DARK_GRAY))
                    .append(FtcFormatter.itemAndAmount(i).color(color));
        }

        return builder.build();
    }

    ItemStack findShulker() {
        Block b = CHEST_POS.getBlock(Worlds.OVERWORLD);

        if (!(b.getState() instanceof Chest chest)) return null;
        Inventory i = chest.getBlockInventory();

        // Find first non null item in chest
        for (ItemStack item : i) {
            if (ItemStacks.isEmpty(item)) continue;

            return item.clone();
        }

        // The chest had no items
        return null;
    }

    @Override
    public void run(Player player, Entity entity) throws CommandSyntaxException {
        if(ALREADY_USED.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("Cannot claim items twice", NamedTextColor.RED));
            return;
        }

        PlayerInventory inventory = player.getInventory();

        if (!hasItems(inventory)) {
            player.sendMessage(listRequiredItems(inventory));
            return;
        }

        // Remove items
        for (ItemStack i : HAROLD_ITEMS) {
            inventory.removeItemAnySlot(i.clone());
        }

        ItemStack item = findShulker();

        // If chest doesn't have reward item
        if (ItemStacks.isEmpty(item)) {
            getSLF4JLogger().warn("findShulker() returns an empty item, cannot give valentines day rewards");
            return;
        }

        inventory.addItem(item);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set " + CosmeticEmotes.HUG.getPerm() + " true");
        player.sendMessage(
                Component.text("Thank you for helping me, much appreciated", NamedTextColor.YELLOW)
                        .append(Component.newline())
                        .append(Component.text("You can use /hug now :D", NamedTextColor.GRAY))
        );

        ALREADY_USED.add(player.getUniqueId());

        getSLF4JLogger().info("Gave rewards to {}", player.getName());
    }
}

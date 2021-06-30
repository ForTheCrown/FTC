package net.forthecrown.pirates.grappling;

import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class GhLevelSelector implements InventoryHolder {

    public static final Style STYLE = Style.style(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);

    private final GrapplingHookParkour parkour;
    public GhLevelSelector(GrapplingHookParkour parkour){
        this.parkour = parkour;
    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54);

        inv.setItem(20, new ItemStackBuilder(Material.GREEN_WOOL).setName(
                Component.text("Confirm")
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
        ).build());

        inv.setItem(24, new ItemStackBuilder(Material.RED_WOOL).setName(
                Component.text("Deny")
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
        ).build());

        return inv;
    }

    public Inventory create(CrownUser user){
        Inventory inv = Bukkit.createInventory(this, 54, Component.text("Level selector"));

        boolean lastFinished = false;
        for (GhLevelData data: parkour.getData().values()){
            boolean completed = data.hasCompleted(user.getUniqueId());

            ItemStackBuilder builder = new ItemStackBuilder(completed ? data.getCompletedMat() : data.getSelectorMat())
                    .addLore(Component.text("Biome: ").append(data.getBiome().displayName()).style(STYLE))
                    .setName(Component.text(data.getName()).style(STYLE.color(data.getBiome().color)));

            if(data.distanceLimited()) builder.addLore(Component.text("Distance limit: " + data.getNextDistance() + " blocks.").style(STYLE));
            if(data.hookLimited()) builder.addLore(Component.text("Hook limit: " + data.getNextHooks()).style(STYLE));
            if(lastFinished && !completed) builder.addEnchant(Enchantment.CHANNELING, 1);

            lastFinished = completed;
            inv.setItem(data.getIndex(), builder.build());
        }

        return inv;
    }
}

package net.forthecrown.pirates;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class BaseEgg implements Listener {

    //TODO invert if statements
    @EventHandler
    public void onEggUse(BlockPlaceEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.isCancelled()) return;


        if (event.getBlockPlaced().getType() == Material.TURTLE_EGG) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(Pirates.inst, () -> {
                if (event.getBlockPlaced().getLocation().getBlock().getType() == Material.TURTLE_EGG) {
                    ItemMeta meta = event.getItemInHand().getItemMeta();

                    if (meta.hasLore()) {
                        event.getBlockPlaced().getLocation().getBlock().setType(Material.AIR);
                        spawnCorrectEntity(event.getPlayer(), event.getBlock().getLocation().add(0.5, 1, 0.5), meta.getLore().get(0), meta.getDisplayName());
                        event.getItemInHand().setAmount(event.getItemInHand().getAmount() - 1);
                    }
                }
            }, 2L);

        }
    }

    private void spawnCorrectEntity(Player player, Location spawnLocation, String loreline, String displayName) {
        if (loreline == null || loreline.equals("") || player == null) return;

        if (loreline.contains("Bartender")) {
            Villager bartender = player.getWorld().spawn(spawnLocation, Villager.class);
            bartender.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            bartender.setVillagerLevel(5);
            bartender.setVillagerType(Villager.Type.PLAINS);
            bartender.setProfession(Villager.Profession.BUTCHER);

            if (displayName == null || displayName.equals("")) bartender.setCustomName("Bartender");
            else bartender.setCustomName(displayName);


            List<MerchantRecipe> trades = new ArrayList<>();

            ItemStack toxicSewageTea = new ItemStack(Material.POTION, 1);
            PotionMeta toxicSewageTeaMeta = (PotionMeta) toxicSewageTea.getItemMeta();
            toxicSewageTeaMeta.setColor(org.bukkit.Color.fromRGB(6583123));
            toxicSewageTeaMeta.addCustomEffect(new PotionEffect(PotionEffectType.CONFUSION, 300, 0), true);
            toxicSewageTeaMeta.displayName(Component.text("Toxic Sewage Tea"));
            toxicSewageTea.setItemMeta(toxicSewageTeaMeta);

            MerchantRecipe recipe0 = new MerchantRecipe(toxicSewageTea, 0, Integer.MAX_VALUE, false);
            recipe0.addIngredient(new ItemStack(Material.GOLD_INGOT));


            ItemStack sailorSweat = new ItemStack(Material.POTION, 1);
            PotionMeta sailorSweatMeta = (PotionMeta) sailorSweat.getItemMeta();
            sailorSweatMeta.setColor(org.bukkit.Color.fromRGB(3682594));
            sailorSweatMeta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 0, 0), true);
            sailorSweatMeta.addCustomEffect(new PotionEffect(PotionEffectType.CONFUSION, 300, 0), true);
            sailorSweatMeta.displayName(Component.text("Sailor Sweat"));
            sailorSweat.setItemMeta(sailorSweatMeta);

            MerchantRecipe recipe1 = new MerchantRecipe(sailorSweat, 0, Integer.MAX_VALUE, false);
            recipe1.addIngredient(new ItemStack(Material.GOLD_INGOT, 2));


            ItemStack puddleJuice = new ItemStack(Material.POTION, 1);
            PotionMeta puddleJuiceMeta = (PotionMeta) puddleJuice.getItemMeta();
            puddleJuiceMeta.setColor(org.bukkit.Color.fromRGB(16776965));
            puddleJuiceMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2), true);
            puddleJuiceMeta.displayName(Component.text("Puddle Juice"));
            puddleJuice.setItemMeta(puddleJuiceMeta);

            MerchantRecipe recipe2 = new MerchantRecipe(puddleJuice, 0, Integer.MAX_VALUE, false);
            recipe2.addIngredient(new ItemStack(Material.GOLD_INGOT));


            trades.add(recipe0);
            trades.add(recipe1);
            trades.add(recipe2);
            bartender.setRecipes(trades);
        }

        else if (loreline.contains("Mending Villager")) {
            Villager mender = player.getWorld().spawn(spawnLocation, Villager.class);
            mender.setVillagerLevel(2);
            mender.setProfession(Villager.Profession.LIBRARIAN);

            if (displayName != null && !displayName.equals("")) mender.setCustomName(displayName);

            List<MerchantRecipe> trades = new ArrayList<>();

            MerchantRecipe recipe0 = new MerchantRecipe(new ItemStack(Material.EMERALD), 0, 15, false, 5, 0.05f);
            recipe0.addIngredient(new ItemStack(Material.PAPER, 25));

            ItemStack mendingBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
            EnchantmentStorageMeta mendingBookmeta = (EnchantmentStorageMeta) mendingBook.getItemMeta();
            mendingBookmeta.addStoredEnchant(Enchantment.MENDING, 1, true);
            mendingBook.setItemMeta(mendingBookmeta);
            MerchantRecipe recipe1 = new MerchantRecipe(mendingBook, 0, Integer.MAX_VALUE, false, 5, 0.05f);
            recipe1.addIngredient(new ItemStack(Material.EMERALD, 7));
            recipe1.addIngredient(new ItemStack(Material.BOOK, 1));

            trades.add(recipe0);
            trades.add(recipe1);
            mender.setRecipes(trades);
        }

        else if (loreline.contains("Charged Creeper")) {
            Creeper creeper = player.getWorld().spawn(spawnLocation, Creeper.class);
            creeper.setPowered(true);
        }
        else if (loreline.contains("White Fox")) {
            Fox whiteFox = player.getWorld().spawn(spawnLocation, Fox.class);
            whiteFox.setFoxType(Fox.Type.SNOW); }
        else if (loreline.contains("Passive Pillager")) {
            Pillager passivePillager = player.getWorld().spawn(spawnLocation, Pillager.class);
            passivePillager.getEquipment().setItemInMainHand(null);
        }
        else if (loreline.contains("Skelly Boy")) {
            Skeleton skellyboy = player.getWorld().spawn(spawnLocation, Skeleton.class);
            skellyboy.getEquipment().setItemInMainHand(null);
        }
        else {
            try {
                player.getWorld().spawnEntity(spawnLocation, EntityType.valueOf(loreline.replace(" ", "_").toUpperCase()));
            } catch (Exception e) {
                Bukkit.broadcastMessage("rip");
            }
        }
    }
}

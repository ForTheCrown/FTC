package net.forthecrown.cosmetics;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.commands.CommandCosmetics;
import net.forthecrown.cosmetics.custominvs.CustomInv;
import net.forthecrown.cosmetics.effects.arrow.ArrowParticleMenu;
import net.forthecrown.cosmetics.effects.death.effects.CosmeticDeathEffect;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Cosmetics extends JavaPlugin {

    public static StateFlag PLAYER_RIDING_ALLOWED;
    private static PlayerRidingManager rideManager;

    private static Cosmetics plugin = null;
    public static synchronized Cosmetics getPlugin() {
        if (plugin == null) plugin = new Cosmetics();
        return plugin;
    }
    private Cosmetics() {}

    public void onEnable() {
        // Config
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        rideManager = new PlayerRidingManager();

        // Events
        getServer().getPluginManager().registerEvents(new CosmeticEvents(), this);
        getServer().getPluginManager().registerEvents(new ArrowParticleMenu(), this);
        getServer().getPluginManager().registerEvents(CosmeticDeathEffect.listener, this);
        getServer().getPluginManager().registerEvents(CustomInv.listener, this);

        // Command
        new CommandCosmetics();
    }

    @Override
    public void onDisable() {
        getRideManager().stopAllRiding();
    }

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("player-riding", true);
            registry.register(flag);
            PLAYER_RIDING_ALLOWED = flag;
        } catch (FlagConflictException e){
            e.printStackTrace();
        }
    }

    public static PlayerRidingManager getRideManager() {
        return rideManager;
    }

    public Inventory getMainCosmeticInventory(CrownUser user) {
        CustomInventory cinv = new CustomInventory(54, ChatColor.BOLD + "C" + ChatColor.RESET + "osmetics", true, false);
        Inventory inv = cinv.getInventory();

        inv.setItem(20, CrownItems.makeItem(Material.BOW, 1, true, ChatColor.YELLOW + "Arrow Particle Trails", "", ChatColor.GRAY + "Upgrade your arrows with fancy particle", ChatColor.GRAY + "trails as they fly through the air!"));
        inv.setItem(22, CrownItems.makeItem(Material.TOTEM_OF_UNDYING, 1, true, ChatColor.YELLOW + "Emotes", "", ChatColor.GRAY + "Poking, smooching, bonking and more", ChatColor.GRAY + "to interact with your friends."));
        inv.setItem(24, CrownItems.makeItem(Material.SKELETON_SKULL, 1, true, ChatColor.YELLOW + "Death Particles", "", ChatColor.GRAY + "Make your deaths more spectacular by", ChatColor.GRAY + "exploding into pretty particles!"));

        if (user.allowsRidingPlayers()) {
            inv.setItem(40, CrownItems.makeItem(Material.SADDLE, 1, true,ChatColor.YELLOW + "You can ride other players!", "",
                    ChatColor.GRAY + "Right-click someone to jump on top of them.",
                    ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
                    ChatColor.GRAY + "Click to disable this feature."));
        }
        else {
            inv.setItem(40, CrownItems.makeItem(Material.BARRIER, 1, true, ChatColor.YELLOW + "You've disabled riding other players.", "",
                    ChatColor.GRAY + "Right-click someone to jump on top of them.",
                    ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
                    ChatColor.GRAY + "Click to enable this feature."));
        }

        int gems = user.getGems();
        try {
            ItemStack item = inv.getItem(cinv.getHeadItemSlot());
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.set(1, ChatColor.GRAY + "You have " + ChatColor.GOLD + gems + ChatColor.GRAY + " Gems.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(cinv.getHeadItemSlot(), item);
        } catch (Exception ignored) {}

        return inv;
    }


    /*public static final Set<Particle> ACCEPTED_ARROW_PARTICLES = new HashSet<>(Arrays.asList(
            Particle.FLAME, Particle.SNOWBALL, Particle.SNEEZE,
            Particle.HEART, Particle.DAMAGE_INDICATOR, Particle.DRIPPING_HONEY,
            Particle.CAMPFIRE_COSY_SMOKE, Particle.SOUL, Particle.FIREWORKS_SPARK));

    public static final Set<String> ACCEPTED_DEATH_PARTICLES = new HashSet<>(
            Arrays.asList("SOUL", "TOTEM", "EXPLOSION", "ENDER_RING"));*/
}

package net.forthecrown.pirates;

import net.forthecrown.emperor.Announcer;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.inventory.CrownItems;
import net.forthecrown.emperor.inventory.CustomInventoryHolder;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.utils.Cooldown;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class GrapplingHook implements Listener {

    private final Pirates main;

    private final FileConfiguration armorStandsFile;
    private FileConfiguration playerProgress;

    public static final List<String> LEVEL_NAMES = Arrays.asList("Journey", "Journey Limited", "Ship", "Ship Limited", "Sky Battle", "Sky Battle Limited", "Floating Islands", "Floating Islands Limited", "Floating Islands Limited Distance", "Big Beans", "Big Beans Limited", "Floating Ruins", "Floating Ruins Temple", "Floating Ruins Temple Limited", "Annoying Islands",
            "Bunk Ships", "Bunk Ships Limited", "Shark Attack", "Sharks Failed", "Watchtower", "Watchtower Limited", "More Towers", "More Towers Limited", "Not Enough Towers", "Not Enough Towers Limited", "The Climb", "The Limited Climb", "Weird Object",
            "Tetrominoes Limited", "Tetris", "Tetris Limited", "Tetris Cannons", "Nightmare", "Angry Tetromino", "Parkour A", "Parkour B", "Temple of the Void");

    private static final List<Material> COMPLETED_LEVEL_INDICATORS = Arrays.asList(
            Material.GREEN_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.PURPLE_TERRACOTTA, Material.TERRACOTTA, Material.BLACK_TERRACOTTA);

    public GrapplingHook(Pirates main){
        this.main = main;

        main.getServer().getPluginManager().registerEvents(this, main);

        try {
            playerProgress = YamlConfiguration.loadConfiguration(getPlayerLevelsFile());
            convertLegacyToDataContainer();
        } catch (Exception ignored){ }
        armorStandsFile = YamlConfiguration.loadConfiguration(getArmorStandFile());
    }

    private void convertLegacyToDataContainer(){
        Set<String> keys = playerProgress.getKeys(false);
        if(keys.size() < 1) return;

        for (String s: keys){
            UUID id = UUID.fromString(s);
            CrownUser user = UserManager.getUser(id);

            ConfigurationSection section = user.getDataContainer().get(Pirates.inst);
            section.set("CompletedLevels", playerProgress.getStringList(s));
            user.getDataContainer().set(Pirates.inst, section);

            if(!user.isOnline()) user.unload();
            else user.save();
        }
        getPlayerLevelsFile().delete();
        //BOTUL YOU ABSOLUTE RETARD
        //... that delete() statement was in the loop, it would delete the file after 1 loop :(
    }

    public File getPlayerLevelsFile() {
        File file = new File(main.getDataFolder(), "PlayerLevelProgress.yml");
        if (!file.exists()) return null;
        return file;
    }

    public List<String> getUserLevels(CrownUser user){
        List<String> toReturn = user.getDataContainer().get(Pirates.inst).getStringList("CompletedLevels");
        return toReturn;
    }

    public void setUserLevels(CrownUser user, List<String> list){
        ConfigurationSection dataSec = user.getDataContainer().get(Pirates.inst);
        dataSec.set("CompletedLevels", list);
        user.getDataContainer().set(Pirates.inst, dataSec);
    }

    public File getArmorStandFile() {
        File file = new File(main.getDataFolder(), "TargetStandData.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    @EventHandler
    public void onPlayerArmorStandEvent(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        CrownUser user = UserManager.getUser(player);

        if (event.getRightClicked().getType() == EntityType.ARMOR_STAND
                && event.getRightClicked().isInvulnerable()
                && event.getRightClicked().getCustomName() != null
                && event.getRightClicked().getCustomName().contains("GHTargetStand")) {

            if(Cooldown.contains(user)) return;
            Cooldown.add(user, 50);

            final String ghArmorStandID = "Stand_" + event.getRightClicked().getName().split(" ")[1];

            //YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getArmorStandFile());
            Location loc = new Location(player.getWorld(), armorStandsFile.getDouble(ghArmorStandID + ".XToCords"), armorStandsFile.getDouble(ghArmorStandID + ".YToCords"), armorStandsFile.getDouble(ghArmorStandID + ".ZToCords"), armorStandsFile.getInt(ghArmorStandID + ".YawToCords"), 0);

            player.sendMessage(ChatColor.GRAY + "Stand on the glowstone for 2 seconds.");

            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                if (player.getLocation().clone().subtract(0, 1, 0).getBlock().getType() == Material.GLOWSTONE) {

                    player.getInventory().clear();
                    player.teleport(loc);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP , 1.0F, 2.0F);
                    player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.GRAY + "You've advanced to the next level!");

                    if (armorStandsFile.getInt(ghArmorStandID + ".StandClass") != 3) Bukkit.dispatchCommand(main.getServer().getConsoleSender(), "grapplinghook give " + player.getName() + " " + armorStandsFile.getInt(ghArmorStandID + ".NextLevelHooks") + " " + armorStandsFile.getInt(ghArmorStandID + ".NextLevelDistance"));

                    String name = event.getRightClicked().getName().replaceAll("[\\D]", "");

                    int level;
                    try {
                        level = Integer.parseInt(name);
                    }
                    catch (Exception e) {
                        Announcer.log(Level.WARNING, ChatColor.RED + "Wrong target-armorstand found: " + name + " as id is not valid.");
                        return;
                    }


                    List<String> levelList = LEVEL_NAMES;

                    if (!getUserLevels(user).contains(levelList.get(level))) {
                        switch (armorStandsFile.getInt(ghArmorStandID + ".StandClass")) {
                            case 2:
                                player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.GRAY + "You have recieved " + ChatColor.GOLD  + "25,000 Rhines " + ChatColor.GRAY + "for completing all levels in a biome.");
                                CrownCore.getBalances().add(player.getUniqueId(), 25000, false);
                                main.givePP(player, 5);
                                break;
                            case 3:
                                player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.GRAY + "You have recieved " + ChatColor.GOLD + "50,000 Rhines and the Captain's Cutlass " + ChatColor.GRAY + "for completing all the Grappling Hook levels!");
                                CrownCore.getBalances().add(player.getUniqueId(), 50000, false);

                                Bukkit.getScheduler().scheduleSyncDelayedTask(CrownCore.inst(), () -> player.getInventory().addItem(CrownItems.BASE_CUTLASS.clone()), 5);
                                main.givePP(player, 25);
                                break;
                            default:
                                break;

                        }
                    }


                    List<String> list = new ArrayList<>();
                    if (getUserLevels(user).isEmpty()) {
                        list.add("started");
                        list.add(levelList.get(level));
                    }
                    else {
                        list = getUserLevels(user);
                        if (!list.contains(levelList.get(level))) list.add(levelList.get(level));
                    }
                    setUserLevels(user, list);
                    getUserLevels(user);
                } else {
                    player.sendMessage(ChatColor.GRAY + "Cancelled.");
                }
            }, 40L);
        }
    }

    @EventHandler
    public void equipping(PlayerArmorStandManipulateEvent event) {
        if (event.getRightClicked().isInvulnerable() && event.getRightClicked().getCustomName() != null && event.getRightClicked().getCustomName().contains("GHTargetStand")) {
            event.setCancelled(true);
        }
    }


    void openLevelSelector(Player player) {
        Inventory inv = createLevelSelectorInv();
        inv = personalizeInventory(player, inv);
        player.openInventory(inv);

        Pirates.inst.getServer().getPluginManager().registerEvents(new GhSubClass(player), Pirates.inst);
    }



    Inventory createLevelSelectorInv() {
        Inventory result = Bukkit.createInventory(new CustomInventoryHolder(), 54, Component.text("Level Selector"));

        List<String> levelList = LEVEL_NAMES;

        ItemStack item;
        ItemMeta meta;
        for (int i = 0; i < result.getSize(); i++) {
            if (i < 15) {
                item = new ItemStack(Material.GRASS_BLOCK);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + levelList.get(i) + ChatColor.RESET);
                item.setItemMeta(meta);
                result.setItem(i, item);
            }
            else if (i >= 15 && i < 27) {
                item = new ItemStack(Material.RED_SANDSTONE);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + levelList.get(i) + ChatColor.RESET);
                item.setItemMeta(meta);
                result.setItem(i, item);
            }
            else if (i >= 27 && i < 34) {
                item = new ItemStack(Material.PURPLE_STAINED_GLASS);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + levelList.get(i) + ChatColor.RESET);
                item.setItemMeta(meta);
                result.setItem(i, item);
            }
            else if (i == 34 || i == 35) {
                item = new ItemStack(Material.OAK_PLANKS);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + levelList.get(i) + ChatColor.RESET);
                item.setItemMeta(meta);
                result.setItem(i, item);
            }
            else if (i == 40) {
                item = new ItemStack(Material.GILDED_BLACKSTONE);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "Temple of the Void");
                item.setItemMeta(meta);
                result.setItem(i, item);
            }
            else if (i > 40) break;
        }
        return result;
    }

    private Inventory personalizeInventory(Player player, Inventory inv) {
        CrownUser user = UserManager.getUser(player);
        List<String> completedLevels = getUserLevels(user);

        if (completedLevels.isEmpty()) {
            List<String> list = new ArrayList<>();
            list.add("started");
            setUserLevels(user, list);
        }
        else {
            ItemStack item;
            for (String completedLevel : completedLevels) {
                if (completedLevel.contains("started")) continue;

                item = getItemWithNameFrom(inv, completedLevel);
                if (item == null) continue;

                switch (item.getType()){
                    case RED_SANDSTONE:
                        item.setType(Material.ORANGE_TERRACOTTA);
                        break;
                    case PURPLE_STAINED_GLASS:
                        item.setType(Material.PURPLE_TERRACOTTA);
                        break;
                    case OAK_PLANKS:
                        item.setType(Material.TERRACOTTA);
                        break;
                    case GILDED_BLACKSTONE:
                        item.setType(Material.BLACK_TERRACOTTA);
                        break;
                    default:
                        item.setType(Material.GREEN_TERRACOTTA);
                }
                //inv.setItem(i, item);
            }
        }

        for (int i = 0; i < 41; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR && (!COMPLETED_LEVEL_INDICATORS.contains(item.getType()))) {
                item.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
                break;
            }
        }
        return inv;
    }

    private ItemStack getItemWithNameFrom(Inventory inv, String name) {
        for (ItemStack item : inv.getContents()) {
            if(item == null) continue;
            if (item.getType() != Material.AIR && item.getItemMeta().getDisplayName().contains(name))
                return item;
        }
        return null;
    }

    public class GhSubClass implements Listener {

        private final Player p;
        private GhSubClass(Player p){
            this.p = p;
        }

        @EventHandler
        public void onPlayerClickItemInInv(InventoryClickEvent event) {
            if(!event.getWhoClicked().equals(p)) return;
            if(event.isShiftClick()) event.setCancelled(true);
            if(event.getClickedInventory() instanceof PlayerInventory) return;
            event.setCancelled(true);

            if(Cooldown.contains(event.getWhoClicked())) return;
            Cooldown.add(event.getWhoClicked(), 6);
            Player player = (Player) event.getWhoClicked();

            if (!player.getWorld().getName().contains("world_void")) return; // extra check

            if (!player.getInventory().isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "Your inventory has to be completely empty to enter.");
                event.setCancelled(true);
                return;
            }

            if (event.getInventory().getItem(event.getSlot()) == null) return;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

            if (COMPLETED_LEVEL_INDICATORS.contains(event.getInventory().getItem(event.getSlot()).getType()) || event.getInventory().getItem(event.getSlot()).getEnchantments().containsKey(Enchantment.CHANNELING)) {

                int slot = event.getSlot() - 1;
                Location loc;

                if (slot == 39) slot = 35;
                if (slot == -1) {
                    loc = new Location(player.getWorld(), -1003.5, 21, 3.5, 180, 0); // Level 1 start
                    Bukkit.dispatchCommand(main.getServer().getConsoleSender(), "grapplinghook give " + player.getName());
                }
                else {
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getArmorStandFile());
                    String ghArmorStandID = "Stand_" + slot;
                    loc = new Location(player.getWorld(), yaml.getDouble(ghArmorStandID + ".XToCords"), yaml.getDouble(ghArmorStandID + ".YToCords"), yaml.getDouble(ghArmorStandID + ".ZToCords"), yaml.getInt(ghArmorStandID + ".YawToCords"), 0);
                    Bukkit.dispatchCommand(main.getServer().getConsoleSender(), "grapplinghook give " + player.getName() + " " + yaml.getInt(ghArmorStandID + ".NextLevelHooks") + " " + yaml.getInt(ghArmorStandID + ".NextLevelDistance"));
                }

                player.teleport(loc);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT , 1.0F, 1.0F);
                player.sendMessage(ChatColor.GRAY + "You can " + ChatColor.YELLOW + "/leave" + ChatColor.GRAY + " at any time.");
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClose(InventoryCloseEvent event) {
            if(!event.getPlayer().equals(p)) return;
            if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

            HandlerList.unregisterAll(this);
        }
    }
}

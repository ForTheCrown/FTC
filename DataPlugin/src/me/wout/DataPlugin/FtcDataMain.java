package me.wout.DataPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.wout.DataPlugin.commands.AddPet;
import me.wout.DataPlugin.commands.MakeBaron;
import me.wout.DataPlugin.commands.MakeKing;
import me.wout.DataPlugin.commands.RemoveKing;
import me.wout.DataPlugin.commands.addrank;
import me.wout.DataPlugin.commands.canswapbranch;
import me.wout.DataPlugin.commands.getbranch;
import me.wout.DataPlugin.commands.listrank;
import me.wout.DataPlugin.commands.rank;
import me.wout.DataPlugin.commands.removerank;
import me.wout.DataPlugin.commands.setbranch;
import me.wout.DataPlugin.commands.setswapbranch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;


public class FtcDataMain extends JavaPlugin implements Listener {

    //public File wilhelmFile;
    //public YamlConfiguration wilhelmYaml;
    //public List<String> players = new ArrayList<String>();
    //public List<ItemStack> itemsToGet = new ArrayList<ItemStack>();

    rank r;

    public List<FtcUserData> loadedPlayerDatas;
    public static FtcDataMain plugin;

    public void onEnable() {
        loadedPlayerDatas = new ArrayList<>();
        plugin = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        new addrank(this);
        new removerank(this);
        new listrank(this);
        new canswapbranch(this);
        new setswapbranch(this);
        new getbranch(this);
        new setbranch(this);
        new MakeBaron(this);
        new MakeKing(this);
        new RemoveKing(this);
        new AddPet(this);

        r = new rank(this);
    }

    public void onDisable() {

    }


    public FtcUserData getUserData(Player base){
        for (FtcUserData data : loadedPlayerDatas){
            if(data.getPlayerLink() == base.getUniqueId()) return data;
        }
        return new FtcUserData(base);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();

        if (getConfig().getConfigurationSection("players").getKeys(false) == null || (!getConfig().getConfigurationSection("players").getKeys(false).contains(uuid)))
        {
            createPlayerSection(uuid, event.getPlayer().getName());
        }
        else if (!getConfig().getString("players." + uuid + ".PlayerName").equalsIgnoreCase(event.getPlayer().getName()))
        {
            getConfig().set("players." + uuid + ".PlayerName", event.getPlayer().getName());
        }
    }

    public void createPlayerSection(String playerUuid, String playerName) {
        getConfig().createSection("players." + playerUuid);

        getConfig().createSection("players." + playerUuid + ".PlayerName");
        getConfig().set("players." + playerUuid + ".PlayerName", playerName);

        getConfig().createSection("players." + playerUuid + ".KnightRanks");
        getConfig().set("players." + playerUuid + ".KnightRanks", new ArrayList<String>());

        getConfig().createSection("players." + playerUuid + ".PirateRanks");
        getConfig().set("players." + playerUuid + ".PirateRanks", new ArrayList<String>());

        getConfig().createSection("players." + playerUuid + ".CurrentRank");
        getConfig().set("players." + playerUuid + ".CurrentRank", "default");

        getConfig().createSection("players." + playerUuid + ".CanSwapBranch");
        getConfig().set("players." + playerUuid + ".CanSwapBranch", true);

        getConfig().createSection("players." + playerUuid + ".ActiveBranch");
        getConfig().set("players." + playerUuid + ".ActiveBranch", "Knight");

        getConfig().createSection("players." + playerUuid + ".Pets");
        getConfig().set("players." + playerUuid + ".Pets", new ArrayList<String>());

        getConfig().createSection("players." + playerUuid + ".ParticleArrowActive");
        getConfig().set("players." + playerUuid + ".ParticleArrowActive", "none");

        getConfig().createSection("players." + playerUuid + ".ParticleArrowAvailable");
        getConfig().set("players." + playerUuid + ".ParticleArrowAvailable", new ArrayList<String>());

        saveConfig();
    }

    @SuppressWarnings("deprecation")
    public String trySettingUUID(String givenPlayer) {
        String result;
        try {
            result = Bukkit.getPlayer(givenPlayer).getUniqueId().toString();
        } catch (Exception ignored) {
            try {
                result = Bukkit.getOfflinePlayer(givenPlayer).getUniqueId().toString();
            }
            catch (Exception ignored2) {
                return null;
            }
        }
        return result;
    }



    // ************************************ /Rank inventory functions ************************************ //

    Set<String> onCooldown = new HashSet<String>();

    @EventHandler
    public void onPlayerClickItemInInv(InventoryClickEvent event)
    {
        String title = event.getView().getTitle().replace("s", "");

        if (getPossibleBranches().contains(title))
        {
            event.setCancelled(true);

            if (event.getClickedInventory() instanceof PlayerInventory) return;
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE || event.getSlot() == 4) return;

            Player player = (Player) event.getWhoClicked();
            String playeruuid = player.getUniqueId().toString();

            if (onCooldown.contains(playeruuid)) return;
            onCooldown.add(playeruuid);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    onCooldown.remove(playeruuid);
                }
            }, 5L);


            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

            // Next page clicked
            if (event.getSlot() == 8)
            {
                List<String> possibleBranches = getPossibleBranches();
                int nextInv = possibleBranches.indexOf(title) + 1;

                String nextBranch = possibleBranches.get(nextInv % possibleBranches.size());
                Boolean isActive = (nextBranch.contains(getActiveBranch(playeruuid)));

                player.openInventory(r.makeRankInventory(playeruuid, nextBranch, isActive));
                return;
            }

            // Other clicked
            else
            {
                if (!title.contains(getActiveBranch(playeruuid))) return; // Clicked in non-active-branch inventory
                if (clickedItem.containsEnchantment(Enchantment.CHANNELING)) return; // Clicked the current rank item

                ItemMeta meta = clickedItem.getItemMeta();
                if (!meta.hasLore()) return;
                List<String> lore = meta.getLore();

                for (String loreline : lore)
                {
                    if (loreline.contains("Click to make this your active rank.")) // if item available
                    {
                        resetPreviousCurrentRankItem(event.getClickedInventory());
                        makeCurrentItem(clickedItem);
                        String newRank = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                        getConfig().set("players." + player.getUniqueId().toString() + ".CurrentRank", newRank);
                        saveConfig();
                        Bukkit.dispatchCommand(getServer().getConsoleSender(), "tab player " + player.getName() + " tabprefix " + getCorrectPrefix(newRank));
                        player.sendMessage(ChatColor.GRAY + "You rank has been updated.");
                        return;
                    }
                }
                player.sendMessage(ChatColor.GRAY + "[FTC] You don't have this rank at the moment.");
                return;
            }


        }
    }


    private void resetPreviousCurrentRankItem(Inventory clickedInventory) {
        for (ItemStack item : clickedInventory.getContents())
        {
            if (item == null) continue;
            if (item.containsEnchantment(Enchantment.CHANNELING))
            {
                item.removeEnchantment(Enchantment.CHANNELING);
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                lore.add(net.md_5.bungee.api.ChatColor.of("#73715b") +  "Click to make this your active rank.");
                meta.setLore(lore);
                item.setItemMeta(meta);
                return;
            }
        }

    }

    private void makeCurrentItem(ItemStack clickedItem)
    {
        clickedItem.addUnsafeEnchantment(Enchantment.CHANNELING, 1);

        ItemMeta meta = clickedItem.getItemMeta();
        List<String> lore = meta.getLore();
        lore.remove(lore.size()-1);
        meta.setLore(lore);
        clickedItem.setItemMeta(meta);
    }

    public Map<String, Integer> getSlotsOfRankItems()
    {
        Map<String, Integer> result = new HashMap<String, Integer>();

        // Knights
        result.put("default", 10);
        result.put("knight", 12);
        result.put("baron", 13);
        result.put("baroness", 14);

        result.put("lord", 29);
        result.put("lady", 38);

        result.put("duke", 31);
        result.put("duchess", 40);

        result.put("prince", 33);
        result.put("princess", 42);

        // Pirates
        result.put("sailor", 21);
        result.put("pirate", 23);

        result.put("captain", 39);
        result.put("admiral", 41);

        return result;
    }

    public List<String> getPossibleBranches()
    {
        List<String> result = new ArrayList<String>();
        result.add("Knight");
        result.add("Pirate");
        return result;
    }

    public String getActiveBranch(String playeruuid)
    {
        return getConfig().getString("players." + playeruuid + ".ActiveBranch");
    }

    private String getCorrectPrefix(String newRank) {
        switch (newRank)
        {
            case ("knight"): return "&8[&7Knight&8] &r";
            case ("baron"): return "&8[&7Baron&8] &r";
            case ("baroness"): return "&8[&7Baroness&8] &r";

            case ("lord"): return "#959595[&6Lord#959595] &r";
            case ("lady"): return "#959595[&6Lady#959595] &r";

            case ("duke"): return "#bfbfbf[#ffbf15Duke#bfbfbf] &r";
            case ("duchess"): return "#bfbfbf[#ffbf15Duchess#bfbfbf] &r";

            case ("prince"): return "[#FBFF0FPrince&r] &r";
            case ("princess"): return "[#FBFF0FPrincess&r] &r";


            case ("sailor"): return "&8&l{&7Sailor&8&l} &r";
            case ("pirate"): return "&8&l{&7Pirate&8&l} &r";

            case ("captain"): return "#bfbfbf{#ffbf15Captain#bfbfbf} &r";
            case ("admiral"): return "{&eAdmiral&r} &r";

            default: return "";

        }
    }

    Map<String, Boolean> map = new HashMap<>();

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerClick(PlayerInteractEntityEvent event) {
        if(!event.getHand().equals(EquipmentSlot.HAND))
            return;

        Player player = (Player) event.getPlayer();
        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Jerome"))
            {
                event.setCancelled(true);

                if (onCooldown.contains(player.getUniqueId().toString())) return;
                onCooldown.add(player.getUniqueId().toString());
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                    @Override
                    public void run() {
                        onCooldown.remove(player.getUniqueId().toString());
                    }
                }, 40L);

                map.put(player.getName(), true);

                TextComponent message1 = new TextComponent("[Info about Knight]");
                message1.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                message1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/commandun"));
                message1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me!").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));

                TextComponent message2 = new TextComponent("[Join Knights]");
                message2.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                message2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/commanddeux"));
                message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me!").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));

                player.sendMessage(ChatColor.GOLD + "--" + ChatColor.WHITE + " Hi, what can I do for you? " + ChatColor.GOLD + "--");
                player.spigot().sendMessage(message1);
                player.spigot().sendMessage(message2);

                event.setCancelled(true);

            }
        }
    }
    @EventHandler
    public void onPlayerTab(PlayerCommandSendEvent e) {
        List<String> blockedCommands = new ArrayList<>();
        blockedCommands.add("commandun");
        blockedCommands.add("commanddeux");
        e.getCommands().removeAll(blockedCommands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (map.containsKey(player.getName()) && map.get(player.getName()) == true)
        {
            switch (label)
            {
                case "commandun": // Knight Information
                    map.replace(player.getName(), true, false);
                    Bukkit.dispatchCommand(getServer().getConsoleSender(), "tellraw " + player.getName() + " [\"\",{\"text\":\"The Knights\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n The noble knights of Hazelguard, a faction of honor most loyal to the crown\\n\\n\"},{\"text\":\"The Dungeons\",\"color\":\"gold\"},{\"text\":\"\\n To get the Knight rank, you must beat the first 3 bosses in The Dungeons and trade-in the golden apples with \"},{\"text\":\"Diego\",\"color\":\"yellow\"},{\"text\":\". This will make you a knight and give you the \"},{\"text\":\"Royal Sword\",\"color\":\"yellow\"},{\"text\":\", an unbreakable golden sword that can be leveled up with mob kills.\\n\\n\"},{\"text\":\"Player Shops\",\"color\":\"gold\"},{\"text\":\"\\n Players of the Knight branch can own shops in Hazelguard. There they can sell or do whatever they like, even start up a casino or create a betting racket.\\n\\n\"},{\"text\":\"Knight Ranks\",\"color\":\"gold\"},{\"text\":\"\\n The knights have the ranks of \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Knight\",\"color\":\"gray\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Baron\",\"color\":\"gray\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"Lord\",\"color\":\"gold\"},{\"text\":\"]\",\"color\":\"dark_gray\"},{\"text\":\", \"},{\"text\":\"[\",\"color\":\"gray\"},{\"text\":\"Duke\",\"color\":\"gold\"},{\"text\":\"]\",\"color\":\"gray\"},{\"text\":\" and [\"},{\"text\":\"Prince\",\"color\":\"yellow\"},{\"text\":\"], and their female variants.\\n\\n \"}]");
                    return true;

                case "commanddeux": // Swap Branch
                    map.replace(player.getName(), true, false);
                    if (getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".ActiveBranch").contains("Knight"))
                    {
                        player.sendMessage("You're already a Knight.");
                        return false;
                    }
                    if (!getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".CurrentRank").contains("default"))
                    {
                        player.sendMessage("You need to be a default rank before you can join the knights.");
                        return false;
                    }

                    if (Bukkit.dispatchCommand(getServer().getConsoleSender(), "setbranch " + player.getName() + " Knight"))
                        player.sendMessage("You're now part of the knights!");
                    else
                        player.sendMessage("You can't become a knight atm.");
                    return true;

                default:
                    return false;
            }
        }

        else
        {
            map.put(player.getName(), false);
            return false;
        }

    }
}
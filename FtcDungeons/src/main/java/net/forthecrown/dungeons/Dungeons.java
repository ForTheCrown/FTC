package net.forthecrown.dungeons;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.dungeons.bosses.Drawned;
import net.forthecrown.dungeons.bosses.HideySpidey;
import net.forthecrown.dungeons.bosses.Skalatan;
import net.forthecrown.dungeons.bosses.Zhambie;
import net.forthecrown.dungeons.commands.addDonator;
import net.forthecrown.dungeons.commands.addlore;
import net.forthecrown.dungeons.commands.removeDonator;
import net.forthecrown.dungeons.enchantments.DolphinSwimmer;
import net.forthecrown.dungeons.enchantments.HealingBlock;
import net.forthecrown.dungeons.enchantments.PoisonCrit;
import net.forthecrown.dungeons.enchantments.StrongAim;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class Dungeons extends JavaPlugin implements Listener {

    List<ItemStack> zhambieItems = new ArrayList<>();
    List<ItemStack> skalatanItems = new ArrayList<>();
    List<ItemStack> spideyItems = new ArrayList<>();
    List<ItemStack> drawnedItems = new ArrayList<>();
    //List<ItemStack> magmacubeItems = new ArrayList<ItemStack>();
    ItemMeta meta;
    List<String> lore = new ArrayList<String>();
    Plugin plugin = this;

    Zhambie z = new Zhambie(this);
    Skalatan s = new Skalatan(this);
    HideySpidey h = new HideySpidey(this);
    Drawned d = new Drawned(this);
    //Magmalovania m = new Magmalovania(this);

    StrongAim enchant1 = new StrongAim(new NamespacedKey(this, "strongaim"), this);
    HealingBlock enchant2 = new HealingBlock(new NamespacedKey(this, "healingblock"), this);
    PoisonCrit enchant3 = new PoisonCrit(new NamespacedKey(this, "criticalpoison"), this);
    DolphinSwimmer enchant4 = new DolphinSwimmer(new NamespacedKey(this, "dolphinswimmer"), this);

    public void onEnable() {
        lore.add("Dungeon Item");

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        new addDonator(this);
        new removeDonator(this);
        new addlore(this);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(z, this);
        getServer().getPluginManager().registerEvents(s, this);
        getServer().getPluginManager().registerEvents(h, this);
        getServer().getPluginManager().registerEvents(d, this);
        //getServer().getPluginManager().registerEvents(m, this);

        fillLists();

        loadEnchantments();
        this.getServer().getPluginManager().registerEvents(enchant1, this);
        this.getServer().getPluginManager().registerEvents(enchant2, this);
        this.getServer().getPluginManager().registerEvents(enchant3, this);
        this.getServer().getPluginManager().registerEvents(enchant4, this);
    }

    private void fillLists() {
        makeItem(zhambieItems, Material.ROTTEN_FLESH, 15);
        makeItem(zhambieItems, Material.GOLD_NUGGET, 30);
        makeItem(zhambieItems, Material.GOLD_INGOT, 1);
        makeItem(zhambieItems, Material.DRIED_KELP, 45);

        makeItem(skalatanItems, Material.BONE, 15, "Stray Bone");
        makeItem(skalatanItems, Material.BONE, 30, "Horse Bone");
        makeItem(skalatanItems, Material.BONE, 30, "Floaty Bone");
        makeItem(skalatanItems, Material.BLACK_DYE, 30);

        makeItem(spideyItems, Material.STRING, 30);
        makeItem(spideyItems, Material.FERMENTED_SPIDER_EYE, 20);
        makeItem(spideyItems, Material.SPIDER_EYE, 45);
        makeSpecialItem(spideyItems, Material.TIPPED_ARROW, 5, PotionEffectType.POISON);

        makeItem(drawnedItems, Material.IRON_NUGGET, 1, "Iron Artifact");
        makeItem(drawnedItems, Material.PRISMARINE_CRYSTALS, 1, "Elder Artifact");
        makeItem(drawnedItems, Material.SCUTE, 1, "Turtle Artifact");
        makeItem(drawnedItems, Material.QUARTZ, 1, "Hidden Artifact");
        makeItem(drawnedItems, Material.NAUTILUS_SHELL, 1, "Nautilus Artifact");

        //makeSimpleItem(magmacubeItems, Material.BLAZE_POWDER, 1);
        //makeSimpleItem(magmacubeItems, Material.NETHER_WART, 1);
        //makeSimpleItem(magmacubeItems, Material.REDSTONE_BLOCK, 1);
    }


    private void makeItem(List<ItemStack> list, Material mat, int amount, String... name) {
        ItemStack item = new ItemStack(mat, amount);
        meta = item.getItemMeta();
        meta.setLore(lore);
        if (name != null) {
            String itemname = "";
            for (String str : name) {
                itemname = itemname + str;
            }
            meta.setDisplayName(itemname);
        }
        item.setItemMeta(meta);
        list.add(item);
    }


    private void makeSpecialItem(List<ItemStack> list, Material mat, int amount, PotionEffectType effect) {
        ItemStack item = new ItemStack(mat, amount);
        meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);

        PotionMeta pmeta = (PotionMeta) item.getItemMeta();
        pmeta.setBasePotionData(new PotionData(PotionType.POISON));
        item.setItemMeta(pmeta);
        list.add(item);

    }

    ArrayList<Integer> mayChange = new ArrayList<Integer>();

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Husk) {
            Husk dummy = (Husk) event.getEntity();
            if (dummy.getCustomName() != null && (dummy.getCustomName().contains(ChatColor.GOLD + "Hit Me!") || dummy.getCustomName().contains(ChatColor.RED + "Damage:"))) {
                String damageName = ChatColor.RED + "Damage: " + String.format("%.1f", event.getDamage());;
                dummy.setCustomName(damageName);
                mayChange.add(0);

                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> dummy.setHealth(200), 5L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                    mayChange.remove(0);
                    if (mayChange.isEmpty()) {
                        dummy.setCustomName(ChatColor.GOLD + "Hit Me!");
                        dummy.setHealth(200);
                    }
                }, 100L);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void anvil(PrepareAnvilEvent event) {
        if (event.getInventory() == null) return;
        if (event.getInventory().getItem(0) != null && event.getInventory().getItem(1) != null && event.getInventory().getItem(2) != null) {
            if ((event.getInventory().getItem(0).getItemMeta().getDisplayName() != null && event.getInventory().getItem(1).getItemMeta().getDisplayName().contains(ChatColor.YELLOW + "" + ChatColor.BOLD + "Royal Sword"))
                    || ((event.getInventory().getItem(0).getItemMeta().getDisplayName() != null && event.getInventory().getItem(0).getItemMeta().getDisplayName().contains(ChatColor.YELLOW + "" + ChatColor.BOLD + "Royal Sword")))) {
                ItemStack item = event.getResult();
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "-" + ChatColor.YELLOW + ChatColor.BOLD + "Royal Sword" + ChatColor.GOLD + "-");
                item.setItemMeta(meta);
                event.setResult(item);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        boolean found = false;
        for (ItemStack item : event.getEntity().getInventory().getContents()) {
            if (item != null) {
                if (item.getItemMeta().getLore() != null && item.getItemMeta().getLore().contains("Dungeon Item")) {
                    found = true;
                    if (item.getAmount() >= 10) item.setAmount(item.getAmount() - ThreadLocalRandom.current().nextInt(0, 11));
                    else item.setAmount(ThreadLocalRandom.current().nextInt(0, item.getAmount()+1));
                }
            }
        }
        if (found) event.getEntity().sendMessage(ChatColor.RED + "[FTC] " + ChatColor.WHITE + "You lost a random amount of your Dungeon Items...");
    }


    @EventHandler
    public void onPlayerClick(PlayerInteractEntityEvent event) {
        if(!event.getHand().equals(EquipmentSlot.HAND))
            return;
        Player player = (Player) event.getPlayer();

        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            if (event.getRightClicked().getName().contains(ChatColor.GOLD + "Diego")) {
                if (checkIfInvContainsAllApples(player.getInventory())) {
                    takeGoldenApples(player.getInventory());

                    ItemStack sword = new ItemStack(Material.GOLDEN_SWORD, 1);
                    ItemMeta swordMeta = sword.getItemMeta();
                    swordMeta.setDisplayName(ChatColor.GOLD + "-" + ChatColor.YELLOW + ChatColor.BOLD + "Royal Sword" + ChatColor.GOLD + "-");
                    ArrayList<String> lore = new ArrayList<String>();
                    lore.add(ChatColor.GRAY + "Rank I");
                    lore.add(ChatColor.DARK_GRAY + "------------------------------");
                    lore.add(ChatColor.GOLD + "The bearer of this weapon has proven themselves,");
                    lore.add(ChatColor.GOLD + "not only to the Crown, but also to the Gods...");
                    lore.add(ChatColor.DARK_GRAY + "------------------------------");
                    lore.add(ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Zombies to Rank Up.");
                    lore.add(ChatColor.DARK_GRAY + "Donators can upgrade Royal tools beyond Rank 5.");
                    swordMeta.setLore(lore);
                    swordMeta.setUnbreakable(true);
                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 7, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
                    swordMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
                    modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", -2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
                    swordMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, modifier);
                    swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    sword.setItemMeta(swordMeta);
                    sword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                    sword.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                    sword.addEnchantment(Enchantment.SWEEPING_EDGE, 3);
                    player.getInventory().addItem(sword);
                    player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
                    for (int i = 0; i <= 5; i++) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().getX(), player.getLocation().getY()+2, player.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d);
                            }
                        }, i*5L);
                    }
                    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "You've been promoted to " + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Knight" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " !");
                    player.sendMessage(ChatColor.WHITE + "You can select the tag in " + ChatColor.YELLOW + "/rank" + ChatColor.WHITE + " now.");
                    FtcCore.getUser(player.getUniqueId()).addRank(Rank.KNIGHT);
                    Bukkit.dispatchCommand(getServer().getConsoleSender(), "lp user " + player.getName() + " parent add free-rank");
                }
                else {
                    player.sendMessage(ChatColor.RED + "[FTC]" + ChatColor.GRAY + " You need one of each kind of the boss Golden Apples to get the Royal Sword.");
                }
            }
        }
        else if (event.getRightClicked().getType() == EntityType.SLIME) {
            String name = event.getRightClicked().getName();

            if (name.contains(ChatColor.AQUA + "Spawn Zhambie")) {
                if (checkIfInvContainsAllItems(event.getPlayer().getInventory(), zhambieItems)) {
                    takeItems(event.getPlayer().getInventory(), zhambieItems);
                    player.sendMessage(ChatColor.GRAY + "[FTC] You have summoned Zhambie!");
                    z.summonZhambie(event.getRightClicked().getLocation());
                }
                else {
                    sendZombieMessage(player, false);
                }
            }
            else if (name.contains(ChatColor.AQUA + "Zombie Level Info")) {
                sendZombieMessage(player, true);
            }

            else if (name.contains(ChatColor.AQUA + "Spawn Skalatan")) {
                if (checkIfInvContainsAllItems(event.getPlayer().getInventory(), skalatanItems)) {
                    takeItems(event.getPlayer().getInventory(), skalatanItems);
                    player.sendMessage(ChatColor.GRAY + "[FTC] You have summoned Skalatan!");
                    s.summonSkalatan(event.getRightClicked().getLocation());
                }
                else {
                    sendSkeletonMessage(player, false);
                }
            }
            else if (name.contains(ChatColor.AQUA + "Skeleton Level Info")) {
                sendSkeletonMessage(player, true);
            }

            else if (name.contains(ChatColor.AQUA + "Spawn Hidey Spidey")) {
                if (checkIfInvContainsAllItems(event.getPlayer().getInventory(), spideyItems)) {
                    takeItems(event.getPlayer().getInventory(), spideyItems);
                    player.sendMessage(ChatColor.GRAY + "[FTC] You have summoned Hidey Spidey!");
                    h.summonHideySpidey(event.getRightClicked().getLocation());
                }
                else {
                    sendSpiderMessage(player, false);
                }
            }
            else if (name.contains(ChatColor.AQUA + "Spider Level Info")) {
                sendSpiderMessage(player, true);
            }
            else if (name.contains(ChatColor.AQUA + "Spawn Drawned")) {
                if (checkIfInvContainsAllItems(event.getPlayer().getInventory(), drawnedItems)) {
                    takeItems(event.getPlayer().getInventory(), drawnedItems);
                    player.sendMessage(ChatColor.GRAY + "[FTC] You have summoned Drawned!");
                    d.summonDrawned(event.getRightClicked().getLocation());
                }
                else {
                    sendWaterMessage(player, false);
                }
            }
            else if (name.contains(ChatColor.AQUA + "Water Level Info")) {
                sendWaterMessage(player, true);
            }

            else if (name.contains(ChatColor.AQUA + "Right Click Me!")) {
                player.sendMessage(ChatColor.AQUA + "[FTC] " + ChatColor.WHITE + "Right clicking this will show you a list of items you need to gather throughout this level.");
            }
            else if (name.contains(ChatColor.AQUA + "Right Click to Spawn")) {
                player.sendMessage(ChatColor.AQUA + "[FTC] " + ChatColor.WHITE + "If you have all the required items in your inventory, right clicking this will spawn the boss.");
            }
            else if (name.contains(ChatColor.AQUA + "customs")) {
                ItemStack item = new ItemStack(Material.BOW, 1);
                ItemMeta meta = item.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();
                lore.add(ChatColor.GRAY + enchant1.getName());
                meta.setLore(lore);
                item.setItemMeta(meta);
                item.addUnsafeEnchantment(enchant1, 1);
                player.getInventory().addItem(item);

                ItemStack item2 = new ItemStack(Material.SHIELD, 1);
                ItemMeta meta2 = item2.getItemMeta();
                ArrayList<String> lore2 = new ArrayList<String>();
                lore2.add(ChatColor.GRAY + enchant2.getName());
                meta2.setLore(lore2);
                item2.setItemMeta(meta2);
                item2.addUnsafeEnchantment(enchant2, 1);
                player.getInventory().addItem(item2);

                ItemStack item3 = new ItemStack(Material.DIAMOND_SWORD, 1);
                ItemMeta meta3 = item3.getItemMeta();
                ArrayList<String> lore3 = new ArrayList<String>();
                lore3.add(ChatColor.GRAY + enchant3.getName());
                meta3.setLore(lore3);
                item3.setItemMeta(meta3);
                item3.addUnsafeEnchantment(enchant3, 1);
                player.getInventory().addItem(item3);

                ItemStack item4 = new ItemStack(Material.GOLDEN_SWORD, 1);
                ItemMeta meta4 = item4.getItemMeta();
                meta4.setDisplayName(ChatColor.GOLD + "-" + ChatColor.YELLOW + ChatColor.BOLD + "Royal Sword" + ChatColor.GOLD + "-");
                ArrayList<String> lore4 = new ArrayList<>();
                lore4.add(ChatColor.GRAY + "Rank I");
                lore4.add(ChatColor.DARK_GRAY + "------------------------------");
                lore4.add(ChatColor.GOLD + "The bearer of this weapon has proven themselves");
                lore4.add(ChatColor.GOLD + "not only to the Crown but also to the Gods...");
                lore4.add(ChatColor.DARK_GRAY + "------------------------------");
                lore4.add(ChatColor.AQUA + "0/1000"+ ChatColor.DARK_AQUA + " Zombies to Rank Up.");
                lore4.add(ChatColor.DARK_GRAY + "Donators can upgrade Royal tools beyond Rank 5.");
                meta4.setLore(lore4);
                meta4.setUnbreakable(true);
                AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 7, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
                meta4.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", -2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
                meta4.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, modifier);
                meta4.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item4.setItemMeta(meta4);
                item4.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                item4.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                item4.addEnchantment(Enchantment.SWEEPING_EDGE, 3);
                player.getInventory().addItem(item4);

                ItemStack item5 = new ItemStack(Material.TRIDENT, 1);
                ItemMeta meta5 = item5.getItemMeta();
                ArrayList<String> lore5 = new ArrayList<String>();
                lore5.add(ChatColor.GRAY + enchant4.getName());
                meta5.setLore(lore5);
                item5.setItemMeta(meta5);
                item5.addUnsafeEnchantment(enchant4, 1);
                player.getInventory().addItem(item5);
            }
			/*else if (name.contains(ChatColor.RED + "Magmalovania")) {
				if (!m.magmalovanias.isEmpty())
				{
					player.sendMessage(ChatColor.GRAY + "[FTC] There can only be one alive at the same time.");

				}
				else if (checkIfInvContainsMagmaAllItems(event.getPlayer().getInventory()) == true) {
					takeMagmaItems(event.getPlayer().getInventory());
					player.sendMessage(ChatColor.GRAY + "[FTC] You have summoned Magmalovania!");
					m.summonMagmalovania(event.getRightClicked().getLocation(), event.getPlayer().getName());
				}
				doPoofSmoke(event.getRightClicked().getLocation());
			}*/
        }
    }

	/*private void doPoofSmoke(Location loc) {
		loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.getX(), loc.getY()+0.5, loc.getZ(), 5, 0.0, 0.6, 0.0, 0.01);
		loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.3f, 0.8f);

	}

	// Magma
	private boolean checkIfInvContainsMagmaAllItems(PlayerInventory inv) {
		int size = 36;
		Boolean found;
		for (ItemStack item : magmacubeItems) {
			found = false;
	    	for (int i = 0; i < size; i++) {
	    		ItemStack invItem = inv.getItem(i);
	    		if (invItem != null) {
		    		if (invItem.getType() == item.getType()) {
	    				found = true;
		    			break;
		    		}
	    		}
	    	}
	    	if (!found) {
	    		return false;
	    	}
		}
		return true;
	}

	// Magma
	private void takeMagmaItems(PlayerInventory inv) {
		int size = 36;
		for (ItemStack item : magmacubeItems) {
	    	for (int i = 0; i < size; i++) {
	    		ItemStack invItem = inv.getItem(i);
	    		if (inv.getItem(i) != null)
	    		{
		    		if (invItem.getType() == item.getType())
		    			invItem.setAmount(invItem.getAmount() - item.getAmount());
	    		}
	    	}

		}
		return;
	}

	// Magma
	private void makeSimpleItem(List<ItemStack> list, Material mat, int amount) {
		ItemStack item = new ItemStack(mat, amount);
		list.add(item);
	}*/

    private LootTable empty = new LootTable() {
        @Override
        public NamespacedKey getKey() {return new NamespacedKey(plugin, "empty");}
        @Override
        public Collection<ItemStack> populateLoot(Random arg0, LootContext arg1) {return null;}
        @Override
        public void fillInventory(Inventory arg0, Random arg1, LootContext arg2) {}
    };

    @EventHandler(ignoreCancelled = true)
    public void onMotherKill(EntityDeathEvent event){
        if(event.getEntity().getKiller() != null && event.getEntity() instanceof Spider){
            if (event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() == 75d) {
                Location spawnLoc =  event.getEntity().getLocation();

                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i <= 2; i++) {
                            CaveSpider caveSpider = spawnLoc.getWorld().spawn(spawnLoc.add(new Vector(0.2*i*Math.pow(-1, i), i*0.1, 0.2*i*Math.pow(-1, i))), CaveSpider.class);
                            caveSpider.setLootTable(empty);
                            caveSpider.setHealth(1);
                        }
                    }
                }, 15L);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event){
        if(event.getEntity().getKiller() != null){
            if (event.getEntity().getKiller().getInventory().getItemInMainHand().getType() == Material.GOLDEN_SWORD || ((Player) event.getEntity().getKiller()).getInventory().getItemInMainHand().getType() == Material.NETHERITE_SWORD) {
                Player player = event.getEntity().getKiller();
                if (player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains(ChatColor.YELLOW + "" + ChatColor.BOLD + "Royal Sword") || player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains(net.md_5.bungee.api.ChatColor.of("#D1C8BA") + "")) {
                    EntityDamageEvent event2 = event.getEntity().getLastDamageCause();
                    if (!(event2.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) || event2.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK))) return;
                    ItemStack sword = player.getInventory().getItemInMainHand();
                    swordAddXp(sword, event.getEntity(), player);

                }
            }
        }
    }

    private void swordAddXp(ItemStack sword, LivingEntity entity, Player player) {
        if (sword == null || entity == null) return;
        ItemMeta meta = sword.getItemMeta();
        List<String> lore = meta.getLore();
        String rankLine = lore.get(0);

        boolean isDonator = Bukkit.getPlayer(UUID.fromString(player.getUniqueId().toString())).hasPermission("ftc.donator1");

        if (rankLine.contains(ChatColor.GRAY + "Rank IX")) {
            if (entity.getType().equals(EntityType.WITHER) && isDonator) actuallyAddXp(player.getName(), lore, meta, sword, "Rank X", ChatColor.DARK_AQUA + "Max Rank.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank VIII")) {
            if (entity.getType().equals(EntityType.CREEPER) && isDonator) {
                if (((Creeper) entity).isPowered()) actuallyAddXp(player.getName(), lore, meta, sword, "Rank IX", ChatColor.AQUA + "0/3" + ChatColor.DARK_AQUA + " Withers to Rank Up.");
            }
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank VII")) {
            if (entity.getType().equals(EntityType.GHAST) && isDonator) actuallyAddXp(player.getName(), lore, meta, sword, "Rank VIII", ChatColor.AQUA + "0/10" + ChatColor.DARK_AQUA + " Charged Creepers to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank VI")) {
            if (entity.getType().equals(EntityType.ENDERMAN) && isDonator) actuallyAddXp(player.getName(), lore, meta, sword, "Rank VII", ChatColor.AQUA + "0/50"+ ChatColor.DARK_AQUA + " Ghasts to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank IV")) {
            if (entity.getType().equals(EntityType.CREEPER)) actuallyAddXp(player.getName(), lore, meta, sword, "Rank V", ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Blazes to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank V")) {
            if (entity.getType().equals(EntityType.BLAZE) && isDonator) actuallyAddXp(player.getName(), lore, meta, sword, "Rank VI", ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Endermen to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank III")) {
            if (entity.getType().equals(EntityType.SNOWMAN)) actuallyAddXp(player.getName(), lore, meta, sword, "Rank IV", ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Creepers to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank II")) {
            if (entity.getType().equals(EntityType.SKELETON)) actuallyAddXp(player.getName(), lore, meta, sword, "Rank III", ChatColor.AQUA + "0/100" + ChatColor.DARK_AQUA + " Snowmen to Rank Up.");
        }
        else if (rankLine.contains(ChatColor.GRAY + "Rank I")) {
            if (entity.getType().equals(EntityType.ZOMBIE)) actuallyAddXp(player.getName(), lore, meta, sword, "Rank II", ChatColor.AQUA + "0/1000" + ChatColor.DARK_AQUA + " Skeletons to Rank Up.");
        }
    }

    private void actuallyAddXp(String player, List<String> lore, ItemMeta meta, ItemStack sword, String newRank, String Upgrade) {
        int xp = Integer.parseInt(lore.get(5).split("/")[0].substring(2)) + 1;
        int xpneeded = Integer.parseInt(lore.get(5).split("/")[1].split("§3")[0].replace(" ", ""));
        if (xp >= xpneeded) {
            lore.remove(0);
            lore.add(0, ChatColor.GRAY + newRank);
            lore.remove(5);
            lore.add(5, Upgrade);
            Bukkit.getPlayer(player).playSound(Bukkit.getPlayer(player).getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
            Bukkit.getPlayer(player).playSound(Bukkit.getPlayer(player).getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
            for (int i = 0; i <= 5; i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> Bukkit.getPlayer(player).getWorld().spawnParticle(Particle.TOTEM, Bukkit.getPlayer(player).getLocation().getX(), Bukkit.getPlayer(player).getLocation().getY()+2, Bukkit.getPlayer(player).getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d), i*5L);
            }

            Bukkit.getPlayer(player).sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.WHITE + "Your Sword was upgraded to " + newRank + "!");
            if (newRank.contains("Rank V") && (!newRank.contains("I"))) {
                meta.setLore(lore);
                sword.setItemMeta(meta);
                sword.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 4);
                Bukkit.getPlayer(player).getInventory().setItemInMainHand(sword);
                Bukkit.getPlayer(player).sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "Looting IV has been added to your Sword.");
            }
            else if (newRank.contains("Rank X")) {
                meta.setLore(lore);
                sword.setItemMeta(meta);
                sword.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 5);
                Bukkit.getPlayer(player).getInventory().setItemInMainHand(sword);
                Bukkit.getPlayer(player).sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "Looting V has been added to your Sword.");

            }
        }
        else {
            lore.remove(5);
            lore.add(5, ChatColor.AQUA + "" + xp + "/" + meta.getLore().get(5).split("/")[1]);
        }
        meta.setLore(lore);
        sword.setItemMeta(meta);
    }


    private boolean canDrop = true;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJosh(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof WitherSkeleton && (event.getDamager() instanceof Player || event.getDamager().getType() == EntityType.ARROW)) {
            WitherSkeleton Josh = (WitherSkeleton) event.getEntity();
            if (Josh.getCustomName() != null && Josh.getCustomName().contains("Josh") && Josh.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                if (canDrop == false) return;

                double random = Math.random();
                if (random < 0.25) {
                    ItemStack item = new ItemStack(Material.BLACK_DYE, 1);
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = new ArrayList<String>();
                    lore.add("Dungeon Item");
                    meta.setLore(lore);
                    meta.setDisplayName("Wither Goo");
                    item.setItemMeta(meta);
                    Item joshDrop = Josh.getWorld().dropItem(Josh.getLocation(), item);
                    joshDrop.setVelocity(new Vector(0, 0.2, 0));
                }
                else return;
                this.canDrop = false;
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        canDrop = true;
                    }
                }, 100L);
            }
        }
    }

    private void sendZombieMessage(Player player, boolean info) {
        if (!info) {
            player.sendMessage(ChatColor.GRAY + "You don't have all the items in your inventory!");
            player.sendMessage(ChatColor.AQUA + "To spawn Zhambie, you need:");
        }
        else {
            player.sendMessage(ChatColor.AQUA + "To spawn the boss in this level, you need:");
        }
        player.sendMessage(ChatColor.AQUA + "- 45" + ChatColor.DARK_AQUA + " Dried Kelp");
        player.sendMessage(ChatColor.AQUA + "- 30" + ChatColor.DARK_AQUA + " Gold Nuggets");
        player.sendMessage(ChatColor.AQUA + "- 15" + ChatColor.DARK_AQUA + " Knight's Flesh");
        player.sendMessage(ChatColor.AQUA + "- 1" + ChatColor.DARK_AQUA + " Hidden Mummy Ingot");
    }

    private void sendSkeletonMessage(Player player, boolean info) {
        if (!info) {
            player.sendMessage(ChatColor.GRAY + "You don't have all the items in your inventory!");
            player.sendMessage(ChatColor.AQUA + "To spawn Skalatan, you need:");
        }
        else {
            player.sendMessage(ChatColor.AQUA + "To spawn the boss in this level, you need:");
        }
        player.sendMessage(ChatColor.AQUA + "- 30" + ChatColor.DARK_AQUA + " Wither Goo");
        player.sendMessage(ChatColor.AQUA + "- 30" + ChatColor.DARK_AQUA + " Floaty Bones");
        player.sendMessage(ChatColor.AQUA + "- 30" + ChatColor.DARK_AQUA + " Horse Bones");
        player.sendMessage(ChatColor.AQUA + "- 15" + ChatColor.DARK_AQUA + " Stray Bones");
    }

    private void sendSpiderMessage(Player player, boolean info) {
        if (!info) {
            player.sendMessage(ChatColor.GRAY + "You don't have all the items in your inventory!");
            player.sendMessage(ChatColor.AQUA + "To spawn Hidey Spidey, you need:");
        }
        else {
            player.sendMessage(ChatColor.AQUA + "To spawn the boss in this level, you need:");
        }
        player.sendMessage(ChatColor.AQUA + "- 45" + ChatColor.DARK_AQUA + " Spider Eyes");
        player.sendMessage(ChatColor.AQUA + "- 20" + ChatColor.DARK_AQUA + " Fermented Spider Eyes");
        player.sendMessage(ChatColor.AQUA + "- 30" + ChatColor.DARK_AQUA + " String");
        player.sendMessage(ChatColor.AQUA + "- 5" + ChatColor.DARK_AQUA + " Tipped Poison Arrows");
    }

    private void sendWaterMessage(Player player, boolean info) {
        if (!info) {
            player.sendMessage(ChatColor.GRAY + "You don't have all the items in your inventory!");
            player.sendMessage(ChatColor.AQUA + "To spawn Drawned, you need:");
        }
        else {
            player.sendMessage(ChatColor.AQUA + "To spawn the boss in this level, you need:");
        }
        player.sendMessage(ChatColor.AQUA + "- 1" + ChatColor.DARK_AQUA + " Iron Artifact");
        player.sendMessage(ChatColor.AQUA + "- 1" + ChatColor.DARK_AQUA + " Elder Artifact");
        player.sendMessage(ChatColor.AQUA + "- 1" + ChatColor.DARK_AQUA + " Turtle Artifact");
        player.sendMessage(ChatColor.AQUA + "- 1" + ChatColor.DARK_AQUA + " Hidden Artifact");
        player.sendMessage(ChatColor.AQUA + "- 1" + ChatColor.DARK_AQUA + " Nautilus Artifact");
    }

    private boolean checkIfInvContainsAllItems(PlayerInventory inv, List<ItemStack> itemsNeeded) {
        int size = 36;
        Boolean found;
        for (ItemStack item : itemsNeeded) {
            found = false;
            for (int i = 0; i < size; i++) {
                ItemStack invItem = inv.getItem(i);
                if (invItem != null) {
                    if (invItem.getType() == item.getType() && invItem.getAmount() >= item.getAmount()) {
                        if (invItem.getItemMeta().getLore() != null && invItem.getItemMeta().getLore().contains("Dungeon Item")) {
                            if (invItem.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName() != null && invItem.getItemMeta().getDisplayName().contains(item.getItemMeta().getDisplayName())) {
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private void takeItems(PlayerInventory inv, List<ItemStack> itemsToTake) {
        int size = 36;
        for (ItemStack item : itemsToTake) {
            for (int i = 0; i < size; i++) {
                ItemStack invItem = inv.getItem(i);
                if (inv.getItem(i) != null) {

                    if (invItem.getType() == item.getType() && invItem.getAmount() >= item.getAmount()) {
                        if (invItem.getItemMeta().getLore() != null && invItem.getItemMeta().getLore().contains("Dungeon Item")) {
                            if (invItem.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName() != null && invItem.getItemMeta().getDisplayName().contains(item.getItemMeta().getDisplayName())) {
                                invItem.setAmount(invItem.getAmount() - item.getAmount());
                            }
                        }
                    }
                }
            }

        }
        return;
    }

    private void takeGoldenApples(PlayerInventory inv) {
        int size = 36;
        List<ItemStack> itemsToGet = new ArrayList<ItemStack>();

        ItemStack apple = new ItemStack(Material.GOLDEN_APPLE, 1);
        ItemMeta meta = apple.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add("hug");
        meta.setLore(lore);
        apple.setItemMeta(meta);
        itemsToGet.add(apple);

        ItemStack apple2 = new ItemStack(Material.GOLDEN_APPLE, 1);
        ItemMeta meta2 = apple2.getItemMeta();
        lore.clear();;
        lore.add("style");
        meta2.setLore(lore);
        apple2.setItemMeta(meta2);
        itemsToGet.add(apple2);

        ItemStack apple3 = new ItemStack(Material.GOLDEN_APPLE, 1);
        ItemMeta meta3 = apple3.getItemMeta();
        lore.clear();
        lore.add("hide");
        meta3.setLore(lore);
        apple3.setItemMeta(meta3);
        itemsToGet.add(apple3);

        for (ItemStack item : itemsToGet) {
            for (int i = 0; i < size; i++) {
                ItemStack invItem = inv.getItem(i);
                if (inv.getItem(i) != null) {

                    if (invItem.getType() == Material.GOLDEN_APPLE && invItem.getAmount() >= 1) {
                        if (invItem.getItemMeta().getLore() != null && invItem.getItemMeta().getLore().get(0).contains(item.getItemMeta().getLore().get(0))) {
                            invItem.setAmount(invItem.getAmount() - 1);
                        }
                    }
                }
            }

        }
        return;
    }



    private boolean checkIfInvContainsAllApples(PlayerInventory inv) {
        int size = 36;
        List<ItemStack> itemsToGet = new ArrayList<ItemStack>();

        ItemStack apple = new ItemStack(Material.GOLDEN_APPLE, 1);
        ItemMeta meta = apple.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add("hug");
        meta.setLore(lore);
        apple.setItemMeta(meta);
        itemsToGet.add(apple);

        ItemStack apple2 = new ItemStack(Material.GOLDEN_APPLE, 1);
        ItemMeta meta2 = apple2.getItemMeta();
        lore.clear();;
        lore.add("style");
        meta2.setLore(lore);
        apple2.setItemMeta(meta2);
        itemsToGet.add(apple2);

        ItemStack apple3 = new ItemStack(Material.GOLDEN_APPLE, 1);
        ItemMeta meta3 = apple3.getItemMeta();
        lore.clear();
        lore.add("hide");
        meta3.setLore(lore);
        apple3.setItemMeta(meta3);
        itemsToGet.add(apple3);

        Boolean found;

        for (ItemStack item : itemsToGet) {
            found = false;

            for (int i = 0; i < size; i++) {
                ItemStack invItem = inv.getItem(i);
                if (invItem != null) {

                    if (invItem.getType() == Material.GOLDEN_APPLE && invItem.getAmount() >= 1) {
                        if (invItem.getItemMeta().getLore() != null && invItem.getItemMeta().getLore().get(0).contains(item.getItemMeta().getLore().get(0))) {
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (!found) {
                return false;
            }
        }
        return true;
    }


    @SuppressWarnings("unchecked")
    public void onDisable() {
        try {
            Field byIdField = Enchantment.class.getDeclaredField("byId");
            Field byNameField = Enchantment.class.getDeclaredField("byName");

            byIdField.setAccessible(true);
            byNameField.setAccessible(true);

            HashMap<NamespacedKey, Enchantment> byId = (HashMap<NamespacedKey, Enchantment>) byIdField.get(null);
            HashMap<NamespacedKey, Enchantment> byName = (HashMap<NamespacedKey, Enchantment>) byNameField.get(null);

            if (byId.containsKey(enchant1.getKey())) {
                byId.remove(enchant1.getKey());
            }
            if (byName.containsKey(enchant1.getKey())) {
                byName.remove(enchant1.getKey());
            }

            if (byId.containsKey(enchant2.getKey())) {
                byId.remove(enchant2.getKey());
            }
            if (byName.containsKey(enchant2.getKey())) {
                byName.remove(enchant2.getKey());
            }

            if (byId.containsKey(enchant3.getKey())) {
                byId.remove(enchant3.getKey());
            }
            if (byName.containsKey(enchant3.getKey())) {
                byName.remove(enchant3.getKey());
            }

            if (byId.containsKey(enchant4.getKey())) {
                byId.remove(enchant4.getKey());
            }
            if (byName.containsKey(enchant4.getKey())) {
                byName.remove(enchant4.getKey());
            }

        } catch (Exception ignored) {
        }
    }

    private void loadEnchantments() {
        try {
            try {
                Field f = Enchantment.class.getDeclaredField("acceptingNew");
                f.setAccessible(true);
                f.set(null, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Enchantment.registerEnchantment(enchant1);
            } catch (IllegalArgumentException ignored) {
            }

            try {
                Enchantment.registerEnchantment(enchant2);
            } catch (IllegalArgumentException ignored) {
            }

            try {
                Enchantment.registerEnchantment(enchant3);
            } catch (IllegalArgumentException ignored) {
            }

            try {
                Enchantment.registerEnchantment(enchant4);
            } catch (IllegalArgumentException ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package net.forthecrown.royals.dungeons;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.clickevent.ClickEventHandler;
import net.forthecrown.core.clickevent.ClickEventTask;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.forthecrown.royals.RoyalUtils;
import net.forthecrown.royals.Royals;
import net.forthecrown.royals.dungeons.bosses.BossItems;
import net.forthecrown.royals.dungeons.bosses.Bosses;
import net.forthecrown.royals.dungeons.bosses.mobs.Drawned;
import net.forthecrown.royals.dungeons.bosses.mobs.Skalatan;
import net.forthecrown.royals.dungeons.bosses.mobs.Zhambie;
import net.forthecrown.royals.enchantments.CrownEnchant;
import net.forthecrown.royals.enchantments.RoyalEnchants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Bukkit.getServer;

public class DungeonEvents implements Listener, ClickEventTask {

    private final Royals plugin;
    private final String id;

    public DungeonEvents(Royals plugin) {
        this.plugin = plugin;
        this.id = ClickEventHandler.registerClickEvent(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity().getPersistentDataContainer().has(RoyalUtils.PUNCHING_BAG_KEY, PersistentDataType.BYTE)) {
            hitDummy((Husk) event.getEntity(), event.getDamage());
            return;
        }
        if(event.getEntity() instanceof WitherSkeleton && event.getEntity().getCustomName().contains("Josh") && DungeonAreas.DUNGEON_AREA.contains(event.getEntity())){
            ThreadLocalRandom random = ThreadLocalRandom.current();
            if(random.nextInt(4) > 0) return;
            WitherSkeleton skeleton = (WitherSkeleton) event.getEntity();
            ItemStack item = Skalatan.witherGoo();
            item.setAmount(1);
            skeleton.getWorld().dropItemNaturally(skeleton.getLocation(), item);
        }
    }

    private final Map<Husk, BukkitRunnable> hit = new HashMap<>();
    private void hitDummy(Husk dummy, double damage){
        Component name = Component.text("Damage: " + String.format("%.1f",damage)).color(NamedTextColor.RED);
        dummy.customName(name);
        dummy.setHealth(200);

        if(hit.containsKey(dummy)) hit.get(dummy).cancel();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                dummy.customName(Component.text("Hit Me!").color(NamedTextColor.GOLD));
                hit.remove(dummy);
            }
        };
        runnable.runTaskLater(Royals.inst, 100);
        hit.put(dummy, runnable);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!DungeonAreas.DUNGEON_AREA.contains(event.getRightClicked())) return;

        if(event.getRightClicked() instanceof Villager){
            Villager villager = (Villager) event.getRightClicked();
            if(!villager.isInvulnerable()) return;
            if(villager.getCustomName() == null) return;
            if(!villager.getCustomName().contains("Diego")) return;
            Player player = event.getPlayer();

            ClickEventHandler.allowCommandUsage(event.getPlayer(), true, false);
            TextComponent component = Component.text()
                    .append(Component.text("Hello, what can I do for ya?").color(NamedTextColor.YELLOW))
                    .append(Component.newline())
                    .append(Component.text("[Claim Royal Sword]")
                            .color(NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.runCommand(ClickEventHandler.getCommand(id, "sword")))
                            .hoverEvent(CrownItems.BASE_ROYAL_SWORD.asHoverEvent())
                    )
                    .append(Component.text(" or "))
                    .append(Component.text("[Claim Trident]")
                            .color(NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.runCommand(ClickEventHandler.getCommand(id, "trident")))
                            .hoverEvent(FORK.asHoverEvent())
                    )
                    .build();
            player.sendMessage(component);
        }

        if(!(event.getRightClicked() instanceof Slime)) return;

        Slime slime = (Slime) event.getRightClicked();
        if(slime.getCustomName() == null) return;
        Player player = event.getPlayer();
        String name = ChatColor.stripColor(slime.getCustomName());

        //Interaction cooldown
        if(Cooldown.contains(player)) return;
        Cooldown.add(player, 20);

        //Boss spawning
        if(name.contains("Spawn ")){
            Bosses.BY_NAME.get(name.replaceAll("Spawn ", "").trim()).attemptSpawn(player);
            return;
        }

        //Level 4 artifactss
        if(name.contains("Artifact")){
            //Essentials kit signs can suck my [REDACTED]
            Drawned.Artifacts artifacts = Drawned.Artifacts.valueOf(name.substring(0, name.indexOf(" ")).toUpperCase());
            if(Cooldown.contains(player, "dungeons_" + artifacts.toString().toLowerCase() + "_artifact")) return;
            Cooldown.add(player, "dungeons_" + artifacts.toString().toLowerCase() + "_artifact", 5*60*20);
            player.getInventory().addItem(artifacts.item());
            player.sendMessage(
                    Component.text("You got the ")
                    .color(NamedTextColor.GRAY)
                    .append(slime.customName().color(NamedTextColor.YELLOW))
            );
            return;

            //Hidden -> -171.5 42, 84.5
            //Nautilus -> -105.5 45 126.5
            //Iron -> -110.5 46 140.5
        }

        switch (name){
            case "Right Click Me!":
                player.sendMessage(Component.text()
                        .append(FtcCore.prefix().color(NamedTextColor.AQUA))
                        .append(Component.text("Right clicking this will show you a list of items needed to spawn the level's boss"))
                        .build()
                );
                break;
            case "Right Click to Spawn":
                player.sendMessage(Component.text()
                        .append(FtcCore.prefix().color(NamedTextColor.AQUA))
                        .append(Component.text("If you have all the required items, using this, will spawn the boss"))
                        .build()
                );
                break;

            case "Zombie Level Info":
                player.sendMessage(RoyalUtils.itemRequiredMessage(Bosses.zhambie()));
                break;
            case "Skeleton Level Info":
                player.sendMessage(RoyalUtils.itemRequiredMessage(Bosses.skalatan()));
                break;
            case "Water Level Info":
                player.sendMessage(RoyalUtils.itemRequiredMessage(Bosses.drawned()));
                break;
            case "Spider Level Info":
                player.sendMessage(RoyalUtils.itemRequiredMessage(Bosses.hideySpidey()));
                break;
            case "Hidden Mummy Ingot":
                if(Cooldown.contains(player, "Dungeons_Mummy_Ingot")) return;
                player.sendMessage(CrownUtils.translateHexCodes("&7You got the &eHidden Mummy Ingot"));
                player.getInventory().addItem(Zhambie.mummyIngot());
                Cooldown.add(player, "Dungeons_Mummy_Ingot", 5*20*60);
                break;
        }
    }

    //Diego clickable text code
    @Override
    public void run(Player player, String[] args) {
        PlayerInventory inv = player.getInventory();
        if(args[1].contains("sword")){
            if(!inv.containsAtLeast(BossItems.ZHAMBIE.item(), 1) || !inv.containsAtLeast(BossItems.SKALATAN.item(), 1) || !inv.containsAtLeast(BossItems.HIDEY_SPIDEY.item(), 1))
                throw new CrownException(player, "You need one of each kind of the boss Golden Apples to get the Royal Sword.");
            
            inv.removeItemAnySlot(BossItems.ZHAMBIE.item());
            inv.removeItemAnySlot(BossItems.SKALATAN.item());
            inv.removeItemAnySlot(BossItems.HIDEY_SPIDEY.item());
            
            inv.addItem(CrownItems.BASE_ROYAL_SWORD.clone());
            
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
            for (int i = 0; i <= 5; i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().getX(), player.getLocation().getY()+2, player.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d), i*5L);
            }
            CrownUser user = UserManager.getUser(player);
            if(user.getBranch() == Branch.DEFAULT || user.getBranch() == Branch.ROYALS || !user.hasRank(Rank.KNIGHT)){
                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "You've been promoted to " + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Knight" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " !");
                player.sendMessage(ChatColor.WHITE + "You can select the tag in " + ChatColor.YELLOW + "/rank" + ChatColor.WHITE + " now.");
                user.addRank(Rank.KNIGHT);
            }

            Bukkit.dispatchCommand(getServer().getConsoleSender(), "lp user " + player.getName() + " parent add free-rank");

        } else  if(args[1].contains("trident")){
            if(!inv.contains(BossItems.DRAWNED.item())) throw new CrownException(player, "You need to have the Drawned apple to get the Dolphin Swimmer Trident");
            ItemStack toGive = FORK.clone();
            inv.removeItemAnySlot(BossItems.DRAWNED.item());
            inv.addItem(toGive);
        }
    }

    public static final ItemStack FORK;
    static {
        FORK = new ItemStackBuilder(Material.TRIDENT, 1)
                .setName(Component.text("Fork").decorate(TextDecoration.BOLD))
                .build();
        CrownEnchant.addCrownEnchant(FORK, RoyalEnchants.dolphinSwimmer(), 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        boolean found = false;
        for (ItemStack item : event.getEntity().getInventory().getContents()) {
            if (item == null) continue;
            if (item.getItemMeta().getLore() != null && item.getItemMeta().getLore().contains("Dungeon Item")) {
                found = true;
                if (item.getAmount() >= 10) item.setAmount(item.getAmount() - ThreadLocalRandom.current().nextInt(0, 11));
                else item.setAmount(ThreadLocalRandom.current().nextInt(0, item.getAmount()+1));
            }
        }
        if (found) event.getEntity().sendMessage(ChatColor.RED + "[FTC] " + ChatColor.WHITE + "You lost a random amount of your Dungeon Items...");
    }

    @EventHandler(ignoreCancelled = true)
    public void onMotherKill(EntityDeathEvent event){
        if(event.getEntity().getKiller() != null && event.getEntity() instanceof Spider){
            if (event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() == 75d) {
                Location spawnLoc =  event.getEntity().getLocation();

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    for (int i = 0; i <= 2; i++) {
                        CaveSpider caveSpider = spawnLoc.getWorld().spawn(spawnLoc.add(new Vector(0.2*i*Math.pow(-1, i), i*0.1, 0.2*i*Math.pow(-1, i))), CaveSpider.class);
                        caveSpider.setLootTable(LootTables.EMPTY.getLootTable());
                        caveSpider.setHealth(1);
                    }
                }, 15L);
            }
        }
    }
}

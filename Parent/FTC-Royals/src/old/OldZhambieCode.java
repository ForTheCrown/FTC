@SuppressWarnings("deprecation")
public void summonZhambie(Location loc) {
    loc.setY(loc.getY()+2);
    Husk zhambie = loc.getWorld().spawn(loc, Husk.class);
    zhambie.setBaby(false);
    zhambie.setCustomName(ChatColor.YELLOW + "Zhambie");
    zhambie.setCustomNameVisible(true);
    zhambie.setRemoveWhenFarAway(false);
    zhambie.setPersistent(true);
    zhambies.add(zhambie.getUniqueId());

    zhambie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300);
    zhambie.setHealth(300);
    zhambie.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
    zhambie.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
    zhambie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);
    zhambie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.31F);;

    zhambie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));

    createBossBar(loc);
    zhelpers(loc, zhambie, zhambie.getUniqueId());
}

private void zhelpers(Location loc, Husk zhambie, UUID uuid) {
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
        if (zhambies.contains(uuid) && zhambiehelpers.size() < 3) {
            spawnloc = zhambie.getLocation();
            spawnloc.getWorld().spawnParticle(Particle.FLAME, spawnloc.add(0, 2, 0), 5, 0.1D, 0.4D, 0.1D, 0.01D);
            for (long i = 10L; i < 60L; i = i + 10L) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> spawnloc.getWorld().spawnParticle(Particle.FLAME, spawnloc, 5, 0.1D, 0.4D, 0.1D, 0.01D), i);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Husk zhelper = spawnloc.getWorld().spawn(spawnloc, Husk.class);
                zhelper.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                zhelper.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
                zhelper.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
                zhelper.getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
                zhelper.setBaby(false);
                zhelper.getWorld().playSound(zhelper.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 0.7f, 1.0f);
                zhelper.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8f);
                zhelper.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);
                zhelper.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.32F);;
                zhelper.setRemoveWhenFarAway(false);
                zhelper.setPersistent(true);
                zhambiehelpers.add(zhelper.getUniqueId());
                zhelpers(zhambie.getLocation(), zhambie ,uuid);
            }, 60L);
        }
        else if (zhambies.contains(uuid)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> zhelpers(zhambie.getLocation(), zhambie ,uuid), 60L);
        }
    }, 80L);

}

@EventHandler(ignoreCancelled = true)
public void onMobDeath(EntityDeathEvent event){
    if(event.getEntity() instanceof Husk){
        if(zhambies.contains(event.getEntity().getUniqueId())) {
            zhambies.remove(event.getEntity().getUniqueId());
            event.getDrops().clear();

            ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 1);
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = new ArrayList<>();
            itemMeta.setDisplayName("Zhambie Defeated!");
            itemLore.add("He only wanted to hug you...");
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);

            event.getDrops().add(item);
            if (event.getEntity().getKiller() instanceof Player)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + event.getEntity().getKiller().getName() + " only adventure:zhambie");

            if (bossbar != null) {
                bossbar.removeAll();
                bossbar.setVisible(false);
                bossbar = null;
            }
        }
        else if (zhambiehelpers.contains(event.getEntity().getUniqueId())) {
            zhambiehelpers.remove(event.getEntity().getUniqueId());
            event.getDrops().clear();
        }
    }
}

@EventHandler(ignoreCancelled = true)
public void onHit(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Husk && event.getDamager() instanceof Arrow) {
        Husk husk = (Husk) event.getEntity();
        if (zhambies.contains(husk.getUniqueId())) {
            event.setCancelled(true);
            husk.getWorld().playSound(husk.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
            husk.getWorld().spawnParticle(Particle.SQUID_INK, husk.getLocation().add(0, husk.getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
        }
    }
}

@SuppressWarnings("deprecation")
@EventHandler(ignoreCancelled = true)
public void onZhambieHit(EntityDamageEvent event) {
    if (zhambies.contains(event.getEntity().getUniqueId())) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (bossbar != null) bossbar.setProgress(((Husk) event.getEntity()).getHealth() / ((Husk) event.getEntity()).getMaxHealth());
            }
        }, 2L);
    }
}

public void createBossBar(Location loc) {
    if (bossbar != null) return;
    bossbar = Bukkit.createBossBar(ChatColor.YELLOW + "Zhambie", BarColor.YELLOW, BarStyle.SEGMENTED_12);
    bossbar.setProgress(1.0);

    List<Entity> nearbyEntities = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 30, 80, 30);
    for (Entity ent : nearbyEntities) {
        if (ent instanceof Player) {
            bossbar.addPlayer((Player) ent);
        }
    }
    bossbar.setVisible(true);
}
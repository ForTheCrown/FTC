public void summonHideySpidey(Location loc) {
    Spider spidey = loc.getWorld().spawn(loc.add(new Vector(0, 1.5, 0)), Spider.class);
    spidey.setCustomName("Hidey Spidey");
    spidey.setCustomNameVisible(false);
    spidey.setRemoveWhenFarAway(false);
    spidey.setPersistent(true);
    spideys.add(spidey.getUniqueId());

    Attributable att = (Attributable) spidey;

    AttributeInstance maxHealth = att.getAttribute(Attribute.GENERIC_MAX_HEALTH);
    maxHealth.setBaseValue(300);
    spidey.setHealth(300);

    AttributeInstance followRange = att.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
    followRange.setBaseValue(25);

    AttributeInstance knockBackRes = att.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
    knockBackRes.setBaseValue(1.0f);

    AttributeInstance attackDmg = att.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
    attackDmg.setBaseValue(11);

    AttributeInstance speed = att.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
    speed.setBaseValue(0.28);

    PotionEffect potion = new PotionEffect(PotionEffectType.INVISIBILITY, 9999, 0, false, false);
    potion.apply((LivingEntity) spidey);

    createBossBar(loc);
}

@EventHandler(ignoreCancelled = true)
public void onMobDeath(EntityDeathEvent event){
    if(event.getEntity() instanceof Spider){
        if(spideys.contains(event.getEntity().getUniqueId())) {
            event.getDrops().clear();

            ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 1);
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = new ArrayList<>();
            itemMeta.setDisplayName("Hidey Spidey Defeated!");
            itemLore.add("He can hide but he can't run...");
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);

            event.getDrops().add(item);
            if (event.getEntity().getKiller() instanceof Player)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + event.getEntity().getKiller().getName() + " only adventure:hideyspidey");

            spideys.remove(event.getEntity().getUniqueId());

            if (bossbar != null) {
                bossbar.removeAll();
                bossbar.setVisible(false);
                bossbar = null;
            }
        }
    }
}

@EventHandler(ignoreCancelled = true)
public void onHit(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Spider && event.getDamager() instanceof Arrow) {
        if (spideys.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
            event.getEntity().getWorld().spawnParticle(Particle.SQUID_INK, event.getEntity().getLocation().add(0, event.getEntity().getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
        }
    }
    else if (event.getEntity() instanceof Player && event.getDamager() instanceof Spider) {
        if (spideys.contains(event.getDamager().getUniqueId())) {
            PotionEffect potion = new PotionEffect(PotionEffectType.POISON, 60, 1, true, true);
            potion.apply((LivingEntity) event.getEntity());
        }
    }
    else if (event.getEntity() instanceof Spider && event.getDamager() instanceof Player) {
        if (spideys.contains(event.getEntity().getUniqueId())) {
            putOutFire(event.getEntity());
        }
    }
}

private void putOutFire(Entity entity) {
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        @Override
        public void run() {
            entity.setFireTicks(0);
        }
    }, 1L);

}

@EventHandler(ignoreCancelled = true)
public void fireHit(EntityDamageEvent event) {
    if (event.getEntity() instanceof Spider) {
        if (spideys.contains(event.getEntity().getUniqueId())) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                event.setCancelled(true);
                event.getEntity().setFireTicks(0);
            }
        }
    }
}

@SuppressWarnings("deprecation")
@EventHandler(ignoreCancelled = true)
public void onSpideyHit(EntityDamageEvent event) {
    if (spideys.contains(event.getEntity().getUniqueId())) {
        if (bossbar != null) bossbar.setProgress(((Spider) event.getEntity()).getHealth() / ((Spider) event.getEntity()).getMaxHealth());
    }
}

public void createBossBar(Location loc) {
    if (bossbar != null) return;
    bossbar = Bukkit.createBossBar(ChatColor.YELLOW + "Hidey Spidey", BarColor.YELLOW, BarStyle.SEGMENTED_12);
    bossbar.setProgress(1.0);

    List<Entity> nearbyEntities = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 30, 80, 30);
    for (Entity ent : nearbyEntities) {
        if (ent instanceof Player) {
            bossbar.addPlayer((Player) ent);
        }
    }
    bossbar.setVisible(true);
}
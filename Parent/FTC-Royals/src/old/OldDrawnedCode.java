@SuppressWarnings("deprecation")
    public void summonDrawned(Location loc) {
        loc.setY(loc.getY()+8);
        Drowned drawned = loc.getWorld().spawn(loc, Drowned.class);
        drawned.getEquipment().setItemInMainHand(new ItemStack(Material.TRIDENT));
        drawned.setBaby(false);
        drawned.setCustomName(ChatColor.YELLOW + "Drawned");
        drawned.setCustomNameVisible(true);
        drawned.setRemoveWhenFarAway(false);
        drawned.setPersistent(true);
        drawneds.add(drawned.getUniqueId());

        drawned.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(400);
        drawned.setHealth(400);
        drawned.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(50.0);
        drawned.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
        drawned.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.32F);

        createBossBar(loc);
        startCycle(drawned.getLocation(), drawned, drawned.getUniqueId());
        guardians.clear();
    }



    private void startCycle(Location loc, Drowned drawned, UUID uuid) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (drawneds.contains(uuid)) {
                drawned.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 1));

                // Sounds
                for(int i = 1; i < 6; i++) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if (drawneds.contains(uuid)) {
                            loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, drawned.getLocation().getX(), drawned.getLocation().getY()+1.5, drawned.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 1);
                            loc.getWorld().playSound(drawned.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 2f, 0.8f);
                        }
                    }, i*8L);

                }

                // Lightning Strike
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (drawneds.contains(uuid)) {
                        loc.getWorld().playSound(drawned.getLocation(), Sound.ITEM_TRIDENT_THUNDER, SoundCategory.MASTER, 2f, 1.0f);

                        // Remove dead guardians
                        Set<UUID> temp = new HashSet<UUID>();
                        for (UUID id : guardians)
                            temp.add(id);
                        for (UUID id : temp) {
                            if (Bukkit.getEntity(id) == null || Bukkit.getEntity(id).isDead())
                                guardians.remove(id);
                        }
                        temp.clear();

                        if (guardians.size() < 3) {
                            for (int i = 0; i < 4; i++) {
                                Guardian guardian = loc.getWorld().spawn(drawned.getLocation().add(0, 2.5, 0), Guardian.class);
                                guardian.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
                                guardians.add(guardian.getUniqueId());
                            }
                        }

                        // Make all invulnerable
                        drawned.setInvulnerable(true);
                        for (Entity e : drawned.getNearbyEntities(3, 5, 3)) {
                            if (e.getType() == EntityType.DROPPED_ITEM || e.getType() == EntityType.GUARDIAN) {
                                e.setInvulnerable(true);
                            }
                        }


                        loc.getWorld().strikeLightning(drawned.getLocation()); //strike

                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            // Make all vulnerable
                            drawned.setInvulnerable(false);
                            for (Entity e : drawned.getNearbyEntities(3, 5, 3)) {
                                if (e.getType() == EntityType.DROPPED_ITEM || e.getType() == EntityType.GUARDIAN) {
                                    e.setInvulnerable(false);
                                }
                            }
                            for (UUID id : guardians) {
                                Bukkit.getEntity(id).setInvulnerable(false);
                            }
                        }, 20L);
                    }
                }, 52L);

                startCycle(drawned.getLocation(), drawned, uuid);
            }
        }, 600L);
    }


    @EventHandler(ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event){
        if (event.getEntity() instanceof Drowned) {
            if(drawneds.contains(event.getEntity().getUniqueId())) {
                drawneds.remove(event.getEntity().getUniqueId());
                event.getDrops().clear();

                ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 1);
                ItemMeta itemMeta = item.getItemMeta();
                List<String> itemLore = new ArrayList<>();
                itemMeta.setDisplayName("Drawned Defeated!");
                itemLore.add("Never too late to learn how to swim...");
                itemMeta.setLore(itemLore);
                item.setItemMeta(itemMeta);
                event.getDrops().add(item);

                for (UUID id : guardians) {
                    try {
                        ((Guardian) Bukkit.getEntity(id)).setHealth(1);
                    } catch (Exception ignored) {}
                }

                if (bossbar != null) {
                    bossbar.removeAll();
                    bossbar.setVisible(false);
                    bossbar = null;
                }
            }
        }
        else if (event.getEntity() instanceof Guardian) {
            if (guardians.contains(event.getEntity().getUniqueId())) {
                guardians.remove(event.getEntity().getUniqueId());
                event.getDrops().clear();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTridentHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Player && event.getEntity() instanceof Trident) {
            if (event.getEntity().getShooter() instanceof Drowned) {
                Drowned drawned = (Drowned) event.getEntity().getShooter();
                if (drawneds.contains(drawned.getUniqueId())) {
                    try {
                        if (((Player) event.getHitEntity()).getAbsorptionAmount() == 0)
                            ((Player) event.getHitEntity()).setHealth(((Player) event.getHitEntity()).getHealth() - 4);
                        else {
                            try {
                                ((Player) event.getHitEntity()).setAbsorptionAmount(((Player) event.getHitEntity()).getAbsorptionAmount() - 4);
                            } catch (IllegalArgumentException e) {
                                ((Player) event.getHitEntity()).setAbsorptionAmount(0);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        ((Player) event.getHitEntity()).setHealth(0);
                    }
                    ((Player) event.getHitEntity()).setCooldown(((Player) event.getHitEntity()).getInventory().getItemInMainHand().getType(), 100);
                    event.getHitEntity().setLastDamageCause(new EntityDamageEvent(event.getHitEntity(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, 4.0));
                    if (((Player) event.getHitEntity()).isBlocking()) {
                        ((Player) event.getHitEntity()).playSound(event.getHitEntity().getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
                        ((Player) event.getHitEntity()).setCooldown(Material.SHIELD, 100);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onDrawnedHit(EntityDamageEvent event) {
        if (drawneds.contains(event.getEntity().getUniqueId())) {
            if (bossbar != null) bossbar.setProgress(((Drowned) event.getEntity()).getHealth() / ((Drowned) event.getEntity()).getMaxHealth());
        }
    }

    public void createBossBar(Location loc) {
        if (bossbar != null) return;
        bossbar = Bukkit.createBossBar(ChatColor.YELLOW + "Drawned", BarColor.YELLOW, BarStyle.SEGMENTED_12);
        bossbar.setProgress(1.0);

        List<Entity> nearbyEntities = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 30, 80, 30);
        for (Entity ent : nearbyEntities) {
            if (ent instanceof Player) {
                bossbar.addPlayer((Player) ent);
            }
        }
        bossbar.setVisible(true);
    }
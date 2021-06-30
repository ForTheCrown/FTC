package net.forthecrown.cosmetics;

import org.bukkit.event.Listener;

public class CosmeticEvents implements Listener {

    /*@EventHandler
    public void onPlayerClickItemInInv(InventoryClickEvent event) throws CannotAffordTransactionException {
        if(!(event.getInventory().getHolder() instanceof CustomMenu)) return;
        if(event.isShiftClick()) event.setCancelled(true);
        if (event.getClickedInventory() instanceof PlayerInventory) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        CrownUser user = UserManager.getUser(player);
        int slot = event.getSlot();
        String title = ChatUtils.getString(event.getView().title());

        if (title.contains("osmetics")) {
            switch (slot) {
                case 20:
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    ArrowParticleMenu apm = new ArrowParticleMenu(user);
                    player.openInventory(apm.getInv());
                    break;
                case 22:
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    EmoteMenu em = new EmoteMenu(user);
                    player.openInventory(em.getInv());
                    break;
                case 24:
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    DeathParticleMenu dpm = new DeathParticleMenu(user);
                    player.openInventory(dpm.getInv());
                    break;
                case 40:
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    user.setAllowsRidingPlayers(!user.allowsRidingPlayers());
                    player.openInventory(Cosmetics.getPlugin().getMainCosmeticInventory(user));
            }
        }

        else if (title.contains("Arrow Effects")) {
            ArrowParticleMenu apm = new ArrowParticleMenu(user);

            int gemCost = 0;
            try {
                gemCost = Integer.parseInt(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getLore().get(2)).replaceAll("[\\D]", ""));
            } catch (Exception ignored){ }


            switch (slot) {
                case 4:
                    player.openInventory(Cosmetics.getPlugin().getMainCosmeticInventory(user));
                    break;
                case 10:
                    doArrowParticleStuff(Particle.FLAME, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 11:
                    doArrowParticleStuff(Particle.SNOWBALL, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 12:
                    doArrowParticleStuff(Particle.SNEEZE, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 13:
                    doArrowParticleStuff(Particle.HEART, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 14:
                    doArrowParticleStuff(Particle.DAMAGE_INDICATOR, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 15:
                    doArrowParticleStuff(Particle.DRIPPING_HONEY, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 16:
                    doArrowParticleStuff(Particle.CAMPFIRE_COSY_SMOKE, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 19:
                    doArrowParticleStuff(Particle.SOUL, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 20:
                    doArrowParticleStuff(Particle.FIREWORKS_SPARK, player, user, gemCost);
                    player.openInventory(apm.getInv());
                    break;
                case 31:
                    user.setArrowParticle(null);
                    player.openInventory(apm.getInv());
                    break;

                default: return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
        }

        else if (title.contains("Death Effects")) {
            DeathParticleMenu dpm = new DeathParticleMenu(user);

            int gemCost = 0;
            try {
                gemCost = Integer.parseInt(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getLore().get(2)).replaceAll("[\\D]", ""));
            } catch (Exception ignored){ }

            switch (slot) {
                case 4:
                    player.openInventory(Cosmetics.getPlugin().getMainCosmeticInventory(user));
                    break;
                case 10:
                    doDeathParticleStuff("SOUL", player, user, gemCost);
                    player.openInventory(dpm.getInv());
                    break;
                case 11:
                    doDeathParticleStuff("TOTEM", player, user, gemCost);
                    player.openInventory(dpm.getInv());
                    break;
                case 12:
                    doDeathParticleStuff("EXPLOSION", player, user, gemCost);
                    player.openInventory(dpm.getInv());
                    break;
                case 13:
                    doDeathParticleStuff("ENDER_RING", player, user, gemCost);
                    player.openInventory(dpm.getInv());
                    break;
                case 31:
                    user.setDeathParticle("none");
                    player.openInventory(dpm.getInv());
                    break;
                default:
                    return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
        }

        else if (title.contains("Emotes")) {
            EmoteMenu em = new EmoteMenu(user);

            switch(slot) {
                case 4:
                    player.openInventory(Cosmetics.getPlugin().getMainCosmeticInventory(user));
                    break;
                case 31:
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "sudo " + player.getName() + " toggleemotes");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Cosmetics.getPlugin(), () -> player.openInventory(em.getInv()), 3);
                    break;
                default:
                    return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
        }
    }*/
}

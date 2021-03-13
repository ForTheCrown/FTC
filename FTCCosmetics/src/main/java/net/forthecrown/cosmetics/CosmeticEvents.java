package net.forthecrown.cosmetics;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.exceptions.CannotAffordTransaction;
import net.forthecrown.cosmetics.inventories.ArrowParticleMenu;
import net.forthecrown.cosmetics.inventories.CustomInventory;
import net.forthecrown.cosmetics.inventories.DeathParticleMenu;
import net.forthecrown.cosmetics.inventories.EmoteMenu;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class CosmeticEvents implements Listener {

    private final Cosmetics main;
    CosmeticEvents(Cosmetics main){
        this.main = main;
    }

    private void doArrowParticleStuff(Particle particle, Player player, CrownUser user, int gemCost){
        if(!user.getParticleArrowAvailable().contains(particle)){
            if(user.getGems() < gemCost) throw new CannotAffordTransaction(player);
            user.addGems(-gemCost);

            List<Particle> set = user.getParticleArrowAvailable();
            set.add(particle);
            user.setParticleArrowAvailable(set);
        }
        user.setArrowParticle(particle);
    }

    private void doDeathParticleStuff(String effect, Player player, CrownUser user, int gemCost){
        if(!user.getParticleDeathAvailable().contains(effect)){
            if(user.getGems() < gemCost) throw new CannotAffordTransaction(player);
            user.addGems(-gemCost);

            List<String> asd = user.getParticleDeathAvailable();
            asd.add(effect);
            user.setParticleDeathAvailable(asd);
        }
        user.setDeathParticle(effect);
    }

    @EventHandler
    public void onPlayerClickItemInInv(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof CustomInventory) {
            if (event.getClickedInventory() instanceof PlayerInventory && !event.isShiftClick()) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            CrownUser user = FtcCore.getUser(player);
            int slot = event.getSlot();
            String title = event.getView().getTitle();

            if (title.contains("osmetics"))
            {
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
                        player.openInventory(main.getMainCosmeticInventory(user));
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
                        player.openInventory(main.getMainCosmeticInventory(user));
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
                        player.openInventory(main.getMainCosmeticInventory(user));
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
                        player.openInventory(main.getMainCosmeticInventory(user));
                        break;
                    case 31:
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "sudo " + player.getName() + " toggleemotes");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> player.openInventory(em.getInv()), 3);
                        break;
                    default:
                        return;
                }

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            }
        }
    }
}

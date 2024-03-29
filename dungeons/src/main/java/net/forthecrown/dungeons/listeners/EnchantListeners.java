package net.forthecrown.dungeons.listeners;

import com.destroystokyo.paper.ParticleBuilder;
import com.destroystokyo.paper.event.inventory.PrepareGrindstoneEvent;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.forthecrown.dungeons.enchantments.DungeonEnchantments;
import net.forthecrown.enchantment.FtcEnchant;
import net.forthecrown.enchantment.FtcEnchants;
import net.forthecrown.text.Text;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class EnchantListeners implements Listener {

  @EventHandler
  public void onPrepareAnvil(PrepareAnvilEvent event) {
    if (ItemStacks.isEmpty(event.getResult())) {
      return;
    }

    AnvilInventory anvil = event.getInventory();

    ItemStack first = anvil.getFirstItem();
    ItemStack second = anvil.getSecondItem();
    ItemStack result = event.getResult();

    if (ItemStacks.isEmpty(first) || ItemStacks.isEmpty(second)) {
      return;
    }

    var resMeta = result.getItemMeta();
    Mutable<Boolean> pointer = new MutableBoolean();

    addEnchants(resMeta, first, second, pointer);
    //addEnchants(resMeta, second, first, pointer);

    if (pointer.getValue()) {
      event.setResult(null);
      return;
    }

    result.setItemMeta(resMeta);
    event.setResult(result);
  }

  private void addEnchants(ItemMeta resultMeta,
                           ItemStack first,
                           ItemStack second,
                           Mutable<Boolean> shouldCancel
  ) {
    Map<Enchantment, Integer> enchants;
    var meta = second.getItemMeta();

    if (meta instanceof EnchantmentStorageMeta me) {
      enchants = me.getStoredEnchants();
    } else {
      enchants = meta.getEnchants();
    }

    boolean combiningEnchants = first.getType() == second.getType();
    boolean book = second.getType() == Material.ENCHANTED_BOOK;

    for (var e : enchants.entrySet()) {
      if (!(e.getKey() instanceof FtcEnchant enchant)) {
        continue;
      }

      if (combiningEnchants) {
        if (!book && !enchant.canEnchantItem(first)) {
          if (enchants.size() == 1) {
            shouldCancel.setValue(true);
            return;
          }

          continue;
        }
      } else if (!enchant.canEnchantItem(first)) {
        if (enchants.size() == 1) {
          shouldCancel.setValue(true);
          return;
        }

        continue;
      }

      int level = e.getValue();
      FtcEnchants.addEnchant(resultMeta, enchant, level);
    }
  }

  @SuppressWarnings("deprecation") // The non-deprecated event didn't function
  @EventHandler(ignoreCancelled = true)
  public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
    if (ItemStacks.isEmpty(event.getResult())) {
      return;
    }

    List<ObjectIntPair<FtcEnchant>> removing = new ArrayList<>();

    var lower = event.getInventory().getLowerItem();
    var upper = event.getInventory().getUpperItem();

    if (ItemStacks.notEmpty(lower)) {
      count(removing, lower);
    }

    if (ItemStacks.notEmpty(upper)) {
      count(removing, upper);
    }

    if (removing.isEmpty()) {
      return;
    }

    var result = event.getResult();
    var meta = result.getItemMeta();

    var lore = meta.lore();

    if (lore == null || lore.isEmpty()) {
      return;
    }

    List<Component> finalLore = new ArrayList<>(lore);

    removing.stream()
        .map(pair -> pair.left().displayName(pair.rightInt()))
        .map(Text::plain)
        .forEach(plain -> {
          finalLore.removeIf(component1 -> {
            String lorePlain = Text.plain(component1);

            return lorePlain.contains(plain)
                || plain.contains(lorePlain);
          });
        });

    meta.lore(finalLore);
    result.setItemMeta(meta);
    event.setResult(result);
  }

  private void count(List<ObjectIntPair<FtcEnchant>> enchants, ItemStack item) {
    for (var e: item.getEnchantments().entrySet()) {
      if (!(e.getKey() instanceof FtcEnchant enc)) {
        continue;
      }

      enchants.add(ObjectIntPair.of(enc, e.getValue()));
    }
  }

  // How else do I detect if the player is on the ground? I'm not doing
  // it manually, f you
  @SuppressWarnings("deprecation")
  @EventHandler(ignoreCancelled = true)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)
        || event.getCause() == EntityDamageEvent.DamageCause.THORNS
    ) {
      return;
    }

    if (!hasEnchant(player.getInventory().getItemInMainHand(), DungeonEnchantments.POISON_CRIT)) {
      return;
    }

    if (event.getEntity() instanceof Player dmgPlayer && dmgPlayer.isBlocking()) {
      return;
    }

    if (!(player.getFallDistance() > 0.0F
        && !player.isOnGround()
        && !player.hasPotionEffect(PotionEffectType.BLINDNESS)
        && event.getEntity() instanceof LivingEntity hitEntity
    )) {
      return;
    }

    final boolean monster = hitEntity instanceof Monster;

    hitEntity.addPotionEffect(new PotionEffect(
        monster ? PotionEffectType.WITHER : PotionEffectType.POISON,
        monster ? 45 : 35,
        monster ? 2 : 1,
        false,
        false
    ));

    new ParticleBuilder(Particle.REDSTONE)
        .location(
            hitEntity.getLocation()
                .add(0, hitEntity.getHeight() / 2, 0)
        )
        .data(new Particle.DustOptions(
            Color.fromRGB(31, 135, 62),
            1
        ))
        .count(10)
        .offset(0.2D, 0.2D, 0.2D)
        .spawn();

    hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_SPIDER_HURT, 0.2f, 0.7f);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamageEntEvent(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    if (!player.isBlocking() && event.getFinalDamage() != 0) {
      return;
    }

    var inv = player.getInventory();
    if (!(hasEnchant(inv.getItemInOffHand(), DungeonEnchantments.HEALING_BLOCK)
        || hasEnchant(inv.getItemInMainHand(), DungeonEnchantments.HEALING_BLOCK))
    ) {
      return;
    }

    if (player.hasCooldown(Material.SHIELD)) {
      return;
    }

    var maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
    assert maxHealthAttr != null;

    double maxHealth = maxHealthAttr.getValue();

    player.setHealth(Math.min(player.getHealth() + 2, maxHealth));
    player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.6f, 1f);
    player.setCooldown(Material.SHIELD, 2 * 40);
  }

  public static boolean hasEnchant(@Nullable ItemStack item, FtcEnchant enchant) {
    if (ItemStacks.isEmpty(item)) {
      return false;
    }

    var map = item.getEnchantments();
    return map.containsKey(enchant);
  }

  @EventHandler
  public void onActivate(PlayerInteractEvent event) {
    if (event.getAction() != Action.LEFT_CLICK_AIR
        && event.getAction() != Action.LEFT_CLICK_BLOCK
    ) {
      return;
    }

    var player = event.getPlayer();

    if (!player.isSwimming()) {
      return;
    }

    var item = player.getInventory().getItemInMainHand();

    if (ItemStacks.isEmpty(item)) {
      return;
    }

    var map = item.getEnchantments();

    if (!map.containsKey(DungeonEnchantments.DOLPHIN_SWIMMER)) {
      return;
    }

    if (player.getGameMode() == GameMode.SURVIVAL
        || player.getGameMode() == GameMode.ADVENTURE
    ) {
      Damageable dmg = ((Damageable) item.getItemMeta());

      // If we're just below the amount that
      // would destroy the item, then stop
      if (dmg.getDamage() >= 249) {
        return;
      }

      dmg.setDamage(dmg.getDamage() + 2);
      item.setItemMeta(dmg);
    }

    player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 120, 1));
    player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1f, 1.5f);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityShootBow(EntityShootBowEvent event) {
    if (!hasEnchant(event.getBow(), DungeonEnchantments.STRONG_AIM)) {
      return;
    }

    if (event.getForce() != 1) {
      return;
    }

    if (!(event.getProjectile() instanceof Arrow)) {
      return;
    }

    Tasks.runTimer(new StrongAimScheduler((Arrow) event.getProjectile()), 3, 3);
  }

  private record StrongAimScheduler(Arrow arrow) implements Consumer<BukkitTask> {

    @Override
    public void accept(BukkitTask task) {
      arrow.setVelocity(arrow.getVelocity().add(new Vector(0, 0.075, 0)));

      if (arrow.isDead() || arrow.isOnGround()) {
        Tasks.cancel(task);
      }
    }
  }
}
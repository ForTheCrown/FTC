package net.forthecrown.dungeons.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.text.Text;
import net.forthecrown.utils.EntityRef;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.spigotmc.SpigotConfig;

public class PunchingBags implements Listener {

  public static final NamespacedKey DATA_CONTAINER_KEY = DungeonUtils.royalsKey("dummy");
  public static final String SCOREBOARD_TAG = "punching_bag";

  private static final Map<UUID, PunchingBagInstance> punchingBags = new HashMap<>();

  public static final Component DUMMY_NAME = Component.text("Hit me!", NamedTextColor.GOLD);

  public static boolean isPunchingBag(Entity entity) {
    if (entity.getScoreboardTags().contains(SCOREBOARD_TAG)) {
      return true;
    }

    return entity.getPersistentDataContainer().has(DATA_CONTAINER_KEY);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    if (!isPunchingBag(event.getEntity())) {
      return;
    }

    LivingEntity dummy = (LivingEntity) event.getEntity();

    if (event.getCause() == DamageCause.VOID || event.getFinalDamage() >= dummy.getHealth()) {
      return;
    }

    PunchingBagInstance punchingBag = punchingBags.computeIfAbsent(
        dummy.getUniqueId(),
        uuid -> new PunchingBagInstance()
    );

    punchingBag.hit(dummy, event.getFinalDamage());
    event.setDamage(0d);
  }

  private static class PunchingBagInstance {

    private BukkitTask nameResetTask;
    private EntityRef entity;

    void hit(LivingEntity entity, double dmg) {
      Component name = Text.format("Damage: {0, number}", NamedTextColor.RED, dmg);
      entity.customName(name);
      entity.setCustomNameVisible(true);

      this.entity = EntityRef.of(entity);
      pushbackTask();
    }

    void pushbackTask() {
      Tasks.cancel(nameResetTask);

      nameResetTask = Tasks.runLater(() -> {
        LivingEntity entity = (LivingEntity) this.entity.get();

        if (entity == null) {
          return;
        }

        entity.customName(DUMMY_NAME);
        entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
      }, 5 * 20);
    }
  }

  public static void spawnDummy(Location location) {
    location.getWorld().spawn(location, Husk.class, zomzom -> {
      zomzom.getEquipment().setHelmet(new ItemStack(Material.HAY_BLOCK));
      zomzom.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
      zomzom.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
      zomzom.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

      zomzom.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(SpigotConfig.maxHealth);
      zomzom.setHealth(SpigotConfig.maxHealth);

      zomzom.setAI(false);
      zomzom.stopSound(SoundStop.all());
      zomzom.setGravity(false);

      zomzom.setCustomNameVisible(true);
      zomzom.customName(DUMMY_NAME);

      zomzom.setRemoveWhenFarAway(false);
      zomzom.setPersistent(true);
      zomzom.setCanPickupItems(false);
      zomzom.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
      zomzom.addScoreboardTag(SCOREBOARD_TAG);
    });
  }
}
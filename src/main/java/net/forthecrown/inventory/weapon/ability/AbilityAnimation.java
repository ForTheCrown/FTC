package net.forthecrown.inventory.weapon.ability;

import static net.forthecrown.inventory.weapon.SwordConfig.swordAnim_initialDistance;

import io.papermc.paper.entity.LookAnchor;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnDisable;
import net.forthecrown.cosmetics.travel.TravelUtil;
import net.forthecrown.inventory.weapon.SwordConfig;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.Usables;
import net.forthecrown.useables.actions.ActionScript;
import net.forthecrown.useables.test.TestScript;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.Keybinds;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.math.imaginary.Quaterniond;
import org.spongepowered.math.vector.Vector2f;
import org.spongepowered.math.vector.Vector3d;

public class AbilityAnimation {
  private static final Logger LOGGER = Loggers.getLogger();

  public static final float PARTICLE_Y_OFFSET = 1.25F;
  public static final int CIRCLE_POINTS = 4;

  @Getter
  private static final AbilityAnimation instance = new AbilityAnimation();

  @Getter
  @Setter
  private Animation ongoing;

  private AbilityAnimation() {

  }

  public boolean hasOngoing() {
    return ongoing != null && !ongoing.itemTaken;
  }

  public void start(ItemStack sword,
                    Location location,
                    User user
  ) {
    if (ongoing != null && !ongoing.isItemTaken()) {
      ongoing.stash();
      ongoing.stop();
      ongoing = null;
    }

    Animation animation = new Animation(sword, user.getUniqueId());
    animation.setPhase(Phase.STARTING);
    animation.setLocation(location.clone());
    animation.itemTaken = false;

    var points = TravelUtil.getCirclePoints(
        PARTICLE_Y_OFFSET,
        swordAnim_initialDistance,
        CIRCLE_POINTS
    );

    animation.particles = points.toArray(Vector3d[]::new);

    user.getPlayer().lookAt(
        location.clone().add(0, PARTICLE_Y_OFFSET, 0),
        LookAnchor.EYES
    );

    user.getPlayer().closeInventory(Reason.CANT_USE);
    location.setYaw(-90F);
    location.setPitch(0F);

    animation.swordHolder = spawnStand(
        location.clone().subtract(0, PARTICLE_Y_OFFSET - 0.2, 0),
        sword
    );

    animation.task = Tasks.runTimer(animation, 1, 1);
    ongoing = animation;
  }

  private static ArmorStand spawnStand(Location l, ItemStack helmet) {
    return l.getWorld().spawn(l, ArmorStand.class, stand -> {
      stand.setMarker(true);
      stand.setInvisible(true);
      stand.setBasePlate(false);
      stand.setInvulnerable(true);
      stand.setGravity(false);
      stand.setCanTick(false);

      var equipment = stand.getEquipment();
      equipment.setHelmet(helmet);
    });
  }

  @OnDisable
  public void onDisable() {
    if (ongoing == null || ongoing.itemTaken) {
      return;
    }

    ongoing.stash();
  }

  @Getter
  @Setter
  @RequiredArgsConstructor
  public class Animation implements Runnable {
    private final ItemStack sword;
    private final UUID owner;

    private Phase phase = Phase.STARTING;
    private int tick = 0;
    private BukkitTask task;
    private BukkitTask stashTask;

    Location location;
    Vector2f center;

    ArmorStand swordHolder;
    Vector3d[] particles;

    boolean itemTaken = false;

    @Override
    public void run() {
      ++tick;
      phase.onTick(this);

      if (tick < phase.length) {
        return;
      }

      if (phase != Phase.FINISHED) {
        setPhase(phase.next());
      }
    }

    public void stop() {
      tick = 0;
      task = Tasks.cancel(task);
      stashTask = Tasks.cancel(stashTask);
    }

    public void setPhase(Phase phase) {
      this.phase = phase;
      tick = 0;
    }

    public void setLocation(Location location) {
      this.location = location;
      this.center = Vector2f.from(
          (float) location.getX(),
          (float) location.getZ()
      );
    }

    void stash() {
      if (itemTaken) {
        return;
      }

      var dump = SwordConfig.swordPostAnimDump;

      if (dump == null) {
        LOGGER.error(
            "Cannot store ability animation sword! "
                + "No dump container set in config... dropping"
        );

        dropSword();
        return;
      }

      var block = Vectors.getBlock(dump, Worlds.overworld());
      var state = block.getState();

      if (!(state instanceof Container holder)) {
        LOGGER.error(
            "Cannot store ability animation sword in container! "
                + "{} is not a container... dropping item",
            dump
        );

        dropSword();
        return;
      }

      var inv = holder.getInventory();
      if (inv.firstEmpty() == -1) {
        LOGGER.error(
            "Cannot store ability animation sword "
                + "in container! Inventory full, pos={}... dropping",
            dump
        );

        dropSword();
        return;
      }

      inv.addItem(getSword());
      holder.update();
    }

    void dropSword() {
      // Offset by 2 blocks, so it doesn't fall in the lava lol
      location.getWorld()
          .dropItem(location.clone().add(2, 0, 0), sword);
    }

    void rotateIngredientHolders(int rotationOffset, boolean converge) {
      var center = getCenter();
      float progress = ((float) 1) / this.phase.getLength();

      Quaterniond quaternion = Quaterniond.fromAxesAnglesRad(
          0F, rotationOffset + getTick(), 0F
      );

      for (int i = 0; i < particles.length; i++) {
        var pos = particles[i];

        if (converge) {
          pos = pos.mul(1F - progress);
        }

        pos = quaternion.rotate(pos);
        var loc = new Location(
            swordHolder.getWorld(),

            pos.x() + center.x(),
            pos.y() + location.getY(),
            pos.z() + center.y()
        );

        particles[i] = pos;

        Particle.END_ROD.builder()
            .extra(0D)
            .location(loc)
            .allPlayers()
            .spawn();
      }
    }
  }

  @RequiredArgsConstructor
  public enum Phase {
    STARTING (10) {
      @Override
      public void onTick(Animation animation) {

      }
    },

    SPINNING (40) {
      @Override
      public void onTick(Animation animation) {
        animation.rotateIngredientHolders(0, false);
      }
    },

    CONVERGING (60) {
      @Override
      public void onTick(Animation animation) {
        animation.rotateIngredientHolders(SPINNING.length, true);
      }
    },

    COMPLETION(20) {
      @Override
      public void onTick(Animation animation) {
        // Only run once
        if (animation.getTick() > 1) {
          return;
        }

        var l = animation.getLocation();
        l.getWorld().strikeLightningEffect(l);
      }
    },

    FINISHED (-1) {
      @Override
      public void onTick(Animation animation) {
        var holder = animation.getSwordHolder();
        holder.setMarker(false);
        holder.setCustomNameVisible(true);
        holder.setCanTick(true);
        holder.addEquipmentLock(
            EquipmentSlot.HEAD,
            LockType.REMOVING_OR_CHANGING
        );

        var loc = holder.getLocation();
        loc.add(1, 0.25, 0);
        holder.teleport(loc);

        holder.customName(
            Text.format("{0} to take sword",
                NamedTextColor.YELLOW,
                Component.keybind(Keybinds.USE)
            )
        );

        UsableEntity entity = Usables.getInstance().createEntity(holder);
        entity.setCancelVanilla(true);
        entity.setSilent(false);

        var action = new ActionScript(SwordConfig.swordAnim_claimScript);
        entity.getActions().add(action);

        var check = new TestScript(SwordConfig.swordAnim_claimTest);
        entity.getChecks().add(check);

        animation.stop();
        animation.itemTaken = false;

        animation.stashTask = Tasks.runLater(
            animation::stash,
            Time.millisToTicks(TimeUnit.MINUTES.toMillis(5))
        );
      }
    };

    @Getter
    private final int length;

    public Phase next() {
      if (this == FINISHED) {
        return null;
      }

      return values()[ordinal() + 1];
    }

    public abstract void onTick(Animation animation);
  }
}
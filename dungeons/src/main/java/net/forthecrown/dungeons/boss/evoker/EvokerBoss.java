package net.forthecrown.dungeons.boss.evoker;

import static net.kyori.adventure.text.Component.text;

import io.papermc.paper.event.entity.EntityMoveEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Worlds;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.KeyedBossImpl;
import net.forthecrown.dungeons.boss.SingleEntityBoss;
import net.forthecrown.dungeons.boss.SpawnTest;
import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.dungeons.boss.components.EmptyRoomComponent;
import net.forthecrown.dungeons.boss.components.InsideRoomComponent;
import net.forthecrown.dungeons.boss.components.TargetUpdateComponent;
import net.forthecrown.dungeons.boss.evoker.phases.AttackPhase;
import net.forthecrown.dungeons.boss.evoker.phases.AttackPhases;
import net.forthecrown.dungeons.boss.evoker.phases.GhastPhase;
import net.forthecrown.dungeons.boss.evoker.phases.SummonPhase;
import net.forthecrown.dungeons.boss.evoker.phases.SwarmPhase;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.user.Users;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.text.Text;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3d;

public class EvokerBoss extends KeyedBossImpl implements SingleEntityBoss {

  public static final int NO_TRANSITION = -1;

  private Evoker evoker;
  private final TickSequence spawnAnim;
  private final TickSequence deathAnim;

  AttackPhase[] attackOrder;
  int phaseIndex;
  int transitionTick;

  @Getter
  @Setter
  boolean attackingAllowed;

  @Getter
  @Setter
  boolean invulnerable;

  @Getter
  @Setter
  Spellcaster.Spell spell;

  @Getter
  private BossBar phaseBar;

  AttackPhase phase;
  AttackPhase lastPhase;

  EvokerState state;

  public EvokerBoss() {
    super("Emo",
        new Location(Worlds.voidWorld(), -277.5 + 202, 37 - 48, 44.5 - 49),
        DungeonAreas.EVOKER_ROOM,

        SpawnTest.items(
            ItemStacks.fromNbtString("{Count:1b,dataVersion:3218,id:\"minecraft:glowstone_dust\",tag:{display:{Lore:['{\"text\":\"Some sparkles never hurt\",\"italic\":false}'],Name:'{\"text\":\"Magic Dust\",\"color\":\"gold\",\"italic\":false}'}}}"),
            ItemStacks.fromNbtString("{Count:1b,dataVersion:3218,id:\"minecraft:gunpowder\",tag:{display:{Lore:['{\"text\":\"Kill some mobs in the dungeon.\",\"italic\":false}'],Name:'{\"text\":\"Evoker Dust\",\"color\":\"gray\",\"italic\":false}'}}}")
        )
    );

    spawnAnim = EvokerSequences.createSummoning(this);
    deathAnim = EvokerSequences.createDeath(this);
  }

  @Override
  protected void createComponents(Set<BossComponent> c) {
    c.add(TargetUpdateComponent.create());
    c.add(InsideRoomComponent.create());
    c.add(EmptyRoomComponent.create(this));
  }

  @Override
  public void spawn() {
    if (isAlive()) {
      return;
    }

    currentContext = BossContext.create(getRoom());
    Validate.isTrue(!currentContext.players().isEmpty(),
        "Context players is empty, cannot spawn. Spawner in creative?");
    logSpawn(currentContext);

    spawnAnim.start();

    state = EvokerState.SPAWNING;
  }

  void onSummoningFinish() {
    registerEvents();
    startTickTask();

    createPhaseOrder();
    phaseIndex = 0;
    transitionTick = NO_TRANSITION;
    attackingAllowed = false;
    spell = null;
    invulnerable = false;

    phaseBar = Bukkit.createBossBar("Phase Progress", BarColor.BLUE, BarStyle.SOLID);

    createBossBar(currentContext);

    evoker = createEvoker(currentContext);
    evoker.getPersistentDataContainer().set(Bosses.BOSS_TAG, PersistentDataType.STRING, getKey());
    evoker.setLootTable(LootTables.EMPTY.getLootTable());

    Location l = evoker.getLocation();
    EvokerEffects.impactEffect(getWorld(), Vectors.doubleFrom(l));

    runComponents(component -> component.onSpawn(this, currentContext));

    state = EvokerState.ALIVE;
  }

  @Override
  public void kill(boolean force) {
    if (!isAlive()) {
      return;
    }

    unregisterEvents();
    destroyBossBar();
    stopTickTask();

    if (!force) {
      Location l = evoker.getLocation();
      Vector3d pos = Vectors.doubleFrom(l);
      EvokerEffects.shockwave(getWorld(), pos);

      finalizeKill(currentContext);

      getWorld().playSound(
          Sound.sound(org.bukkit.Sound.ENTITY_ENDERMAN_DEATH, Sound.Source.MASTER, 1, 2),
          l.getX(), l.getY(), l.getZ()
      );
    } else {
      deathAnim.stop();
      spawnAnim.stop();
    }

    SummonPhase.killAllSpawned();
    SwarmPhase .killAllSpawned();
    GhastPhase .killAllSpawned();

    runComponents(component -> component.onDeath(this, currentContext, force));
    setPhase(null);

    evoker.remove();
    evoker = null;
    currentContext = null;
    attackOrder = null;
    phaseIndex = 0;
    transitionTick = NO_TRANSITION;
    attackingAllowed = false;
    spell = null;

    phaseBar.setVisible(false);
    phaseBar.removeAll();
    phaseBar = null;

    state = EvokerState.NOT_ALIVE;
  }

  @Override
  public @Nullable Evoker getBossEntity() {
    return evoker;
  }

  @Override
  protected void tick() {
    if (spell != null || !attackingAllowed) {
      Spellcaster.Spell normalized = spell == null
          ? Spellcaster.Spell.NONE
          : spell;

      if (evoker.getSpell() != normalized) {
        evoker.setSpell(normalized);
      }
    }

    if (transitionTick != NO_TRANSITION
        && !animationOngoing()
    ) {
      transitionTick++;

      if (transitionTick >= EvokerConfig.phaseTransition) {
        nextPhase(false);
      }
    }

    if (phase != null) {
      phase.onTick(this, currentContext);
    }
  }

  public void nextPhase(boolean transition) {
    updateBossbarViewers();

    if (transition) {
      transitionTick = 0;
      setPhase(null);
    } else {
      setPhase(attackOrder[phaseIndex++]);
      transitionTick = NO_TRANSITION;

      if (phaseIndex >= attackOrder.length) {
        phaseIndex = 0;
        createPhaseOrder();
      }
    }
  }

  public boolean animationOngoing() {
    return spawnAnim.isRunning() || deathAnim.isRunning();
  }

  void createPhaseOrder() {
    List<AttackPhase> phases = new ObjectArrayList<>(Arrays.asList(AttackPhases.SELECTABLE_PHASES));
    Collections.shuffle(phases, random);

    phases.addAll(Arrays.asList(AttackPhases.NON_SELECTABLE_PHASES));

    SummonPhase.shuffleSpawns(random);
    this.attackOrder = phases.toArray(AttackPhase[]::new);
  }

  public void setPhase(AttackPhase newPhase) {
    if (phase != null) {
      phase.end(this);
    }

    lastPhase = phase;
    phase = newPhase;

    if (phase != null) {
      phase.start(this);
    }
  }

  private Evoker createEvoker(BossContext context) {
    return getWorld().spawn(getSpawn(), Evoker.class, evoker -> {
      AttackPhases.modifyHealth(evoker, EvokerConfig.baseHealth, context);

      evoker.getEquipment().setItem(EquipmentSlot.HEAD, null);

      evoker.customName(name().color(NamedTextColor.YELLOW));
      evoker.setCustomNameVisible(true);

      AttributeInstance moveSpeed = evoker.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
      DungeonUtils.clearModifiers(moveSpeed);
      moveSpeed.setBaseValue(0D);

      AttributeInstance knockbackResistance
          = evoker.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);

      DungeonUtils.clearModifiers(knockbackResistance);
      knockbackResistance.setBaseValue(1D);

      evoker.setRemoveWhenFarAway(false);
      getBossTeam().addEntity(evoker);
    });
  }

  @Override
  public boolean isAlive() {
    return getState() != EvokerState.NOT_ALIVE;
  }

  public void broadcast(boolean allowCancellation, BossMessage... msgs) {
    Validate.noNullElements(msgs, "Given messages were null or contained a null message, index: %d");
    Validate.notEmpty(msgs, "Messages were empty");

    if (random.nextBoolean() && allowCancellation) {
      return;
    }

    // Format messages
    TextComponent.Builder builder = text();
    Iterator<BossMessage> iterator = new ObjectArrayIterator<>(msgs);
    Component messagePrefix = name()
        .color(NamedTextColor.YELLOW)
        .append(
            text(" > ")
                .style(Style.style(NamedTextColor.GRAY, TextDecoration.BOLD))
        );

    while (iterator.hasNext()) {
      BossMessage m = iterator.next();

      builder
          .append(messagePrefix)
          .append(m.createMessage(currentContext));

      if (iterator.hasNext()) {
        builder.append(Component.newline());
      }
    }

    Component formatted = builder.build();

    // Send messages
    getRoom().getPlayers().forEach(player -> {
      player.sendMessage(formatted);

      player.playSound(
          Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1f, 2f)
      );
    });
  }

  @Override
  protected void giveRewards(Player player) {
    DungeonUtils.giveOrDropItem(
        player.getInventory(),
        player.getLocation(),
        BossItems.EVOKER.item()
    );

    int rhines = EvokerConfig.rhineReward;

    if (rhines > 0) {
      Users.get(player).addBalance(EvokerConfig.rhineReward);

      player.sendMessage(
          Text.format("You've received &e{0, rhines}",
              NamedTextColor.GOLD,
              rhines
          )
      );
    }

    ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
    FtcEnchants.addEnchant(enchantedBook, FtcEnchants.SOUL_BOND, 1);

    DungeonUtils.giveOrDropItem(
        player.getInventory(),
        player.getLocation(),
        enchantedBook
    );
  }

  public EvokerState getState() {
    return state == null ? EvokerState.NOT_ALIVE : state;
  }

  public void updatePhaseBarViewers() {
    phaseBar.removeAll();
    getRoom().getPlayers().forEach(phaseBar::addPlayer);
  }

  public static Team getBossTeam() {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    Team t = scoreboard.getTeam(EvokerConfig.bossTeam);

    if (t == null) {
      t = scoreboard.registerNewTeam(EvokerConfig.bossTeam);
      t.color(NamedTextColor.YELLOW);
      t.setAllowFriendlyFire(false);
      t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    return t;
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntitySpawn(EntitySpawnEvent event) {
    Entity e = event.getEntity();
    if (attackingAllowed) {
      return;
    }

    if (e instanceof EvokerFangs fangs
        && Objects.equals(evoker, fangs.getOwner())
    ) {
      event.setCancelled(true);
      return;
    }

    if (e instanceof Vex vex
        && Objects.equals(evoker, vex.getSummoner())
    ) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!event.getEntity().equals(getBossEntity())) {
      return;
    }

    if (isInvulnerable()) {
      DungeonUtils.cannotHarmEffect(getWorld(), evoker);

      event.setCancelled(true);
      return;
    }

    double newHealth = evoker.getHealth() - event.getFinalDamage();
    if (newHealth < EvokerConfig.deathPhaseHealth) {
      event.setCancelled(true);
      state = EvokerState.DYING;

      if (phase != null) {
        phase.end(this);
        phase = null;
      }

      if (deathAnim.isRunning()) {
        return;
      }

      deathAnim.start();
    } else {
      // Update boss bar
      bossBar.setProgress(newHealth / evoker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityMove(EntityMoveEvent event) {
    if (!event.getEntity().equals(getBossEntity())) {
      return;
    }

    if (!event.hasChangedPosition()) {
      return;
    }

    event.setCancelled(true);
  }
}
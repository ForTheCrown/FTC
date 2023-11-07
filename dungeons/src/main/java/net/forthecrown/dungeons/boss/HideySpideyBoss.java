package net.forthecrown.dungeons.boss;

import com.destroystokyo.paper.entity.Pathfinder;
import java.util.Set;
import net.forthecrown.Worlds;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.dungeons.boss.components.MinionSpawnerComponent;
import net.forthecrown.text.Text;
import net.forthecrown.titles.RankTier;
import net.forthecrown.titles.UserRanks;
import net.forthecrown.titles.UserTitles;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.spongepowered.math.vector.Vector3d;

public class HideySpideyBoss extends SimpleBoss {

  public static Vector3d SPAWN_VEC = Vector3d.from(-78.5 + 202, 55 - 48, 284.5 - 49);

  public HideySpideyBoss() {
    super("Hidey Spidey",
        new Location(Worlds.voidWorld(), SPAWN_VEC.x(), SPAWN_VEC.y(), SPAWN_VEC.z()),
        DungeonAreas.SPIDEY_ROOM,

        DungeonUtils.makeDungeonItem(Material.SPIDER_EYE, 45, (Component) null),
        DungeonUtils.makeDungeonItem(Material.FERMENTED_SPIDER_EYE, 20, (Component) null),
        DungeonUtils.makeDungeonItem(Material.STRING, 30, (Component) null),

        ItemStacks.potionBuilder(Material.TIPPED_ARROW, 5)
            .setBaseEffect(new PotionData(PotionType.POISON))
            .addLoreRaw(DungeonUtils.DUNGEON_LORE)
            .build()
    );
  }

  @Override
  protected void createComponents(Set<BossComponent> c) {
    super.createComponents(c);

    c.add(
        MinionSpawnerComponent.create(
            (pos, world, context, boss) -> {
              Vector3d dif = SPAWN_VEC.sub(pos).normalize();
              Vector velocity = Vectors.toVec(dif).multiply(1.5f);

              return world.spawn(
                  new Location(world, pos.x(), pos.y(), pos.z()),
                  CaveSpider.class,
                  caveSpider -> {
                    caveSpider.setVelocity(velocity);

                    double health = context.modifier() + 12;
                    caveSpider.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                    caveSpider.setHealth(health);
                    caveSpider.setLootTable(LootTables.EMPTY.getLootTable());
                  }
              );
            },
            200, 10,
            new double[][]{
                {-68.5 + 202, 57 - 48, 284.5 - 49},
                {-88.5 + 202, 57 - 48, 284.5 - 49}
            }
        )
    );
  }

  @Override
  protected Mob onSpawn(BossContext context) {
    return getWorld().spawn(getSpawn(), Spider.class, spidey -> {
      spidey.setCustomName("Hidey Spidey");
      spidey.setCustomNameVisible(false);
      spidey.setRemoveWhenFarAway(false);
      spidey.setPersistent(true);

      double health = context.health(300);
      spidey.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
      spidey.setHealth(health);

      // God, I love modifiers
      DungeonUtils.clearModifiers(spidey.getAttribute(Attribute.GENERIC_MAX_HEALTH));

      spidey.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25);
      spidey.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
      spidey.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.damage(11));
      spidey.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
          .setBaseValue(0.28 + (context.modifier() / 20));

      Pathfinder pathfinder = spidey.getPathfinder();
      pathfinder.setCanFloat(false);

      spidey.setInvisible(true);
    });
  }

  @Override
  protected void giveRewards(Player p) {
    DungeonUtils.giveOrDropItem(
        p.getInventory(),
        entity.getLocation(),
        BossItems.HIDEY_SPIDEY.item()
    );

    if (!PluginUtil.isEnabled("FTC-UserTitles")) {
      return;
    }

    // Final boss of the first 3 levels,
    // Rewards the free rank tier
    User user = Users.get(p);

    // Don't give the tier if you already
    // have the tier lol
    UserTitles titles = user.getComponent(UserTitles.class);
    if (titles.hasTier(RankTier.FREE)) {
      return;
    }

    titles.addTier(RankTier.FREE);
    user.sendMessage(Text.format("Got {0} rank", NamedTextColor.GOLD, UserRanks.KNIGHT));
  }
}
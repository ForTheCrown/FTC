package net.forthecrown.dungeons;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.boss.*;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.usables.*;
import net.forthecrown.registry.Registries;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.actions.UsageAction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Bosses {
    public static final SpawnRequirement.Type
            LEVEL_TYPE = tag -> SpawnRequirement.levelCleared(Keys.parse(tag.getAsString())),
            ITEM_TYPE = tag -> {
                ListTag list = (ListTag) tag;
                List<ItemStack> items = new ObjectArrayList<>();

                for (Tag t: list) {
                    CompoundTag iTag = (CompoundTag) t;

                    net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.of(iTag);
                    ItemStack item = CraftItemStack.asCraftMirror(nmsItem);
                    items.add(item);
                }

                return SpawnRequirement.items(items);
            };

    public static final NamespacedKey KEY = Squire.createRoyalKey("bossitem"); // God, I wish there was an underscore in this
    public static final NamespacedKey BOSS_TAG = Squire.createRoyalKey("boss_tag");
    public static final DungeonUserDataAccessor ACCESSOR = new DungeonUserDataAccessor();

    public static final EvokerBoss EVOKER = register(new EvokerBoss());

    public static final SimpleBoss
            ZHAMBIE         = register(new ZhambieBoss()),
            SKALATAN        = register(new SkalatanBoss()),
            HIDEY_SPIDEY    = register(new HideySpideyBoss()),
            DRAWNED         = register(new DrawnedBoss());

    private Bosses(){
    }

    public static void init() {
        Registries.SPAWN_REQUIREMENTS.register(Keys.forthecrown("required_items"), ITEM_TYPE);
        Registries.SPAWN_REQUIREMENTS.register(Keys.forthecrown("level_cleared"), LEVEL_TYPE);
        Registries.SPAWN_REQUIREMENTS.close();

        Registries.DUNGEON_BOSSES.close();

        register(new ActionGiveArtifact());
        register(new ActionSpawnBoss());
        register(new ActionEntranceInfo());
        register(new ActionShowBossInfo());

        Registries.NPCS.register(DiegoNPC.KEY, new DiegoNPC());
        Registries.USAGE_CHECKS.register(CheckBeatenBoss.KEY, new CheckBeatenBoss());
    }

    private static void register(UsageAction<?> action){
        Registries.USAGE_ACTIONS.register(action.key(), action);
    }

    private static <T extends KeyedBoss> T register(T boss) {
        return (T) Registries.DUNGEON_BOSSES.register(boss.key(), boss);
    }

    public static void shutDown() {
        Registries.DUNGEON_BOSSES.forEach(boss -> boss.kill(true));
    }
}

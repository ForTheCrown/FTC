package net.forthecrown.datafix;

import net.forthecrown.core.Worlds;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.challenge.ItemChallenge;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3i;

public class ChallengeItemContainerFix extends DataUpdater {
  public static final Vector3i
      CHEST_HEADS   = Vector3i.from(263, 71, 191),
      CHEST_ITEMS   = Vector3i.from(263, 72, 191),
      CHEST_POTIONS = Vector3i.from(263, 73, 191);

  @Override
  protected boolean update() throws Throwable {
    var manager = ChallengeManager.getInstance();
    var storage = manager.getStorage();
    var registry = manager.getChallengeRegistry();
    World world = Worlds.overworld();

    for (var e: registry.entries()) {
      var challenge = e.getValue();

      if (!(challenge instanceof ItemChallenge)) {
        continue;
      }

      var k = e.getKey();
      var container = storage.loadContainer(e);
      container.setChestWorld(world);

      if (k.contains("_head")) {
        container.getChests().add(CHEST_HEADS);
      } else if (k.contains("_potion")) {
        container.getChests().add(CHEST_POTIONS);
      } else {
        container.getChests().add(CHEST_ITEMS);
      }

      LOGGER.info("Set challenge {} item chests to {}",
          k, container.getChests()
      );

      storage.saveContainer(container);
    }

    return true;
  }
}
package net.forthecrown.usables.virtual;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.forthecrown.Loggers;
import net.forthecrown.events.Events;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.slf4j.Logger;

public class BlockTriggerSystem implements TriggerSystem<BlockTrigger> {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final NamespacedKey TRIGGERS_KEY
      = new NamespacedKey("usables", "block_triggers");

  // What a fucking type name
  private final Map<String, Long2ObjectMap<TriggerMap<BlockAction>>> actionMap
      = new Object2ObjectOpenHashMap<>();

  private BlockTriggerListener listener;

  @Override
  public void initializeSystem(VirtualUsableManager manager) {
    listener = new BlockTriggerListener(this, manager);
    Events.register(listener);
  }

  static TriggerMap<BlockAction> newTriggerMap() {
    return new TriggerMap<>(BlockAction.CODEC);
  }

  @Override
  public void onTriggerLoaded(VirtualUsable usable, BlockTrigger trigger) {
    onTriggerAdd(usable, trigger);
  }

  @Override
  public void onTriggerAdd(VirtualUsable usable, BlockTrigger trigger) {
    Optional<TileState> blockOpt = trigger.getBlock()
        .map(block -> {
          if (block.getState() instanceof TileState tile) {
            return tile;
          }
          return null;
        });

    blockOpt.ifPresentOrElse(
        tile -> {
          TriggerMap<BlockAction> map = loadMap(tile);
          map.add(trigger.getAction(), usable.getName());
          saveMap(map, tile);
        },

        () -> {
          Long2ObjectMap<TriggerMap<BlockAction>> blockMap
              = actionMap.computeIfAbsent(trigger.getWorld(), s -> new Long2ObjectOpenHashMap<>());

          TriggerMap<BlockAction> actionMap
              = blockMap.computeIfAbsent(trigger.blockKey(), o -> newTriggerMap());

          actionMap.add(trigger.getAction(), usable.getName());
        }
    );
  }

  @Override
  public void onTriggerRemove(VirtualUsable usable, BlockTrigger trigger) {
    trigger.getBlock()
        .map(block -> {
          if (block.getState() instanceof TileState tile) {
            return tile;
          }

          return null;
        })
        .ifPresent(tileState -> {
          var map = loadMap(tileState);

          if (map.remove(trigger.getAction(), usable.getName())) {
            saveMap(map, tileState);
          }
        });

    var blockMap = actionMap.get(trigger.getWorld());
    if (blockMap == null || blockMap.isEmpty()) {
      return;
    }

    var list = blockMap.get(trigger.blockKey());
    if (list == null || list.isEmpty()) {
      return;
    }

    list.remove(trigger.getAction(), usable.getName());
  }

  private void saveMap(TriggerMap<BlockAction> map, TileState tile) {
    var pdc = tile.getPersistentDataContainer();

    map.saveToContainer(pdc, TRIGGERS_KEY)
        .mapError(s -> "Failed to save triggers in block " + tile.getBlock() + ": " + s)
        .resultOrPartial(LOGGER::error)
        .ifPresent(unit -> tile.update());
  }

  private TriggerMap<BlockAction> loadMap(TileState tile) {
    PersistentDataContainer pdc = tile.getPersistentDataContainer();
    TriggerMap<BlockAction> map = newTriggerMap();

    map.loadFromContainer(pdc, TRIGGERS_KEY)
        .mapError(s -> "Failed to load triggers in block " + tile.getBlock() + ": " + s)
        .resultOrPartial(LOGGER::error);

    return map;
  }

  public List<String> getUsables(Block block, BlockAction action) {
    var inData = getFromBlockData(block, action);
    var fromMem = getFromMemory(block, action);

    if (inData.isEmpty() && fromMem.isEmpty()) {
      return ObjectLists.emptyList();
    }

    if (inData.isEmpty()) {
      return fromMem;
    }

    if (fromMem.isEmpty()) {
      return inData;
    }

    // A new list must be created as TriggerActionMap#get returns an immutable list
    List<String> combined = new ObjectArrayList<>(inData.size() + fromMem.size());
    combined.addAll(inData);
    combined.addAll(fromMem);
    return combined;
  }

  private List<String> getFromMemory(Block block, BlockAction action) {
    var map = actionMap.get(block.getWorld().getName());

    if (map == null || map.isEmpty()) {
      return ObjectLists.emptyList();
    }

    var actions = map.get(block.getBlockKey());

    if (actions == null || actions.isEmpty()) {
      return ObjectLists.emptyList();
    }

    return actions.get(action);
  }

  private List<String> getFromBlockData(Block block, BlockAction action) {
    if (!(block.getState() instanceof TileState tile)) {
      return ObjectLists.emptyList();
    }

    TriggerMap<BlockAction> actionMap = loadMap(tile);
    return actionMap.get(action);
  }
}

class BlockTriggerListener implements Listener {

  private final BlockTriggerSystem system;
  private final VirtualUsableManager manager;

  public BlockTriggerListener(BlockTriggerSystem system, VirtualUsableManager manager) {
    this.system = system;
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    List<String> list = system.getUsables(event.getBlock(), BlockAction.ON_BLOCK_BREAK);
    if (list.isEmpty()) {
      return;
    }

    var player = event.getPlayer();

    Triggers.runReferences(
        list, manager, player, event,

        // Context fill
        interaction -> {
          var ctx = interaction.context();
          ctx.put("expToDrop", event.getExpToDrop());
          ctx.put("dropItems", event.isDropItems());
        },

        // Post execution
        interaction -> {
          interaction.getValue("expToDrop", Integer.class).ifPresent(event::setExpToDrop);
          interaction.getBoolean("dropItems").ifPresent(event::setDropItems);
        }
    );
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    List<String> list = system.getUsables(event.getBlock(), BlockAction.ON_BLOCK_PLACE);
    if (list.isEmpty()) {
      return;
    }

    Player player = event.getPlayer();

    Triggers.runReferences(list, manager, player, event, interaction -> {
      var ctx = interaction.context();
      ctx.put("block", event.getBlock());
      ctx.put("location", event.getBlock().getLocation());
      ctx.put("hand", event.getHand());
      ctx.put("againstBlock", event.getBlockAgainst());
    }, null);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    List<String> list
        = system.getUsables(event.getClickedBlock(), BlockAction.ON_BLOCK_INTERACT);

    if (list.isEmpty()) {
      return;
    }

    var player = event.getPlayer();

    Triggers.runReferences(
        list, manager, player, event,

        // Context fill
        interaction -> {
          var ctx = interaction.context();
          ctx.put("useItemInHand", event.useItemInHand());
          ctx.put("item", event.getItem());
          ctx.put("useClickedBlock", event.useInteractedBlock());
          ctx.put("action", event.getAction());
          ctx.put("clickedPosition", event.getInteractionPoint());
          ctx.put("hand", event.getHand());
        },

        // Post execution
        interaction -> {
          interaction.getValue("useItemInHand", Result.class)
              .ifPresent(event::setUseItemInHand);

          interaction.getValue("useClickedBlock", Result.class)
              .ifPresent(event::setUseInteractedBlock);
        }
    );
  }
}

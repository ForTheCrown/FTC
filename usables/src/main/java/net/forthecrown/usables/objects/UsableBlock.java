package net.forthecrown.usables.objects;

import static net.forthecrown.usables.Usables.BLOCK_KEY;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.utils.VanillaAccess;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.slf4j.Logger;

@Getter
public class UsableBlock extends InWorldUsable {

  public static final Logger LOGGER = Loggers.getLogger();

  private final Block block;

  public UsableBlock(Block block) {
    Objects.requireNonNull(block);
    this.block = block;
  }

  @Override
  public void fillContext(Map<String, Object> context) {
    super.fillContext(context);
    context.put("block", block);
    context.put("location", block.getLocation());
  }

  @Override
  public Component name() {
    return Component.text(
        String.format("Block(%s %s %s)", block.getX(), block.getY(), block.getZ())
    );
  }

  @Override
  public String getCommandPrefix() {
    return "/usableblock %s %s %s".formatted(block.getX(), block.getY(), block.getZ());
  }

  @Override
  public CommandSender getCommandSender() {
    var tile = getTileState();

    if (!(tile instanceof CommandBlock sender)) {
      return Bukkit.getConsoleSender();
    }

    return VanillaAccess.getSender(sender);
  }

  @Override
  protected void executeOnContainer(
      boolean saveIntent,
      Consumer<PersistentDataContainer> consumer
  ) {
    TileState tile = getTileState();

    if (tile == null) {
      LOGGER.warn("Usable block at {} is not a tile entity, cannot {}",
          block, saveIntent ? "save" : "load"
      );
      return;
    }

    PersistentDataContainer pdc = tile.getPersistentDataContainer();
    PersistentDataContainer dataPdc;

    if (saveIntent) {
      dataPdc = pdc.getAdapterContext().newPersistentDataContainer();
    } else {
      if (!pdc.has(BLOCK_KEY, PersistentDataType.TAG_CONTAINER)) {
        LOGGER.warn("Cannot load from non-usable block at {}", block);
        return;
      }

      dataPdc = pdc.get(BLOCK_KEY, PersistentDataType.TAG_CONTAINER);
    }

    consumer.accept(dataPdc);

    if (saveIntent) {
      pdc.set(BLOCK_KEY, PersistentDataType.TAG_CONTAINER, dataPdc);
      tile.update();
    }
  }

  public TileState getTileState() {
    BlockState state = block.getState();

    if (!(state instanceof TileState tile)) {
      return null;
    }

    return tile;
  }
}

package net.forthecrown.guilds.listeners;

import static net.forthecrown.guilds.menu.MessageCreationMenu.MESSAGE_SIGN_KEY;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildMessage;
import net.forthecrown.guilds.menu.GuildMenus;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

class SignListener implements Listener {

  private final GuildManager manager;

  public SignListener(GuildManager manager) {
    this.manager = manager;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerOpenSign(PlayerOpenSignEvent event) {
    if (event.getSign().getPersistentDataContainer().has(MESSAGE_SIGN_KEY)) {
      event.setCancelled(false);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onSignChange(SignChangeEvent event) {
    var block = event.getBlock();
    Sign sign = (Sign) block.getState();
    var pdc = sign.getPersistentDataContainer();

    if (!pdc.has(MESSAGE_SIGN_KEY)) {
      return;
    }

    PersistentDataContainer container = pdc.get(MESSAGE_SIGN_KEY, PersistentDataType.TAG_CONTAINER);
    CompoundTag tag = PaperNbt.fromDataContainer(container);

    BlockData oldData = PaperNbt.loadBlockData(tag.getCompound("previous_state"));
    block.setBlockData(oldData, false);
    event.setCancelled(true);

    UUID guildId = tag.getUUID("guild");
    Guild guild = manager.getGuild(guildId);

    if (guild == null) {
      return;
    }

    List<Component> lines = new ArrayList<>(event.lines());

    GuildMessage message = new GuildMessage(
        sign.getType(),
        event.getPlayer().getUniqueId(),
        System.currentTimeMillis(),
        lines.toArray(Component[]::new)
    );
    guild.addMsgBoardPost(message);

    Tasks.runLater(() -> {
      GuildMenus.open(
          GuildMenus.MAIN_MENU.getMessageBoard(),
          Users.get(event.getPlayer()),
          guild
      );
    }, 1);
  }
}

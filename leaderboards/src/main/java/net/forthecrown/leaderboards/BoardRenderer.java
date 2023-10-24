package net.forthecrown.leaderboards;

import net.forthecrown.packet.EntityRenderer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BoardRenderer implements EntityRenderer {

  private final ServiceImpl service;

  public BoardRenderer(ServiceImpl service) {
    this.service = service;
  }

  @Override
  public boolean test(Player player, int entityId, @Nullable Component existingName) {
    return service.getByEntityId().containsKey(entityId);
  }

  @Override
  public Component render(Player player, int entityId, @Nullable Component existingName) {
    BoardData data = service.getByEntityId().get(entityId);

    if (data == null) {
      return existingName;
    }

    BoardImpl board = data.board();
    return board.renderText(player);
  }
}

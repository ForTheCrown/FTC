package net.forthecrown.core.commands.admin;

import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class CommandSkull extends FtcCommand {

  public CommandSkull() {
    super("skull");
    setDescription("Gets a player's skull");
    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<player>", "Gets a <player>'s skull");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("profile", Arguments.USER)
            .executes(c -> {
              Player player = c.getSource().asPlayer();

              if (player.getInventory().firstEmpty() == -1) {
                throw Exceptions.INVENTORY_FULL;
              }

              User user = Arguments.getUser(c, "profile");

              CompletableFuture.runAsync(() -> {
                ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta meta = (SkullMeta) item.getItemMeta();

                meta.setOwningPlayer(user.getOfflinePlayer());
                item.setItemMeta(meta);

                Tasks.runSync(() -> player.getInventory().addItem(item));
              });
              return 0;
            })
        );
  }
}
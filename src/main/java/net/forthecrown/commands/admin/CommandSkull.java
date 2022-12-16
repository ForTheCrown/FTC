package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.concurrent.CompletableFuture;

public class CommandSkull extends FtcCommand {
    public CommandSkull() {
        super("skull");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
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
package net.forthecrown.commands.admin;

import net.forthecrown.core.Crown;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.concurrent.CompletableFuture;

public class CommandSkull extends FtcCommand {
    public CommandSkull(){
        super("skull", Crown.inst());

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("profile", UserArgument.user())
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            if(player.getInventory().firstEmpty() == -1) throw FtcExceptionProvider.inventoryFull();

                            CrownUser user = UserArgument.getUser(c, "profile");

                            CompletableFuture.runAsync(() -> {
                                ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                                SkullMeta meta = (SkullMeta) item.getItemMeta();

                                meta.setOwningPlayer(user.getOfflinePlayer());
                                item.setItemMeta(meta);

                                Bukkit.getScheduler().runTask(Crown.inst(), () -> player.getInventory().addItem(item));
                            });
                            return 0;
                        })
                );
    }
}

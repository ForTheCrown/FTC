package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.concurrent.CompletableFuture;

public class CommandSkull extends FtcCommand {
    public CommandSkull(){
        super("skull", CrownCore.inst());

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("profile", UserType.user())
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            if(player.getInventory().firstEmpty() == -1) throw FtcExceptionProvider.inventoryFull();

                            CrownUser user = UserType.getUser(c, "profile");

                            CompletableFuture.runAsync(() -> {
                                ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                                SkullMeta meta = (SkullMeta) item.getItemMeta();

                                meta.setOwningPlayer(user.getOfflinePlayer());
                                item.setItemMeta(meta);

                                Bukkit.getScheduler().runTask(CrownCore.inst(), () -> player.getInventory().addItem(item));
                            });
                            return 0;
                        })
                );
    }
}

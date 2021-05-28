package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.KitType;
import net.forthecrown.emperor.useables.kits.Kit;
import net.forthecrown.emperor.utils.InterUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandKitEdit extends CrownCommandBuilder {
    public CommandKitEdit(){
        super("kitedit", CrownCore.inst());

        setPermission(Permissions.KIT_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("create")
                        .then(argument("name", StringArgumentType.word())
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    String name = c.getArgument("name", String.class);
                                    Key key = Key.key(CrownCore.getNamespace(), name);

                                    List<ItemStack> items = new ArrayList<>();

                                    for (ItemStack i: player.getInventory()){
                                        if(i == null || i.getType() == Material.AIR) continue;
                                        items.add(i);
                                    }

                                    Kit kit = CrownCore.getKitRegistry().register(key, items);

                                    c.getSource().sendAdmin(
                                            Component.text("Created kit named ")
                                                    .append(kit.displayName())
                                    );
                                    return 0;
                                })
                        )
                )

                .then(argument("kit", KitType.kit())
                        .suggests((c, b) -> KitType.kit().listSuggestions(c, b, true))

                        .then(argument("delete")
                                .executes(c -> {
                                    Kit kit = get(c);
                                    kit.delete();

                                    c.getSource().sendAdmin(Component.text("Deleting kit ").append(kit.displayName()));
                                    return 0;
                                })
                        )

                        .then(argument("give")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    Kit kit = get(c);

                                    if(!kit.hasSpace(player.getInventory())) throw FtcExceptionProvider.create("You don't have enough space");

                                    kit.giveItems(player);
                                    return 0;
                                })
                        )

                        .then(InterUtils.checksArguments(this::get))
                );
    }

    private Kit get(CommandContext<CommandSource> c){
        return KitType.getKit(c, "kit");
    }
}

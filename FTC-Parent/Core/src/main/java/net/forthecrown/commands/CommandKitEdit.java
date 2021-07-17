package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.arguments.KitType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.useables.kits.Kit;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.useables.InteractionUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandKitEdit extends FtcCommand {
    public CommandKitEdit(){
        super("kitedit", CrownCore.inst());

        setPermission(Permissions.KIT_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("name", StringArgumentType.word())
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    String name = c.getArgument("name", String.class);
                                    Key key = FtcUtils.parseKey(name);

                                    List<ItemStack> items = new ArrayList<>();

                                    for (ItemStack i: player.getInventory()){
                                        if(FtcUtils.isItemEmpty(i)) continue;
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

                        .then(literal("delete")
                                .executes(c -> {
                                    Kit kit = get(c);
                                    kit.delete();

                                    c.getSource().sendAdmin(Component.text("Deleting kit ").append(kit.displayName()));
                                    return 0;
                                })
                        )

                        .then(literal("give")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    Kit kit = get(c);

                                    if(!kit.hasSpace(player.getInventory())) throw FtcExceptionProvider.create("You don't have enough space");

                                    kit.giveItems(player);
                                    return 0;
                                })
                        )

                        .then(InteractionUtils.checksArguments(this::get))
                );
    }

    private Kit get(CommandContext<CommandSource> c){
        return KitType.getKit(c, "kit");
    }
}

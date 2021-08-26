package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.management.ManagementFactory;

public class CommandMemory extends FtcCommand {
    public CommandMemory(){
        super("memory", Crown.inst());

        setAliases("mem");
        setPermission(Permissions.HELPER);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    TextColor yelo = NamedTextColor.GRAY;
                    TextColor gold = NamedTextColor.GOLD;

                    TextComponent.Builder builder = Component.text()
                            .color(yelo)
                            .append(Component.text("Uptime: "))
                            .append(FtcFormatter.millisIntoTime(ManagementFactory.getRuntimeMXBean().getUptime()).color(gold))
                            .append(Component.newline())

                            .append(Component.text("Current TPS: "))
                            .append(Component.text(Math.max(20, Bukkit.getTPS()[0])).color(gold))
                            .append(Component.newline())

                            .append(Component.text("Maximum memory: "))
                            .append(Component.text(Runtime.getRuntime().maxMemory()/1000000 + " MB").color(gold))
                            .append(Component.newline())

                            .append(Component.text("Free memory: "))
                            .append(Component.text(Runtime.getRuntime().freeMemory()/1000000 + " MB").color(gold))
                            .append(Component.newline())

                            .append(Component.text("Worlds: "));

                    for (World w: Bukkit.getWorlds()){
                        builder
                                .append(Component.newline())
                                .append(Component.text(w.getName() + ": ").color(gold))
                                .append(Component.text("Entity count: ")
                                        .append(Component.text(w.getEntityCount() + ", ").color(gold))
                                )
                                .append(Component.text("Loaded chunks: ")
                                        .append(Component.text(w.getLoadedChunks().length + ", ").color(gold))
                                )
                                .append(Component.text("Tile entities: ")
                                        .append(Component.text(w.getTileEntityCount() + ", ").color(gold))
                                )
                                .append(Component.text("Tickable tile entities: ")
                                        .append(Component.text(w.getTickableTileEntityCount()).color(gold))
                                );
                    }

                    c.getSource().sendMessage(builder.build());
                    return 0;
                });
    }
}

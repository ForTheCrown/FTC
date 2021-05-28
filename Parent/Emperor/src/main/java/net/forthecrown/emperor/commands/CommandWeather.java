package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.WeatherType;
import org.bukkit.World;

public class CommandWeather extends CrownCommandBuilder {
    public CommandWeather(){
        super("weather", CrownCore.inst());

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("weather", EnumArgument.of(WeatherType.class))
                        .executes(c -> {
                            CommandSource source = c.getSource();
                            World world = source.getWorld();
                            WeatherType weatherType = c.getArgument("weather", WeatherType.class);

                            world.setStorm(weatherType == WeatherType.DOWNFALL);
                            source.sendAdmin(
                                    Component.text("Set weather to " + weatherType.name().toLowerCase())
                            );
                            return 0;
                        })
                );
    }
}

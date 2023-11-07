package net.forthecrown.usables.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Text;
import net.forthecrown.usables.CmdUsables;
import net.forthecrown.usables.UPermissions;
import net.forthecrown.usables.objects.Warp;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class WarpCommand extends CmdUsableCommand<Warp> {

  public WarpCommand(CmdUsables<Warp> usables) {
    super("warp", usables, Warp.class);
    setAliases("warps");
    setPermission(UPermissions.WARP);
  }

  @Override
  protected String usagePrefix() {
    return "<warp>";
  }

  @Override
  public Permission getAdminPermission() {
    return UPermissions.WARP_ADMIN;
  }

  @Override
  protected void createUsages(UsageFactory factory) {
    var admin = factory.withPermission(UPermissions.USABLES);
    var prefix = admin.withPrefix("destination");
    prefix.usage("").addInfo("Displays a warp's current destination location");
    prefix.usage("set").addInfo("Sets a warp's destination to where you're standing");
    prefix.usage("set <pos: x,y,z>").addInfo("Sets a warp's position to the given xyz coordinates");

    admin.usage("instant")
        .addInfo("Shows if a warp will always instantly teleport players or not");

    admin.usage("instant <true | false>")
        .addInfo("Sets if a warp will always instantly teleport players or not");

    super.createUsages(factory);
  }

  @Override
  protected Warp create(String name, CommandSource source) throws CommandSyntaxException {
    Player player = source.asPlayer();
    Location location = player.getLocation();

    Warp warp = new Warp(name);
    warp.setDestination(location);

    return warp;
  }

  @Override
  protected <B extends ArgumentBuilder<CommandSource, B>> void createEditArguments(
      B argument,
      UsableProvider<Warp> provider
  ) {
    super.createEditArguments(argument, provider);

    argument.then(literal("destination")
        .requires(hasAdminPermission())

        .executes(c -> {
          Warp warp = provider.get(c);

          c.getSource().sendMessage(
              Text.format("Warp &e{0}&r's destination: &6{1, location, -w -c}&r",
                  NamedTextColor.GRAY,
                  warp.displayName(),
                  warp.getDestination()
              )
          );
          return 0;
        })

        .then(literal("set")
            .executes(c -> {
              Player player = c.getSource().asPlayer();
              Location l = player.getLocation();
              return updateDestination(c.getSource(), provider.get(c), l);
            })

            .then(argument("dest", ArgumentTypes.position())
                .executes(c -> {
                  Location l = ArgumentTypes.getLocation(c, "dest");
                  return updateDestination(c.getSource(), provider.get(c), l);
                })
            )
        )
    );
  }

  private int updateDestination(CommandSource source, Warp warp, Location location) {
    warp.setDestination(location);

    source.sendSuccess(
        Text.format("Moved Warp &e{0}&r's destination to &6{1, location, -c -w}&r.",
            NamedTextColor.GRAY,
            warp.displayName(),
            location
        )
    );
    return 0;
  }
}

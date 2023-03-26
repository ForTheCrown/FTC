package net.forthecrown.commands.test;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FTC;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.challenge.StreakCategory;
import net.forthecrown.core.challenge.StreakIncreaseEvent;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.ModuleServices;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.useables.Usables;
import net.forthecrown.user.User;
import net.forthecrown.utils.Particles;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.Logger;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitTask;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class CommandFtcTest extends FtcCommand {

  private static final Logger LOGGER = Loggers.getLogger();

  public CommandFtcTest() {
    super("FtcTest");

    register();
  }

  private BukkitTask renderTriggers;

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /FtcTest
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("test_staff_log_marker")
            .executes(c -> {
              LOGGER.info(Loggers.STAFF_LOG, "Hello! Hello? Hello.. Anyone?");
              return 0;
            })
        )

        .then(literal("render_held_banner")
            .executes(c -> {
              var player = c.getSource().asPlayer();
              var held = Commands.getHeldItem(player);

              if (!(held.getItemMeta() instanceof BannerMeta)) {
                throw Exceptions.format("Held item not banner");
              }

              try {
                var img = GuildManager.get().getRenderer()
                    .renderSquare(held);

                ImageIO.write(
                    img,
                    "png",
                    PathUtil.pluginPath("banner_test.png").toFile()
                );
              } catch (IOException e) {
                throw new RuntimeException(e);
              }

              return 0;
            })
        )

        .then(literal("fire_streak_event")
            .then(argument("streak", IntegerArgumentType.integer(1))
                .then(argument("user", Arguments.ONLINE_USER)
                    .executes(c -> {
                      int streak = c.getArgument("streak", Integer.class);
                      User user = Arguments.getUser(c, "user");

                      StreakIncreaseEvent event = new StreakIncreaseEvent(
                          user,
                          StreakCategory.ITEMS,
                          streak,

                          ChallengeManager.getInstance()
                              .getEntry(user.getUniqueId())
                      );

                      event.callEvent();

                      c.getSource().sendMessage("Called event");
                      return 0;
                    })
                )
            )
        )

        .then(literal("guild_message")
            .executes(c -> {
              var user = getUserSender(c);
              var guild = user.getGuild();

              if (guild == null) {
                throw Exceptions.NOT_IN_GUILD;
              }

              guild.sendMessage(
                  text("Hello hello!, test test")
              );
              return 0;
            })
        )

        .then(literal("run_day_change")
            .executes(c -> {
              ModuleServices.DAY_CHANGE.run();
              return 0;
            })
        )

        .then(literal("render_triggers")
            .executes(c -> {
              if (renderTriggers != null) {
                renderTriggers = Tasks.cancel(renderTriggers);
              } else {
                renderTriggers = Tasks.runTimer(() -> {
                  var triggers = Usables.getInstance().getTriggers()
                      .getTriggers();

                  for (var t : triggers) {
                    var bounds = t.getBounds();
                    World w = bounds.getWorld();

                    try {
                      Particles.drawBounds(w, bounds, Color.RED);
                    } catch (Throwable exc) {
                      LOGGER.error("Error drawing bounds of {}: {}",
                          t.getName(), bounds, exc
                      );
                    }
                  }

                }, 15, 15);
              }

              return 0;
            })
        )

        .then(literal("test_script")
            .executes(c -> {
              NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
              NashornScriptEngine scriptEngine = (NashornScriptEngine) factory.getScriptEngine(
                  new String[0],
                  FTC.getPlugin().getClass().getClassLoader(),
                  className -> true
              );

              try {
                Reader reader = Files.newBufferedReader(
                    PathUtil.pluginPath("test_script.js")
                );

                scriptEngine.eval(reader);

                scriptEngine.invokeFunction(
                    "called_by_java",
                    c.getSource().asPlayer()
                );
              } catch (Exception e) {
                e.printStackTrace();
              }

              return 0;
            })
        )

        .then(literal("splitter")
            .executes(c -> {
              Component first = text("a\nb\nc\n");
              List<Component> fSplit = Text.split(Pattern.compile("\n"), first);

              c.getSource().sendMessage("fSplit:");
              fSplit.forEach(component -> c.getSource().sendMessage(component));

              if (fSplit.size() != 4) {
                throw Exceptions.format("First failed, expected size {0}, found {1}", 4,
                    fSplit.size());
              }

              Component second = text("Hello julian!\n Great to meet you :D");
              List<Component> sSplit = Text.split(Pattern.compile("\n"), second);

              c.getSource().sendMessage("sSplit:");
              sSplit.forEach(component -> c.getSource().sendMessage(component));

              if (sSplit.size() != 2) {
                throw Exceptions.format("Second failed, expected size {0}, found {1}", 2,
                    sSplit.size());
              }

              Component third = text("First Second Third Fourth Fifth");
              List<Component> tSplit = Text.split(Pattern.compile("\s"), third);

              c.getSource().sendMessage("tSplit:");
              tSplit.forEach(component -> c.getSource().sendMessage(component));

              if (tSplit.size() != 5) {
                throw Exceptions.format("Third failed, expected size {0}, found {1}", 5,
                    tSplit.size());
              }

              Component fourth = text()
                  .color(NamedTextColor.GRAY)
                  .content("First ")

                  .append(
                      text("Second "),
                      text("Third ", NamedTextColor.YELLOW),
                      text("Fourth", NamedTextColor.GOLD, TextDecoration.BOLD),
                      text("Fifth"),
                      text("Sixth Seventh", NamedTextColor.RED)
                  )

                  .build();

              List<Component> foSplit = Text.split(Pattern.compile("\s+"), fourth);
              c.getSource().sendMessage("foSplit:");
              foSplit.forEach(component -> c.getSource().sendMessage(component));

              if (foSplit.size() != 5) {
                throw Exceptions.format("Fourth failed, expected size {0}, found {1}", 5,
                    foSplit.size());
              }

              return 0;
            })
        )

        .then(literal("test_permission")
            .then(argument("permission", StringArgumentType.string())
                .then(argument("user", Arguments.USER)
                    .executes(c -> {
                      var user = Arguments.getUser(c, "user");
                      var perm = StringArgumentType.getString(c, "permission");

                      c.getSource().sendMessage(
                          Text.format("{0, user} has permission '{1}': {2}",
                              user,
                              perm,
                              user.hasPermission(perm)
                          )
                      );
                      return 0;
                    })
                )
            )
        );
  }
}
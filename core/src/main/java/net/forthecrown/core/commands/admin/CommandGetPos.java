package net.forthecrown.core.commands.admin;

import static net.kyori.adventure.text.Component.text;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandGetPos extends FtcCommand {

  static final String JAVA_FORMAT
      = "new Location(Bukkit.getWorld(\"%s\"), %sd, %sd, %sd, %sf, %sf)";

  static final String TP_EXACT_FORMAT
      = "/tp_exact world=%s x=%s y=%s z=%s yaw=%s pitch=%s";

  public CommandGetPos() {
    super("getpos");
    setDescription("Gets the accurate position of a player");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = Arguments.getUser(c, "user");
              Location l = user.getLocation();

              String clickJava = formatLocationVariables(JAVA_FORMAT, l, false);
              String clickJavaRounded = formatLocationVariables(JAVA_FORMAT, l, true);
              String clickCmd = formatLocationVariables(TP_EXACT_FORMAT, l, false);
              String clickCmdRounded = formatLocationVariables(TP_EXACT_FORMAT, l, true);

              Component tpExactRounded = toolButton("/tp_exact; rounded", clickCmdRounded);
              Component tpExact = toolButton("/tp_exact", clickCmd);
              Component javaConstructor = toolButton("Java", clickJava);
              Component javaConstructorRounded = toolButton("Java; rounded", clickJavaRounded);

              c.getSource().sendMessage(
                  Text.format(
                      """
                      Location of: &e{0, user}&r:
                      x: &e{1}&r
                      y: &e{2}&r
                      z: &e{3}&r
                      yaw: &e{4}&r
                      pitch: &e{5}&r
                      world: '&e{6}&r'
                      tools: {7} {8} {9} {10}""",

                      NamedTextColor.GRAY,

                      user,
                      l.getX(), l.getY(), l.getZ(),
                      l.getYaw(),
                      l.getPitch(),
                      l.getWorld().getName(),

                      tpExact,
                      tpExactRounded,
                      javaConstructor,
                      javaConstructorRounded
                  )
              );

              return 0;
            })
        );
  }

  private static Component toolButton(String display, String copyText) {
    return text()
        .content("[" + display + "]")
        .color(NamedTextColor.AQUA)

        .clickEvent(ClickEvent.suggestCommand(copyText))
        .insertion(copyText)
        .hoverEvent(
            text()
                .content("Chat insertion: ")
                .color(NamedTextColor.GRAY)
                .append(text(copyText, NamedTextColor.WHITE))
                .build()
        )

        .build();
  }

  private static String formatLocationVariables(String format, Location l, boolean round) {
    return String.format(format,
        l.getWorld().getName(),
        round ? Math.floor(l.getX()) + 0.5D : l.getX(),
        l.getY(),
        round ? Math.floor(l.getZ()) + 0.5D : l.getZ(),
        round ? roundRotation(l.getYaw()) : l.getYaw(),
        round ? roundRotation(l.getPitch()) : l.getPitch()
    );
  }

  private static float roundRotation(float value) {
    float[] roundValues = {-180, -135, -90, -45, 0, 45, 90, 135, 180};
    float roundRange = 45.0f / 2;

    for (float roundValue : roundValues) {
      float min = roundValue - roundRange;
      float max = roundValue + roundRange;

      if (value >= min && value <= max) {
        return roundValue;
      }
    }

    return value;
  }
}
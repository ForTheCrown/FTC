package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.Random;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandRoll extends FtcCommand {

  private final Random random;

  static final int DEFAULT_MIN = 0;
  static final int DEFAULT_MAX = 10_000;

  public CommandRoll() {
    super("roll");

    this.random = new Random();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> roll(c.getSource(), DEFAULT_MIN, DEFAULT_MAX))

        .then(argument("min", IntegerArgumentType.integer())
            .executes(c -> {
              int min = c.getArgument("min", Integer.class);
              return roll(c.getSource(), min, 0);
            })

            .then(argument("max", IntegerArgumentType.integer())
                .executes(c -> {
                  int min = c.getArgument("min", Integer.class);
                  int max = c.getArgument("max", Integer.class);
                  return roll(c.getSource(), min, max);
                })
            )
        );
  }

  private int roll(CommandSource source, Integer min, Integer max) {
    int minI = min == null ? Integer.MIN_VALUE : min;
    int maxI = max == null ? Integer.MAX_VALUE : max;

    int rolled = random.nextInt(Math.min(minI, maxI), Math.max(minI, maxI));

    source.sendMessage(
        Text.format("Rolled number: &e{0, number}&r.", NamedTextColor.GRAY, rolled)
    );

    return 0;
  }
}

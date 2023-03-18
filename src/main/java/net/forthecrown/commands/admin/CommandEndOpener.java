package net.forthecrown.commands.admin;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.EndOpener;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.EndConfig;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.world.level.Level;

public class CommandEndOpener extends FtcCommand {

  public CommandEndOpener() {
    super("EndOpener");

    setPermission(Permissions.ADMIN);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Modifies the end opener/closer
   *
   * Valid usages of command:
   * /EndOpener
   *
   * Permissions used:
   * ftc.admin
   *
   * Main Author: Julie
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("regen")
            .executes(c -> {
              c.getSource().sendSuccess(text("Starting remake"));

              c.getSource().sendSuccess(text("Starting end regeneration"));
              EndOpener opener = EndOpener.get();

              opener.regenerateEnd()
                  .whenComplete((unused, throwable) -> {
                    c.getSource().sendSuccess(text("Regenerated end"));
                  });

              return 0;
            })
        )

        .then(literal("close")
            .executes(c -> setOpen(c, false))
        )
        .then(literal("open")
            .executes(c -> setOpen(c, true))
        )

        .then(accessorArg(OpenerAccessor.CLOSE_MESSAGE))
        .then(accessorArg(OpenerAccessor.OPEN_MESSAGE))
        .then(accessorArg(OpenerAccessor.LEVER_POS))
        .then(accessorArg(OpenerAccessor.ENABLED))
        .then(accessorArg(OpenerAccessor.SIZE));
  }

  int setOpen(CommandContext<CommandSource> c, boolean open) {
    EndOpener opener = EndOpener.get();
    opener.setOpen(open);

    c.getSource().sendSuccess(text((open ? "Opened" : "Closed") + " The End"));
    return 0;
  }

  private <T> LiteralArgumentBuilder<CommandSource> accessorArg(OpenerAccessor<T> p) {
    return literal(p.getName())
        .executes(c -> {
          Component display = text("End opener " + p.getName() + ": ")
              .append(p.display());

          c.getSource().sendMessage(display);
          return 0;
        })

        .then(argument("val", p.getType())
            .executes(c -> {
              T val = c.getArgument("val", p.getTypeClass());
              p.set(val, c);

              c.getSource().sendSuccess(
                  text("Set end opener " + p.getName() + " to ")
                      .append(p.display())
              );
              return 0;
            })
        );
  }

  private interface OpenerAccessor<T> {

    OpenerAccessor<Integer> SIZE = new OpenerAccessor<Integer>() {
      @Override
      public String getName() {
        return "next_size";
      }

      @Override
      public ArgumentType<Integer> getType() {
        return IntegerArgumentType.integer(1, Level.MAX_LEVEL_SIZE - 12);
      }

      @Override
      public Class<Integer> getTypeClass() {
        return Integer.class;
      }

      @Override
      public Component display() {
        return text(EndConfig.nextSize);
      }

      @Override
      public void set(Integer val, CommandContext<CommandSource> c) {
        EndConfig.nextSize = val;
      }
    };

    OpenerAccessor<Boolean> ENABLED = new OpenerAccessor<>() {
      @Override
      public String getName() {
        return "enabled";
      }

      @Override
      public ArgumentType<Boolean> getType() {
        return BoolArgumentType.bool();
      }

      @Override
      public Class<Boolean> getTypeClass() {
        return Boolean.class;
      }

      @Override
      public Component display() {
        return text(EndConfig.enabled);
      }

      @Override
      public void set(Boolean val, CommandContext<CommandSource> c) {
        EndConfig.enabled = val;
      }
    };

    OpenerAccessor<ParsedPosition> LEVER_POS = new OpenerAccessor<>() {
      @Override
      public String getName() {
        return "leverPos";
      }

      @Override
      public ArgumentType<ParsedPosition> getType() {
        return ArgumentTypes.position();
      }

      @Override
      public Class<ParsedPosition> getTypeClass() {
        return ParsedPosition.class;
      }

      @Override
      public Component display() {
        return Text.clickableLocation(EndConfig.leverPos.toLocation(), false);
      }

      @Override
      public void set(ParsedPosition val, CommandContext<CommandSource> c) {
        WorldVec3i vec = WorldVec3i.of(val.apply(c.getSource()));
        EndConfig.leverPos = vec;
      }
    };

    OpenerAccessor<Component> CLOSE_MESSAGE = new ComponentAccessor() {
      @Override
      public String getName() {
        return "closeMessage";
      }

      @Override
      public Component display() {
        return EndConfig.closeMessage;
      }

      @Override
      public void set(Component val, CommandContext<CommandSource> c) {
        EndConfig.closeMessage = val;
      }
    };

    OpenerAccessor<Component> OPEN_MESSAGE = new ComponentAccessor() {
      @Override
      public String getName() {
        return "openMessage";
      }

      @Override
      public Component display() {
        return EndConfig.openMessage;
      }

      @Override
      public void set(Component val, CommandContext<CommandSource> c) {
        EndConfig.openMessage = val;
      }
    };

    String getName();

    ArgumentType<T> getType();

    Class<T> getTypeClass();

    Component display();

    void set(T val, CommandContext<CommandSource> c);
  }

  interface ComponentAccessor extends OpenerAccessor<Component> {

    @Override
    default ArgumentType<Component> getType() {
      return Arguments.CHAT;
    }

    @Override
    default Class<Component> getTypeClass() {
      return Component.class;
    }
  }
}
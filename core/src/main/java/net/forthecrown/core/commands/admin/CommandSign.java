package net.forthecrown.core.commands.admin;

import static net.forthecrown.grenadier.Grenadier.toMessage;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.chat.MessageSuggestions;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;

public class CommandSign extends FtcCommand {

  private final Map<UUID, SignCopy> copies = new Object2ObjectOpenHashMap<>();

  private final Map<String, SignType> types = Map.ofEntries(
      entry("oak"),
      entry("spruce"),
      entry("birch"),
      entry("jungle"),
      entry("acacia"),
      entry("dark_oak"),
      entry("mangrove"),
      entry("crimson"),
      entry("warped"),
      entry("cherry"),
      entry("bamboo")
  );

  public CommandSign() {
    super("sign");

    setAliases("editsign");
    setDescription("Allows you to edit a sign");

    register();
  }

  private static Entry<String, SignType> entry(String name) {
    Material wallSign = Material.matchMaterial(name + "_wall_sign");
    Material sign = Material.matchMaterial(name + "_sign");
    Material hanging = Material.matchMaterial(name + "_hanging_sign");

    Objects.requireNonNull(wallSign);
    Objects.requireNonNull(sign);
    Objects.requireNonNull(hanging);

    return entry(name, wallSign, sign, hanging);
  }

  private static Entry<String, SignType> entry(String name, Material wall, Material normal, Material hanging) {
    return Map.entry(name, new SignType(normal, wall, hanging));
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    var prefixed = factory.withPrefix("<pos: x,y,z>");

    prefixed.usage("clear", "Clears the sign of all text");
    prefixed.usage("copy", "Copies the sign's content");
    prefixed.usage("paste", "Pastes your copied sign contents onto a sign");
    prefixed.usage("<line: number(1..4)> <text>", "Sets a sign's <line> to <text>");
    prefixed.usage("<line: number(1..4)> -clear", "Clears <line>");
    prefixed.usage("type <type>", "Sets the sign's type");
    prefixed.usage("glow <true | false>", "Makes a sign glow/not glow");
    prefixed.usage("waxed <true | false>", "Sets a sign to be waxed or not");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("pos", ArgumentTypes.blockPosition())
            .then(literal("clear")
                .executes(c -> {
                  SignInfo sign = get(c);

                  SignCopy.EMPTY.apply(sign.side);
                  sign.update();

                  c.getSource().sendSuccess(
                      text("Cleared")
                          .append(sign.displayName().color(NamedTextColor.YELLOW))
                  );
                  return 0;
                })
            )

            .then(literal("glow")
                .then(argument("glow_state", BoolArgumentType.bool())
                    .executes(c -> {
                      SignInfo sign = get(c);

                      boolean glowing
                          = c.getArgument("glow_state", Boolean.class);

                      sign.side.setGlowingText(glowing);
                      sign.update();

                      c.getSource().sendSuccess(
                          Text.format("Set &e{0}&r glowing: &e{1}&r", sign.displayName(), glowing)
                      );
                      return 0;
                    })
                )
            )

            .then(literal("waxed")
                .then(argument("waxed_state", BoolArgumentType.bool())
                    .executes(c -> {
                      SignInfo sign = get(c);
                      boolean waxed = c.getArgument("waxed_state", Boolean.class);

                      sign.sign.setWaxed(waxed);
                      sign.update();

                      c.getSource().sendSuccess(
                          Text.format("Set &e{0}&r waxed &e{1}&r.", sign.displayName(), waxed)
                      );
                      return 0;
                    })
                )
            )

            .then(literal("type")
                .then(argument("type", ArgumentTypes.map(types))
                    .executes(c -> {
                      SignInfo sign = get(c);
                      SignType type = c.getArgument("type", SignType.class);

                      type.apply(sign.sign());

                      c.getSource().sendSuccess(
                          Text.format("Set &e{0}&r type to &e{1}&r.",
                              sign.displayName(),
                              type.name()
                          )
                      );
                      return 0;
                    })
                )
            )

            .then(literal("color")
                .then(argument("color", ArgumentTypes.enumType(DyeColor.class))
                    .executes(c -> {
                      SignInfo info = get(c);
                      DyeColor color = c.getArgument("color", DyeColor.class);

                      info.side.setColor(color);
                      info.update();

                      c.getSource().sendSuccess(
                          Text.format("Changed &e{0}&r color to &e{1}&r.",
                              info.displayName(),
                              color.name().toLowerCase()
                          )
                      );
                      return 0;
                    })
                )
            )

            .then(literal("copy")
                .executes(c -> {
                  User user = getUserSender(c);
                  SignInfo sign = get(c);

                  SignCopy lines = new SignCopy(sign.side());
                  copies.put(user.getUniqueId(), lines);

                  user.sendMessage(
                      text("Copied ")
                          .color(NamedTextColor.GRAY)
                          .append(sign.displayName().color(NamedTextColor.YELLOW))
                  );

                  return 0;
                })
            )

            .then(literal("paste")
                .executes(c -> {
                  User user = getUserSender(c);
                  SignInfo sign = get(c);

                  SignCopy lines = copies.get(user.getUniqueId());
                  if (lines == null) {
                    throw Exceptions.create("You have no sign copied");
                  }

                  lines.apply(sign.side());
                  sign.update();

                  user.sendMessage(text("Pasted sign"));
                  return 0;
                })
            )

            .then(argument("index", IntegerArgumentType.integer(1, 4))
                .suggests((context, builder) -> {
                  return Completions.suggest(builder, "1", "2", "3", "4");
                })

                .then(literal("set")
                    .then(argument("line", Arguments.CHAT)
                        .suggests((c, b) -> {
                          SignInfo sign = get(c);
                          int line = c.getArgument("index", Integer.class);
                          var token = b.getRemainingLowerCase();

                          var lineComponent = sign.side().line(line - 1);

                          String lineText = LegacyComponentSerializer.legacyAmpersand()
                              .serialize(lineComponent);

                          if (Completions.matches(token, lineText)) {
                            b.suggest(lineText, toMessage(lineComponent));
                            return b.buildFuture();
                          }

                          return MessageSuggestions.get(
                              c, b, true,
                              (builder, source) -> Completions.suggest(builder, "-clear")
                          );
                        })

                        .executes(c -> set(c, Arguments.getMessage(c, "line").asComponent()))
                    )
                )

                .then(literal("clear")
                    .executes(c -> set(c, Messages.DASH_CLEAR))
                )
            )
        );
  }

  private int set(CommandContext<CommandSource> c, Component text) throws CommandSyntaxException {
    int index = c.getArgument("index", Integer.class);
    SignInfo info = get(c);
    SignSide sign = info.side();

    if (Text.isDashClear(text)) {
      sign.line(index - 1, Component.empty());

      c.getSource().sendSuccess(
          Text.format("Cleared &e{0}&r line &e{1, number}&r.", info.displayName(), index)
      );
    } else {
      sign.line(index - 1, text);

      c.getSource().sendSuccess(
          Text.format("Set &e{0}&r line {1, number} to '&e{2}&r'", info.displayName(), index, text)
      );
    }

    info.update();
    return 0;
  }

  private SignInfo get(CommandContext<CommandSource> c) throws CommandSyntaxException {
    Location l = ArgumentTypes.getLocation(c, "pos");

    if (!(l.getBlock().getState() instanceof Sign)) {
      throw CoreExceptions.notSign(l);
    }

    Sign sign = (Sign) l.getBlock().getState();
    Side side;

    if (c.getSource().isEntity()) {
      final int maxDist = 5;
      final int maxDistSq = maxDist * maxDist;

      var loc = c.getSource().getLocation();

      if (loc.distanceSquared(sign.getLocation()) > maxDistSq) {
        side = Side.FRONT;
      } else {
        side = sign.getInteractableSideFor(loc);
      }
    } else {
      side = Side.FRONT;
    }

    SignSide signSide = sign.getSide(side);
    return new SignInfo(sign, signSide, side);
  }

  record SignInfo(Sign sign, SignSide side, Side sideEnum) {

    public void update() {
      sign.update(false, false);
    }

    private TextColor dyeColor() {
      return TextColor.color(side.getColor().getColor().asRGB());
    }

    public Component displayName() {
      var dyeColor = dyeColor();

      return text()
          .append(text("["))
          .append(Component.translatable(sign.getType()))
          .append(text("'s " + Text.prettyEnumName(sideEnum) + " Side"))
          .append(text("]"))

          .hoverEvent(
              text()
                  .append(Text.prettyLocation(sign.getLocation(), true))

                  .append(newline())
                  .append(text(Text.prettyEnumName(sideEnum) + " Side"))

                  .append(newline(), side.line(0).colorIfAbsent(dyeColor))
                  .append(newline(), side.line(1).colorIfAbsent(dyeColor))
                  .append(newline(), side.line(2).colorIfAbsent(dyeColor))
                  .append(newline(), side.line(3).colorIfAbsent(dyeColor))

                  .build()
          )
          .clickEvent(
              ClickEvent.suggestCommand(
                  String.format("/sign %s %s %s ", sign.getX(), sign.getY(), sign.getZ())
              )
          )

          .build();
    }
  }

  /**
   * A small data class to store the data written on a sign or data which can be applied to a sign
   */
  record SignCopy(
      Component line0,
      Component line1,
      Component line2,
      Component line3,
      DyeColor dyeColor,
      boolean glowing
  ) {

    public static final SignCopy EMPTY = new SignCopy(
        Component.empty(),
        Component.empty(),
        Component.empty(),
        Component.empty(),
        null,
        false
    );

    public SignCopy(SignSide side) {
      this(
          side.line(0),
          side.line(1),
          side.line(2),
          side.line(3),
          side.getColor(),
          side.isGlowingText()
      );
    }

    public void apply(SignSide side) {
      side.line(0, emptyIfNull(line0));
      side.line(1, emptyIfNull(line1));
      side.line(2, emptyIfNull(line2));
      side.line(3, emptyIfNull(line3));

      side.setGlowingText(glowing);
      side.setColor(Objects.requireNonNullElse(dyeColor, DyeColor.BLACK));
    }

    private static Component emptyIfNull(Component component) {
      return component == null ? Component.empty() : component;
    }
  }

  record SignType(Material sign, Material wallSign, Material hanging) {

    public String name() {
      return sign.name()
          .toLowerCase()
          .replaceAll("_sign", "");
    }

    public void apply(Sign sign) {
      var data = sign.getBlockData();

      if (data instanceof WallSign wallData) {
        WallSign newData = (WallSign) wallSign.createBlockData();
        newData.setFacing(wallData.getFacing());
        newData.setWaterlogged(wallData.isWaterlogged());

        sign.setBlockData(newData);
      } else if (data instanceof HangingSign hanging) {
        HangingSign newData = (HangingSign) this.hanging.createBlockData();
        newData.setRotation(hanging.getRotation());
        newData.setWaterlogged(hanging.isWaterlogged());
        newData.setAttached(hanging.isAttached());

        sign.setBlockData(newData);
      } else if (data instanceof org.bukkit.block.data.type.Sign signData) {
        org.bukkit.block.data.type.Sign newData
            = (org.bukkit.block.data.type.Sign) sign().createBlockData();

        newData.setWaterlogged(signData.isWaterlogged());
        newData.setRotation(signData.getRotation());

        sign.setBlockData(newData);
      }

      sign.update(true, false);
    }
  }
}
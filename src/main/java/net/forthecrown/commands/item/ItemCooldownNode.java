package net.forthecrown.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.RegistryArgument;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Registry;

public class ItemCooldownNode extends ItemModifierNode {

  private static final RegistryArgument<Material> MATERIAL_ARG
      = RegistryArgument.registry(Registry.MATERIAL, "Material");

  public ItemCooldownNode() {
    super("item_cooldown", "itemcooldown");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    var prefixed = factory.withPrefix("<material>");

    prefixed.usage("")
        .addInfo("Shows how long a <material> is on cooldown for you");

    prefixed.usage("<time>")
        .addInfo("Sets your <material>'s cooldown to <time>");
  }

  @Override
  String getArgumentName() {
    return "cooldown";
  }

  @Override
  public void create(LiteralArgumentBuilder<CommandSource> command) {
    command.then(argument("material", MATERIAL_ARG)
        .executes(c -> {
          var player = c.getSource().asPlayer();
          Material material = c.getArgument("material", Material.class);

          if (!player.hasCooldown(material)) {
            throw Exceptions.format("You do not have a cooldown for {0}",
                material
            );
          }

          int remainingTicks = player.getCooldown(material);

          c.getSource().sendMessage(
              Text.format(
                  "Material &e{0}&r remaining cooldown: &e{1, time, -ticks}&r.",
                  NamedTextColor.GRAY,
                  material,
                  remainingTicks
              )
          );
          return 0;
        })

        .then(argument("cooldown", TimeArgument.time())
            .executes(c -> {
              var player = c.getSource().asPlayer();
              Material material = c.getArgument("material", Material.class);
              long cooldownTicks = TimeArgument.getTicks(c, "cooldown");

              player.setCooldown(material, (int) cooldownTicks);

              c.getSource().sendMessage(
                  Text.format(
                      "Set &e{0}&r's cooldown to &e{1, time, -ticks -short}&r.",
                      NamedTextColor.GRAY,
                      material,
                      cooldownTicks
                  )
              );
              return 0;
            })
        )
    );
  }
}
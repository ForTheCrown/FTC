package net.forthecrown.commands.admin;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Collection;
import javax.annotation.Nullable;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.utils.text.Text;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class CommandHologram extends FtcCommand {

  public CommandHologram() {
    super("hologram");

    setPermission(Permissions.ADMIN);
    register();
  }

  public static final NamespacedKey HOLOGRAM_KEY = new NamespacedKey(FTC.getPlugin(), "hologram");

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Creates an invisible armorstand to create a holographic text effect
   *
   * Note: Use {NL} To make a new line in the <text> argument
   *
   * Valid usages of command:
   * - /hologram <location> <text>
   * - /hologram <text>
   *
   * Main Author: Julie
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("remove")
            .then(argument("holograms", ArgumentTypes.entities())
                .executes(c -> {
                  Collection<Entity> entities = ArgumentTypes.getEntities(c, "holograms");

                  int removed = 0;
                  for (Entity e : entities) {
                    if (!(e instanceof ArmorStand)) {
                      continue;
                    }

                    if (!e.getPersistentDataContainer()
                        .has(HOLOGRAM_KEY, PersistentDataType.BYTE)
                    ) {
                      continue;
                    }

                    e.remove();
                    removed++;
                  }

                  c.getSource().sendSuccess(text("Removed " + removed + " holograms"));
                  return removed;
                })
            )
        )

        .then(literal("create")
            .then(argument("text", StringArgumentType.greedyString())
                .executes(c -> {
                  Player p = c.getSource().asPlayer();
                  createHologram(c.getSource(), p.getLocation(),
                      c.getArgument("text", String.class));
                  return 0;
                })
            )

            .then(argument("location", ArgumentTypes.position())
                .then(argument("text", StringArgumentType.greedyString())
                    .executes(c -> {
                      Location loc = ArgumentTypes.getLocation(c, "location");
                      createHologram(c.getSource(), loc, c.getArgument("text", String.class));
                      return 0;
                    })
                )
            )
        );
  }

  public static void createHologram(@Nullable CommandSource source, Location location,
                                    String input
  ) {
    String[] names = input.split("\\{NL}");
    location.add(0, (names.length - 1) * 0.25, 0);

    for (String name : names) {
      if (!name.isBlank()) {
        ArmorStand stand = (ArmorStand) location.getWorld()
            .spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setAI(false);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(true);
        stand.customName(Text.renderString(name));

        stand.getPersistentDataContainer().set(HOLOGRAM_KEY, PersistentDataType.BYTE, (byte) 1);
      }

      location.subtract(0, 0.25, 0);
    }

    if (source != null) {
      source.sendSuccess(text("Created hologram(s) named:"));
      source.sendSuccess(text(input.replaceAll("\\{NL}", "\n")));
    }
  }
}
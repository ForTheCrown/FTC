package net.forthecrown.usables.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.forthecrown.Loggers;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Text;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.utils.io.Results;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class TestWorld implements Condition {

  public static final ObjectType<TestWorld> TYPE = BuiltType.<TestWorld>builder()
      .parser((reader, source) -> new TestWorld(ArgumentTypes.world().parse(reader)))
      .suggester((context, builder) -> ArgumentTypes.world().listSuggestions(context,builder))

      .saver((value, ops) -> {
        if (value.world == null) {
          return Results.error("No world set???");
        }
        return FtcCodecs.NAMESPACED_KEY.encodeStart(ops, value.world);
      })

      .loader(dynamic -> {
        if (dynamic == null) {
          return Results.error("No data given");
        }

        return dynamic.decode(FtcCodecs.NAMESPACED_KEY)
            .map(Pair::getFirst)
            .flatMap(key -> {
              World world = Bukkit.getWorld(key);
              if (world == null) {
                return Results.error("No world named '%s'", key);
              }
              return DataResult.success(world);
            })
            .map(TestWorld::new);
      })

      .build();

  private final NamespacedKey world;

  public TestWorld(World world) {
    if (world == null) {
      Loggers.getLogger().warn("Found unknown world while creating world usage test!");
      this.world = null;
    } else {
      this.world = world.getKey();
    }
  }

  @Override
  public boolean test(Interaction interaction) {
    if (world == null) {
      return false;
    }

    return interaction.player().getWorld().getKey().equals(world);
  }

  @Override
  public Component failMessage(Interaction interaction) {
    if (world == null) {
      return Component.text("Cannot use this in this world", NamedTextColor.GRAY);
    }

    return Text.format("Can only use this in the {0}",
        NamedTextColor.GRAY,
        Text.formatWorldName(Bukkit.getWorld(world))
    );
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(world.asString());
  }
}

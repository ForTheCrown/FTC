package net.forthecrown.usables.conditions;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import net.forthecrown.text.Text;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.utils.io.FtcCodecs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class TestOneUse implements Condition {

  public static final ObjectType<TestOneUse> TYPE = BuiltType.<TestOneUse>builder()
      .emptyFactory(TestOneUse::new)
      .loader(dynamic -> {
        return dynamic.readList(FtcCodecs.UUID_CODEC).map(uuids -> {
          TestOneUse use = new TestOneUse();
          use.used.addAll(uuids);
          return use;
        });
      })
      .saver((value, ops) -> {
        var list = ops.listBuilder();
        for (UUID uuid : value.used) {
          list.add(FtcCodecs.UUID_CODEC.encodeStart(ops, uuid));
        }
        return list.build(ops.empty());
      })
      .build();

  private final Set<UUID> used = new ObjectOpenHashSet<>();

  @Override
  public boolean test(Interaction interaction) {
    return !used.contains(interaction.playerId());
  }

  @Override
  public void afterTests(Interaction interaction) {
    used.add(interaction.playerId());
  }

  @Override
  public Component failMessage(Interaction interaction) {
    return Component.text("You can only use this once!", NamedTextColor.GRAY);
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    if (used.isEmpty()) {
      return null;
    }

    return Text.format("usedCount={0, number}", used.size());
  }
}

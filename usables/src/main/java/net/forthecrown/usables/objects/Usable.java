package net.forthecrown.usables.objects;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.TypeIds;
import net.forthecrown.text.TextWriter;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.ComponentList;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.Usables;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.TagOps;
import org.slf4j.Logger;

@Getter
public abstract class Usable implements ConditionHolder {

  public static final Logger LOGGER = Loggers.getLogger();

  public static final String KEY_CONDITIONS = "checks";
  public static final String KEY_ACTIONS = "actions";

  @Setter
  private boolean silent;

  private final ComponentList<Condition> conditions;
  private final ComponentList<Action> actions;

  public Usable() {
    this.conditions = ComponentList.newConditionList();
    this.actions = ComponentList.newActionList();
  }

  public void clear() {
    conditions.clear();
    actions.clear();
  }

  @Override
  public void fillContext(Map<String, Object> context) {
    context.put("silent", silent);
  }

  @Override
  public boolean interact(Interaction interaction) {
    if (!runConditions(interaction)) {
      return false;
    }

    Usables.runActions(actions, interaction);
    return true;
  }

  @Override
  public void save(CompoundTag tag) {
    tag.putBoolean("silent", silent);

    save(conditions, tag, KEY_CONDITIONS);
    save(actions, tag, KEY_ACTIONS);
  }

  static void save(ComponentList<?> list, CompoundTag container, String key) {
    list.save(TagOps.OPS)
        .flatMap(binaryTag -> {
          if (binaryTag == null || binaryTag.getId() == TypeIds.END) {
            return Results.error("TAG_End found as save() result????");
          }
          return DataResult.success(binaryTag);
        })
        .mapError(s -> "Failed to save " + key + ": " + s)
        .resultOrPartial(LOGGER::error)
        .ifPresent(binaryTag -> container.put(key, binaryTag));
  }

  static void load(BinaryTag tag, ComponentList<?> list) {
    if (tag == null) {
      list.clear();
      return;
    }

    list.load(new Dynamic<>(TagOps.OPS, tag));
  }

  @Override
  public void load(CompoundTag tag) {
    this.silent = tag.getBoolean("silent");

    load(tag.get(KEY_ACTIONS), actions);
    load(tag.get(KEY_CONDITIONS), conditions);
  }

  @Override
  public void write(TextWriter writer) {
    writer.field("Silent", silent);
    writer.field("Conditions");
    conditions.write(writer, getCommandPrefix() + " tests");

    writer.field("Actions");
    actions.write(writer, getCommandPrefix() + " actions");
  }
}

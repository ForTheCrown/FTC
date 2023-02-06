package net.forthecrown.useables.util;

import static net.minecraft.nbt.Tag.TAG_STRING;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.script2.Script;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.UsableTrigger;
import net.forthecrown.useables.UsageTypeHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang.ArrayUtils;

public class UsablesScripts {
  private UsablesScripts() {}

  public static Script getScript(String name,
                                 UsageTypeHolder holder,
                                 String... args
  ) {
    Script script = Script.of(name).compile(args);

    script.put("_holder", holder);

    holder.as(UsableTrigger.class).ifPresent(trigger -> {
      script.put("_bounds", trigger.getBounds());
    });

    holder.as(UsableBlock.class).ifPresent(block -> {
      script.put("_location", block.getBlock().getLocation());
      script.put("_block", block.getBlock());
    });

    holder.as(UsableEntity.class).ifPresent(entity -> {
      var ent = entity.getEntity();
      script.put("_entity", ent);
      script.put("_location", ent.getLocation());
    });

    script.eval().throwIfError();
    return script;
  }

  public static <T> T parseScript(StringReader reader,
                                  ScriptFactory<T> factory
  ) throws CommandSyntaxException {
    String script = Arguments.SCRIPT.parse(reader);
    String[] args = ArrayUtils.EMPTY_STRING_ARRAY;

    reader.skipWhitespace();

    if (reader.canRead()) {
      String remaining = reader.getRemaining().trim();
      reader.setCursor(reader.getTotalLength());
      args = remaining.split("\s");
    }

    return factory.newInstance(script, args);
  }

  public static <T> T loadScript(Tag tag,
                                 ScriptFactory<T> factory
  ) {
    if (tag.getId() == TAG_STRING) {
      return factory.newInstance(
          tag.getAsString(),
          ArrayUtils.EMPTY_STRING_ARRAY
      );
    }

    CompoundTag compound = (CompoundTag) tag;
    String script = compound.getString("script");

    String[] args = compound.getList("args", TAG_STRING)
        .stream()
        .map(Tag::getAsString)
        .toArray(String[]::new);

    return factory.newInstance(script, args);
  }

  public interface ScriptFactory<T> {
    T newInstance(String scriptName, String[] args);
  }
}
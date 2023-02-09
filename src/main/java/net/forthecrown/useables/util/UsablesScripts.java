package net.forthecrown.useables.util;

import static net.minecraft.nbt.Tag.TAG_STRING;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.script2.Script;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.UsableScriptHolder;
import net.forthecrown.useables.UsableTrigger;
import net.forthecrown.useables.UsageTypeHolder;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang.ArrayUtils;

public class UsablesScripts {
  private UsablesScripts() {}

  public static Script getScript(UsageTypeHolder holder,
                                 UsableScriptHolder scriptHolder
  ) {
    var name = scriptHolder.getScriptName();
    var args = scriptHolder.getArgs();

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

    Supplier<Object> getDataObject = scriptHolder::getDataObject;
    Consumer<Object> setDataObject = scriptHolder::setDataObject;

    script.put("getDataObject", getDataObject);
    script.put("setDataObject", setDataObject);

    script.eval().throwIfError();
    return script;
  }

  public static CompoundTag saveScript(UsableScriptHolder holder) {
    CompoundTag tag = new CompoundTag();
    tag.putString("script", holder.getScriptName());
    tag.put("args", TagUtil.writeArray(holder.getArgs(), StringTag::valueOf));

    if (!Strings.isNullOrEmpty(holder.getDataString())) {
      tag.putString("dataJson", holder.getDataString());
    }

    return tag;
  }

  public static <T extends UsableScriptHolder> T parseScript(
      StringReader reader,
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

  public static <T extends UsableScriptHolder> T loadScript(
      Tag tag,
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

    var instance = factory.newInstance(script, args);

    if (compound.contains("dataJson")) {
      instance.setDataString(compound.getString("dataJson"));
    } else {
      instance.setDataString(null);
    }

    return instance;
  }

  public interface ScriptFactory<T extends UsableScriptHolder> {
    T newInstance(String scriptName, String[] args);
  }
}
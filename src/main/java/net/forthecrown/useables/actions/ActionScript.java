package net.forthecrown.useables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.script2.Script;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.ActionHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.UsageTypeHolder;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionScript extends UsageAction {

  public static final UsageType<ActionScript> TYPE = UsageType.of(ActionScript.class)
      .setSuggests(Arguments.SCRIPT::listSuggestions);

  private final String script;

  public ActionScript(String script) {
    super(TYPE);
    this.script = script;
  }

  @Override
  public void onUse(Player player, ActionHolder holder) {
    try (var _script = getScript(script, holder)) {
      _script.eval();
      _script.invoke("onUse", Users.get(player));
    }
  }

  public static Script getScript(String name, UsageTypeHolder holder) {
    Script _script = Script.of(name).compile();

    _script.put("_holder", holder);

    holder.as(UsableBlock.class).ifPresent(usableEntity -> {
      _script.put("_location", usableEntity.getTileEntity().getLocation());
      _script.put("_block", usableEntity.getBlock());
    });

    holder.as(UsableEntity.class).ifPresent(usableEntity -> {
      var ent = usableEntity.getEntity();
      _script.put("_entity", ent);
      _script.put("_location", ent.getLocation());
    });

    return _script;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(
        String.format("'%s'", script)
    );
  }

  @Override
  public @Nullable Tag save() {
    return StringTag.valueOf(script);
  }

  @UsableConstructor(ConstructType.PARSE)
  public static ActionScript parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    return new ActionScript(Arguments.SCRIPT.parse(reader));
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionScript readTag(Tag tag) {
    return new ActionScript(tag.getAsString());
  }
}
package net.forthecrown.cosmetics;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.ScriptService;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.utils.inventory.Slot;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;
import net.kyori.adventure.text.Component;

public abstract class ScriptedCosmetic extends Cosmetic {

  protected Script script;

  public ScriptedCosmetic(ScriptCosmeticBuilder<?> builder) {
    super(builder);

    Objects.requireNonNull(builder.source, "No 'script' set");

    script = Scripts.newScript(builder.source);
    script.compile();

    script.putConst("_cosmetic", this);
    createBindings(script);

    script.evaluate().throwIfError();
  }

  protected void createBindings(Script script) {

  }

  @Getter @Setter
  @Accessors(chain = true, fluent = true)
  public static abstract class ScriptCosmeticBuilder<T extends ScriptedCosmetic>
      extends AbstractBuilder<T>
  {

    protected Source source;

    @Override
    protected void loadGeneric(JsonWrapper json) {
      super.loadGeneric(json);

      ScriptService service = Scripts.getService();
      source = Sources.loadFromJson(
          json.get("script"),
          service.getScriptsDirectory(),
          true
      );
    }

    @Override
    public ScriptCosmeticBuilder<T> menuSlot(Slot menuSlot) {
      return (ScriptCosmeticBuilder<T>) super.menuSlot(menuSlot);
    }

    @Override
    public ScriptCosmeticBuilder<T> displayName(Component displayName) {
      return (ScriptCosmeticBuilder<T>) super.displayName(displayName);
    }

    @Override
    public ScriptCosmeticBuilder<T> addDescription(Component desc) {
      return (ScriptCosmeticBuilder<T>) super.addDescription(desc);
    }
  }
}
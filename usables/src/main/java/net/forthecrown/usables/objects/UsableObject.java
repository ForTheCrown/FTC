package net.forthecrown.usables.objects;

import java.util.Map;
import java.util.Optional;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.usables.Interaction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface UsableObject {

  default boolean interact(Player player) {
    return interact(createInteraction(player));
  }

  default Interaction createInteraction(Player player) {
    Interaction interaction = Interaction.create(player, this);
    fillContext(interaction.context());
    return interaction;
  }

  boolean interact(Interaction interaction);

  void save(CompoundTag tag);

  void load(CompoundTag tag);

  void write(TextWriter writer);

  default Component displayName() {
    return name()
        .clickEvent(ClickEvent.suggestCommand(getCommandPrefix()))
        .hoverEvent(displayInfo());
  }

  String getCommandPrefix();

  Component name();

  default Component displayInfo() {
    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    write(writer);
    return writer.asComponent();
  }

  default <T extends UsableObject> Optional<T> as(Class<T> type) {
    if (type.isInstance(this)) {
      return Optional.of((T) this);
    }

    return Optional.empty();
  }

  default void fillContext(Map<String, Object> context) {

  }

  default CommandSender getCommandSender() {
    return Bukkit.getConsoleSender();
  }
}

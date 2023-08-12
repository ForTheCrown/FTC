package net.forthecrown.user;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

import com.google.gson.JsonNull;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;

public class Kingship {

  private static final Component KING_TITLE = makeTitle("King");
  private static final Component QUEEN_TITLE = makeTitle("Queen");
  private static final Component MONARCH_TITLE = makeTitle("Monarch");

  private final Path data;

  @Getter @Setter
  private UUID kingId;

  @Getter @Setter
  private GenderPreference preference;

  public Kingship(Path data) {
    this.data = data;
  }

  private static Component makeTitle(String name) {
    return Component.textOfChildren(
        text("[", WHITE, BOLD),
        text(name, YELLOW, BOLD),
        text("] ", WHITE, BOLD)
    );
  }

  public boolean hasKing() {
    return kingId != null;
  }

  public boolean isKing(UUID id) {
    return Objects.equals(kingId, id);
  }

  public void save() {
    SerializationHelper.writeJsonFile(data, this::save);
  }

  public void load() {
    SerializationHelper.readJsonFile(data, this::load);
  }

  public void load(JsonWrapper json) {
    kingId = json.getUUID("king");

    if (json.has("queen")) {
      preference = json.getBool("queen") ? GenderPreference.QUEEN : GenderPreference.KING;
    } else {
      this.preference = json.getEnum("preference", GenderPreference.class, GenderPreference.KING);
    }
  }

  public void save(JsonWrapper json) {
    if (kingId != null) {
      json.addUUID("king", kingId);
    } else {
      json.add("king", JsonNull.INSTANCE);
    }

    json.addEnum("preference", preference);
  }

  public Component getTitle() {
    return switch (preference) {
      case KING -> KING_TITLE;
      case QUEEN -> QUEEN_TITLE;
      case MONARCH -> MONARCH_TITLE;
    };
  }

  public enum GenderPreference {
    KING, QUEEN, MONARCH
  }
}

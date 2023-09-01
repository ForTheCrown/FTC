package net.forthecrown.king;

import com.google.gson.JsonNull;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;

@Getter @Setter
public class Kingship {

  private final Path file;

  private UUID monarchId;
  private MonarchGender gender = MonarchGender.MONARCH;

  public Kingship() {
    this.file = PathUtil.pluginPath("data.json");
  }

  public void save() {
    SerializationHelper.writeJsonFile(file, this::save);
  }

  public void load() {
    SerializationHelper.readJsonFile(file, this::load);
  }

  public void load(JsonWrapper json) {
    monarchId = json.getUUID("king");

    if (json.has("queen")) {
      gender = json.getBool("queen") ? MonarchGender.QUEEN : MonarchGender.KING;
    } else {
      this.gender = json.getEnum("preference", MonarchGender.class, MonarchGender.MONARCH);
    }
  }

  public void save(JsonWrapper json) {
    if (monarchId != null) {
      json.addUUID("king", monarchId);
    } else {
      json.add("king", JsonNull.INSTANCE);
    }

    json.addEnum("preference", gender);
  }

  public boolean hasMonarch() {
    return monarchId != null;
  }

  public boolean isMonarch(UUID id) {
    return Objects.equals(monarchId, id);
  }

  public MonarchGender getGender() {
    return Objects.requireNonNullElse(gender, MonarchGender.MONARCH);
  }

  public Component getPrefix() {
    return getGender().getPrefix();
  }

  public String getTitle() {
    return getGender().getTitle();
  }

  public User getMonarch() {
    if (!hasMonarch()) {
      return null;
    }
    return Users.get(monarchId);
  }
}

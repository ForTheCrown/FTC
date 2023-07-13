package net.forthecrown.core.user;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.UserComponent;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class UnknownComponent implements UserComponent {

  private JsonElement json;

  private String key;

  @Override
  public @Nullable JsonElement serialize() {
    return json;
  }

  @Override
  public void deserialize(@Nullable JsonElement element) {
    this.json = element;
  }
}
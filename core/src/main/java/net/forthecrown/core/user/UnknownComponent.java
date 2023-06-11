package net.forthecrown.core.user;

import com.google.gson.JsonElement;
import net.forthecrown.user.UserComponent;
import org.jetbrains.annotations.Nullable;

public class UnknownComponent implements UserComponent {

  private JsonElement json;

  @Override
  public @Nullable JsonElement serialize() {
    return json;
  }

  @Override
  public void deserialize(@Nullable JsonElement element) {
    this.json = element;
  }
}
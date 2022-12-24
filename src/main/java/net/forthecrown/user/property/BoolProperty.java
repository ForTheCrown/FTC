package net.forthecrown.user.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.forthecrown.user.User;
import org.jetbrains.annotations.NotNull;

public class BoolProperty
    extends UserProperty<Boolean>
    implements UserPreference
{
  public BoolProperty(@NotNull String name, boolean defaultValue) {
    super(name, defaultValue);
  }

  @Override
  public JsonElement serialize(Boolean aBoolean) {
    return new JsonPrimitive(aBoolean);
  }

  @Override
  public Boolean deserialize(JsonElement element) {
    return element.getAsBoolean();
  }

  @Override
  public boolean getState(User user) {
    return user.get(this);
  }

  @Override
  public void setState(User user, boolean state) {
    user.set(this, state);
  }
}
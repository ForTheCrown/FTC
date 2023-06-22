package net.forthecrown.user;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserProperty<T> {

  @NotNull
  T getDefaultValue();

  @Nullable
  PropertyEditCallback<T> getCallback();

  JsonElement serialize(T value);

  T deserialize(JsonElement element);

  int getId();

  String getKey();

  interface PropertyEditCallback<T> {
    void onUpdate(User user, T value);
  }

  interface Builder<T> {

    Builder<T> key(String key);

    Builder<T> callback(PropertyEditCallback<T> callback);

    Builder<T> defaultValue(@NotNull T value);

    UserProperty<T> build();
  }
}
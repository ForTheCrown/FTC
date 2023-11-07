package net.forthecrown.user;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

public interface UserComponent {

  @Nullable JsonElement serialize();

  void deserialize(@Nullable JsonElement element);
}
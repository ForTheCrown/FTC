package net.forthecrown.usables;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public interface UsableComponent {

  default @Nullable Component displayInfo() {
    return null;
  }

  UsageType<? extends UsableComponent> getType();
}

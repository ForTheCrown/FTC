package net.forthecrown.useables;

import java.util.Objects;
import lombok.Getter;
import net.forthecrown.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public abstract class UsageInstance {

  @Getter
  private final UsageType type;

  public UsageInstance(UsageType type) {
    this.type = Objects.requireNonNull(type);

    // Only this class' type is allowed to initialize
    // this class
    Validate.isTrue(
        type.getTypeClass() == getClass(),

        "Invalid type used to initialize %s",
        getClass().getName()
    );
  }

  @Nullable
  public abstract Component displayInfo();

  @Nullable
  public abstract BinaryTag save();
}
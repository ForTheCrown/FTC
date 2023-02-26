package net.forthecrown.useables;

import com.mojang.brigadier.StringReader;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.nbt.BinaryTag;
import org.apache.commons.lang3.ArrayUtils;

@Getter
public enum ConstructType {
  PARSE(StringReader.class, CommandSource.class) {
    @Override
    public void set(UsageType type, UsageType.ReflectionExecutable executable) {
      type.parser = executable;
    }

    @Override
    public UsageType.ReflectionExecutable get(UsageType type) {
      return type.parser;
    }
  },

  TAG(BinaryTag.class) {
    @Override
    public void set(UsageType type, UsageType.ReflectionExecutable executable) {
      type.tagLoader = executable;
    }

    @Override
    public UsageType.ReflectionExecutable get(UsageType type) {
      return type.tagLoader;
    }
  },

  EMPTY {
    @Override
    public void set(UsageType type, UsageType.ReflectionExecutable executable) {
      type.emptyConstructor = executable;
    }

    @Override
    public UsageType.ReflectionExecutable get(UsageType type) {
      return type.emptyConstructor;
    }
  };

  private final Class[] params;
  private final Class[] typedParams;

  ConstructType(Class... params) {
    this.params = params;
    typedParams = ArrayUtils.insert(0, params, UsageType.class);
  }

  public abstract void set(UsageType type, UsageType.ReflectionExecutable executable);

  public abstract UsageType.ReflectionExecutable get(UsageType type);
}
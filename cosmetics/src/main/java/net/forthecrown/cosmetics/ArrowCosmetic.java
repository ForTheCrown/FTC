package net.forthecrown.cosmetics;

import java.util.Objects;
import lombok.Getter;
import net.forthecrown.utils.io.JsonWrapper;
import org.bukkit.Particle;

@Getter
public class ArrowCosmetic extends Cosmetic {

  private final Particle particle;

  public ArrowCosmetic(Builder builder) {
    super(builder);

    this.particle = builder.particle;
    Objects.requireNonNull(particle, "No particle set");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends AbstractBuilder<ArrowCosmetic> {

    private Particle particle;

    @Override
    public void load(JsonWrapper json) {
      loadGeneric(json);

      particle = json.getEnum("particle", Particle.class);
      Objects.requireNonNull(particle, "No 'particle' set");
    }

    @Override
    public ArrowCosmetic build() {
      return new ArrowCosmetic(this);
    }
  }
}
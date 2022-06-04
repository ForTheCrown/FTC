package net.forthecrown.dungeons.mobs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

@RequiredArgsConstructor
public class MobType<T extends Entity> {
    @Getter
    private final Class<T> entityClass;

    private void ensureType(Class<? extends Entity> eClass) {
        Validate.isTrue(eClass.isAssignableFrom(entityClass), "Cannot assign value to type because type entity is not a %s", eClass.getSimpleName());
    }

    private void ensureLiving() {
        ensureType(LivingEntity.class);
    }
}
package net.forthecrown.valhalla.data.triggers.instances;

import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.forthecrown.valhalla.data.triggers.TriggerInstance;
import net.forthecrown.valhalla.data.triggers.functions.EntityFunction;
import net.forthecrown.valhalla.data.triggers.functions.TriggerFunction;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathTrigger implements TriggerInstance<EntityDeathEvent> {
    private final EntityPredicate predicate;

    private final EntityFunction function;
    private final TriggerFunction simpleFunction;

    private final boolean removeAfterExec;

    public EntityDeathTrigger(EntityPredicate predicate, EntityFunction function, TriggerFunction simpleFunction, boolean removeAfterExec) {
        this.predicate = predicate;
        this.function = function;
        this.simpleFunction = simpleFunction;
        this.removeAfterExec = removeAfterExec;
    }

    public EntityPredicate getPredicate() {
        return predicate;
    }

    public EntityFunction getFunction() {
        return function;
    }

    @Override
    public boolean test(EntityDeathEvent event) {
        ServerLevel level = ((CraftWorld) event.getEntity().getWorld()).getHandle();
        Entity entity = ((CraftEntity) event.getEntity()).getHandle();
        Vec3 vec3 = entity.position();

        return predicate.matches(level, vec3, entity);
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        json.add("predicate", predicate.serializeToJson());
        json.add("removeAfterExec", removeAfterExec());
        json.add("function", function.serialize());

        return json.getSource();
    }

    @Override
    public void execute(EntityDeathEvent context, ActiveRaid raid) {
        if(simpleFunction != null) simpleFunction.execute();
        else function.execute(context.getEntity(), raid);
    }

    @Override
    public boolean removeAfterExec() {
        return removeAfterExec;
    }
}

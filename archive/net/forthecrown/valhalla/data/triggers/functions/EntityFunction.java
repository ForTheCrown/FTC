package net.forthecrown.valhalla.data.triggers.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.SerializerType;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.math.MathUtil;
import net.forthecrown.valhalla.Valhalla;
import net.forthecrown.valhalla.VikingUtil;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityFunction implements ParameterizedTriggerFunction<Entity> {
    public static final Key KEY = Valhalla.vikingKey("entity_function");

    public static final SerializerType<EntityFunction> TYPE = new SerializerType<>() {
        @Override
        public EntityFunction deserialize(JsonElement element) {
            JsonBuf json = JsonBuf.of(element.getAsJsonObject());
            Builder builder = new Builder();

            if(json.has("attributeEditors")) {
                for (Map.Entry<String, JsonElement> e: json.getObject("attributeEditors").entrySet()) {
                    Attribute attribute = VikingUtil.attributeFromKey(e.getKey());

                    builder.addAttribute(AttributeFunction.fromJson(e.getValue(), attribute));
                }
            }

            if(json.has("teleport")) {
                builder.teleportLoc(JsonUtils.readPosition(json.getObject("teleport")));
            }

            if(json.has("items")) {
                JsonBuf items = json.getBuf("items");

                for (Map.Entry<String, JsonElement> e: items.entrySet()) {
                    EquipmentSlot slot = EquipmentSlot.valueOf(e.getKey().toUpperCase());

                    builder.addItem(EntityItemFunction.fromJson(e.getValue(), slot));
                }
            }

            if(json.has("cmds")) {
                JsonArray cmds = json.getArray("cmds");

                for (JsonElement e: cmds) {
                    builder.addCommand(EntityCommandFunction.fromJson(e));
                }
            }

            if(json.has("health")) {
                JsonBuf healthBuf = json.getBuf("health");

                builder
                        .setHealth(healthBuf.getDouble("emount"))
                        .setHealthOperation(healthBuf.getEnum("operation", MathOperation.class));
            }

            if(json.has("nbt")) {
                try {
                    CompoundTag tag = TagParser.parseTag(json.getString(json.getString("nbt")));
                    builder.setTag(tag);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return builder.build();
        }

        @Override
        public JsonElement serialize(EntityFunction value) {
            JsonBuf json = JsonBuf.empty();

            if(!ListUtils.isNullOrEmpty(value.attributeFunctions)) {
                JsonBuf editors = JsonBuf.empty();

                for (AttributeFunction e: value.attributeFunctions) {
                    String key = e.getAttribute().getKey().asString();
                    if(editors.has(key)) continue;

                    editors.add(e.getAttribute().getKey().asString(), e);
                }

                json.add("attributeEditors", editors.getSource());
            }

            if(!ListUtils.isNullOrEmpty(value.itemFunctions)) {
                JsonBuf items = JsonBuf.empty();

                for (EntityItemFunction f: value.itemFunctions) {
                    String key = f.getSlot().toString().toLowerCase();
                    if(items.has(key)) continue;

                    items.add(key, f.serialize());
                }

                json.add("items", items.getSource());
            }

            if(!ListUtils.isNullOrEmpty(value.commandFunctions)) {
                json.addList("cmds", value.commandFunctions);
            }

            if(value.teleportLoc != null) json.add("teleport", JsonUtils.writePosition(value.teleportLoc));

            if(value.healthOperation != null && value.health != 0) {
                JsonBuf healthBuf = JsonBuf.empty();

                healthBuf.add("amount", value.health);
                healthBuf.addEnum("operation", value.healthOperation);

                json.add("health", healthBuf);
            }

            if(value.tag != null && !value.tag.isEmpty()) {
                json.add("nbt", value.tag.toString());
            }

            return json.getSource();
        }

        @Override
        public @NotNull Key key() {
            return KEY;
        }
    };

    private final List<AttributeFunction> attributeFunctions;
    private final List<EntityItemFunction> itemFunctions;
    private final List<EntityCommandFunction> commandFunctions;

    private final Position teleportLoc;

    private final double health;
    private final MathOperation healthOperation;

    private final CompoundTag tag;

    public EntityFunction(
            List<AttributeFunction> attributeFunctions,
            List<EntityItemFunction> itemFunctions,
            List<EntityCommandFunction> commandFunctions,
            Position teleportLoc,
            double health, MathOperation healthOperation,
            CompoundTag tag
    ) {
        this.attributeFunctions = attributeFunctions;
        this.itemFunctions = itemFunctions;
        this.commandFunctions = commandFunctions;

        this.teleportLoc = teleportLoc;

        this.health = health;
        this.healthOperation = healthOperation;

        this.tag = tag;
    }

    @Override
    public void execute(Entity entity, ActiveRaid raid) {

        //If we can do things to a living entity, aka modify attributes and health
        if(entity instanceof LivingEntity) {
            LivingEntity ent = (LivingEntity) entity;

            //Attribute stuff
            if(!ListUtils.isNullOrEmpty(attributeFunctions)) {
                for (AttributeFunction e: attributeFunctions) {
                    e.apply(ent, raid);
                }
            }

            //Nomral health stuff
            if(health != 0 && healthOperation != null) {
                double acHealth = health == -1 ? raid.getDifficulty().getModifier() : health;
                double newHealth = healthOperation.apply(acHealth, ent.getHealth());

                //Make sure new health is between 0 and the max health attribute
                newHealth = MathUtil.clamp(newHealth, 0D, ent.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

                ent.setHealth(newHealth);
            }

            //Item and equipment stuff
            if(!ListUtils.isNullOrEmpty(itemFunctions)) {
                EntityEquipment equipment = ent.getEquipment();

                for (EntityItemFunction f: itemFunctions) {
                    f.apply(equipment);
                }
            }
        }

        //Tag stuff, if we have tags
        if(tag != null && !tag.isEmpty()) {
            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();

            CompoundTag existing = nmsEntity.saveWithoutId(new CompoundTag());
            existing.merge(tag.copy());

            nmsEntity.load(existing);
        }

        //Teleport stuff, if we have somewhere to teleport to
        if(teleportLoc != null) {
            entity.teleport(new Location(
                    entity.getWorld(),
                    teleportLoc.x(),
                    teleportLoc.y(),
                    teleportLoc.z()
            ));
        }

        //If we have command stuff, do them
        if(!ListUtils.isNullOrEmpty(commandFunctions)) {
            for (EntityCommandFunction f: commandFunctions) {
                f.execute(entity);
            }
        }
    }

    @Override
    public SerializerType<EntityFunction> serializerType() {
        return TYPE;
    }

    public List<AttributeFunction> getAttributeFunctions() {
        return attributeFunctions;
    }

    public List<EntityItemFunction> getItemFunctions() {
        return itemFunctions;
    }

    public List<EntityCommandFunction> getCommandFunctions() {
        return commandFunctions;
    }

    public Position getTeleportLoc() {
        return teleportLoc;
    }

    public double getHealth() {
        return health;
    }

    public MathOperation getHealthOperation() {
        return healthOperation;
    }

    public CompoundTag getTag() {
        return tag;
    }

    //A class for building the entity function
    public static class Builder {
        private final List<AttributeFunction> attributeFunctions = new ArrayList<>();
        private final List<EntityItemFunction> itemFunctions = new ArrayList<>();
        private final List<EntityCommandFunction> commandFunctions = new ArrayList<>();

        private Position teleportLoc;

        private double health;
        private MathOperation healthOperation;

        private CompoundTag tag;

        public Builder teleportLoc(Position teleportLoc) {
            this.teleportLoc = teleportLoc;
            return this;
        }

        public Builder addItem(EntityItemFunction function) {
            itemFunctions.add(function);
            return this;
        }

        public Builder addAttribute(AttributeFunction editor) {
            attributeFunctions.add(editor);
            return this;
        }

        public Builder addCommand(EntityCommandFunction function) {
            commandFunctions.add(function);
            return this;
        }

        public Builder setHealth(double health) {
            this.health = health;
            return this;
        }

        public Builder setHealthOperation(MathOperation operation) {
            this.healthOperation = operation;
            return this;
        }

        public Builder setTag(CompoundTag tag) {
            this.tag = tag;
            return this;
        }

        public EntityFunction build() {
            return new EntityFunction(
                    attributeFunctions.isEmpty() ? null : attributeFunctions,
                    itemFunctions.isEmpty() ? null : itemFunctions,
                    commandFunctions.isEmpty() ? null : commandFunctions,
                    teleportLoc,
                    health, healthOperation,
                    tag
            );
        }
    }

    //An entity function that executes a command
    public static class EntityCommandFunction implements JsonSerializable {
        private final String command;
        private final boolean asEntity;

        public EntityCommandFunction(String command, boolean asEntity) {
            this.command = command;
            this.asEntity = asEntity;
        }

        public String getCommand() {
            return command;
        }

        public boolean asEntity() {
            return asEntity;
        }

        public void execute(Entity entity) {
            Location l = entity.getLocation();

            //Execute command as either entity or console depending on specification
            //Also replace all placeholders
            Bukkit.dispatchCommand(asEntity ? entity : Bukkit.getConsoleSender(),
                    command
                            .replaceAll("%pos", l.getX() + " " + l.getY() + " " + l.getZ())
                            .replaceAll("%block_pos", l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ())

                            .replaceAll("%name", entity.customName() == null ? entity.getName() : entity.getCustomName())
                            .replaceAll("%uuid", entity.getUniqueId().toString())
            );
        }

        @Override
        public JsonObject serialize() {
            JsonObject json = new JsonObject();

            json.addProperty("command", command);
            json.addProperty("asEntity", asEntity);

            return json;
        }

        public static EntityCommandFunction fromJson(JsonElement element) {
            JsonBuf json = JsonBuf.of(element.getAsJsonObject());;

            return new EntityCommandFunction(
                    json.getString("command"),
                    json.getBool("asEntity")
            );
        }
    }

    public static class AttributeFunction implements JsonSerializable {
        private final Attribute attribute;
        private final double amount;
        private final MathOperation operation;

        public AttributeFunction(Attribute attribute, double amount, MathOperation operation) {
            this.attribute = attribute;
            this.amount = amount;
            this.operation = operation;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public double getAmount() {
            return amount;
        }

        public MathOperation getOperation() {
            return operation;
        }

        public void apply(LivingEntity entity, ActiveRaid raid) {
            AttributeInstance instance = entity.getAttribute(attribute);
            double val = Math.max(0, operation.apply(amount == -1 ? raid.getDifficulty().getModifier() : amount, instance.getValue()));

            instance.setBaseValue(val);
        }

        @Override
        public JsonObject serialize() {
            JsonBuf json = JsonBuf.empty();

            json.addEnum("operation", operation);
            json.add("amount", amount);

            return json.getSource();
        }

        public static AttributeFunction fromJson(JsonElement element, Attribute attribute) {
            JsonBuf json = JsonBuf.of(element.getAsJsonObject());

            return new AttributeFunction(
                    attribute,
                    json.getDouble("amount"),
                    json.getEnum("operation", MathOperation.class)
            );
        }
    }

    public static class EntityItemFunction implements JsonSerializable {
        private final ItemStack item;
        private final EquipmentSlot slot;

        public EntityItemFunction(ItemStack item, EquipmentSlot slot) {
            this.item = item;
            this.slot = slot;
        }

        public ItemStack getItem() {
            return item == null ? null : item.clone();
        }

        public EquipmentSlot getSlot() {
            return slot;
        }

        public void apply(EntityEquipment equipment) {
            equipment.setItem(slot, getItem());
        }

        @Override
        public JsonElement serialize() {
            return item == null ? null : JsonUtils.writeItem(item);
        }

        public static EntityItemFunction fromJson(JsonElement element, EquipmentSlot slot) {
            if(element == null || element.isJsonNull()) return  new EntityItemFunction(null, slot);
            return new EntityItemFunction(JsonUtils.readItem(element), slot);
        }
    }

    //A math operation that can be performed
    public enum MathOperation {
        SET ((d, d2) -> d),
        ADD (Double::sum),
        REMOVE ((d, d2) -> d2 - d),
        MULTIPLY ((d, d2) -> d2*d),
        DIVIDE ((d, d2) -> d2/d);

        private final OperationFunction function;

        MathOperation(OperationFunction function) {
            this.function = function;
        }

        public double apply(double d, double d2) {
            return function.apply(d, d2);
        }
    }

    //The function a math operation uses to apply it self
    public interface OperationFunction {
        /**
         *
         * @param d The given value
         * @param d2 The entity value
         * @return the operation value
         */
        double apply(double d, double d2);
    }
}

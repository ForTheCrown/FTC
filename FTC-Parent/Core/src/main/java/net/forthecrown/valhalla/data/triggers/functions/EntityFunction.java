package net.forthecrown.valhalla.data.triggers.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.valhalla.VikingUtil;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.minecraft.core.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityFunction implements ParameterizedTriggerFunction<Entity> {

    private List<AttributeFunction> attributeFunctions;
    private List<EntityItemFunction> itemFunctions;
    private List<EntityCommandFunction> commandFunctions;

    private Position teleportLoc;

    public EntityFunction(
            List<AttributeFunction> attributeFunctions,
            List<EntityItemFunction> itemFunctions,
            List<EntityCommandFunction> commandFunctions,
            Position teleportLoc
    ) {
        this.attributeFunctions = attributeFunctions;
        this.itemFunctions = itemFunctions;
        this.commandFunctions = commandFunctions;
        this.teleportLoc = teleportLoc;
    }

    public EntityFunction() {
    }

    @Override
    public void execute(Entity entity, ActiveRaid raid) {
        if(entity instanceof LivingEntity) {
            LivingEntity ent = (LivingEntity) entity;

            if(!ListUtils.isNullOrEmpty(attributeFunctions)) {
                for (AttributeFunction e: attributeFunctions) {
                    e.apply(ent, raid);
                }
            }

            if(!ListUtils.isNullOrEmpty(itemFunctions)) {
                EntityEquipment equipment = ent.getEquipment();

                for (EntityItemFunction f: itemFunctions) {
                    f.apply(equipment);
                }
            }
        }

        if(teleportLoc != null) {
            entity.teleport(new Location(
                    entity.getWorld(),
                    teleportLoc.x(),
                    teleportLoc.y(),
                    teleportLoc.z()
            ));
        }

        if(!ListUtils.isNullOrEmpty(commandFunctions)) {
            for (EntityCommandFunction f: commandFunctions) {
                f.execute(entity);
            }
        }
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        if(!ListUtils.isNullOrEmpty(attributeFunctions)) {
            JsonBuf editors = JsonBuf.empty();

            for (AttributeFunction e: attributeFunctions) {
                String key = e.getAttribute().getKey().asString();
                if(editors.has(key)) continue;

                editors.add(e.getAttribute().getKey().asString(), e);
            }

            json.add("attributeEditors", editors.getSource());
        }

        if(!ListUtils.isNullOrEmpty(itemFunctions)) {
            JsonBuf items = JsonBuf.empty();

            for (EntityItemFunction f: itemFunctions) {
                String key = f.getSlot().toString().toLowerCase();
                if(items.has(key)) continue;

                items.add(key, f.serialize());
            }

            json.add("items", items.getSource());
        }

        if(!ListUtils.isNullOrEmpty(commandFunctions)) {
            json.addList("cmds", commandFunctions);
        }

        if(teleportLoc != null) json.add("teleport", JsonUtils.writePosition(teleportLoc));

        return json.getSource();
    }

    public static EntityFunction fromJson(JsonElement element) {
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

        return builder.build();
    }

    public static class Builder {
        private final List<AttributeFunction> attributeFunctions = new ArrayList<>();
        private final List<EntityItemFunction> itemFunctions = new ArrayList<>();
        private final List<EntityCommandFunction> commandFunctions = new ArrayList<>();

        private Position teleportLoc;

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

        public EntityFunction build() {
            return new EntityFunction(
                    attributeFunctions.isEmpty() ? null : attributeFunctions,
                    itemFunctions.isEmpty() ? null : itemFunctions,
                    commandFunctions.isEmpty() ? null : commandFunctions,
                    teleportLoc
            );
        }
    }

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

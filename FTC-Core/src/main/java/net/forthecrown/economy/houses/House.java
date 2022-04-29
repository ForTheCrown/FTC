package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.core.DayChangeListener;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.economy.BalanceHolder;
import net.forthecrown.economy.guilds.VoteState;
import net.forthecrown.economy.houses.components.HouseComponent;
import net.forthecrown.registry.RegistryImpl;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class House implements
        Keyed, Nameable, HoverEventSource<Component>,
        DayChangeListener, BalanceHolder,
        JsonSerializable, JsonDeserializable
{
    private static final NamespacedKey REGISTRY_KEY = Keys.forthecrown("house_components");

    private final Key key;
    private final String name;

    private Component[] description;

    private final Map<House, Relation> houseRelations = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, Relation> relations = new Object2ObjectOpenHashMap<>();

    private final Map<Material, HouseMaterialData> matData = new Object2ObjectOpenHashMap<>();

    private final Registry<HouseComponent> components = new RegistryImpl<>(REGISTRY_KEY);
    private Map<Property, Object> properties = null;
    private int balance;

    long voteTime;
    private BukkitTask voteTask;

    House(String name) {
        this.name = name;
        this.key = Keys.forthecrown("house_" + name.toLowerCase().replaceAll(" ", "_"));
    }

    public Relation getRelationWith(House house) {
        return houseRelations.computeIfAbsent(house, h -> new Relation());
    }

    public Relation getRelationWith(UUID id) {
        return relations.computeIfAbsent(id, uuid -> new Relation());
    }

    public HouseMaterialData getMatData(Material material) {
        return matData.computeIfAbsent(material, HouseMaterialData::new);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Component[] getDescription() {
        return description;
    }

    public void setDescription(Component... description) {
        this.description = Validate.notEmpty(description, "description was null");
    }

    public <T extends HouseComponent> T getComponent(Key key) {
        return (T) components.get(key);
    }

    public void addComponent(HouseComponent component) {
        components.register(component.key(), component);
    }

    public <T> T getProperty(Property<T> property) {
        if(properties == null) return property.defaultValue;
        return (T) properties.getOrDefault(property, property.defaultValue);
    }

    public <T> void setProperty(Property<T> property, T value) {
        if(properties == null) properties = new Object2ObjectOpenHashMap<>();
        properties.put(property, value);
    }

    public Component displayName() {
        return name().hoverEvent(this);
    }

    @Override
    public void onDayChange() {
        // This is where material data should be ticked, aka
        // demand and supply adjusted and slightly randomized
        // to make sure houses don't rely 100% on the player
        // run economy to keep supply and demand going.
        //
        // We could have some random ass attributes like
        // 'material-gathering-speed' to calculate the rate
        // at which a house can regenerate supply.



        // Run component updates
        if(components.isEmpty()) return;

        for (HouseComponent c: components) {
            c.onDayChange();
        }
    }

    public void vote(VoteState state) {
        voteTime = -1;
        VoteModifier modifier = state.getTopic().createModifier(this, state.getData());

        if(modifier.shouldVoteFor()) state.votePro(this);
        else state.voteAgainst(this);
    }

    void scheduleVoteTask() {
        cancelVoteTask();

        if(voteTime == -1) return;

        long executeIn = TimeUtil.timeUntil(voteTime);

        if(executeIn <= 0) {
            vote(Crown.getGuild().getCurrentState());
            return;
        }

        executeIn = TimeUtil.millisToTicks(executeIn);
        Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> vote(Crown.getGuild().getCurrentState()), executeIn);
    }

    @Override
    public int getBalance() {
        return balance;
    }

    @Override
    public void setBalance(int balance) {
        this.balance = balance;
    }

    private void cancelVoteTask() {
        if (voteTask == null || voteTask.isCancelled()) return;
        voteTask.cancel();
        voteTask = null;
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        if(ListUtils.isNullOrEmpty(description)) {
            Crown.logger().warn("House " + getName() + " has no description");
            return HoverEvent.showText(op.apply(Component.text("Error: " + getName() + " has no description")));
        }

        TextComponent.Builder builder = Component.text()
                .append(Component.text("House of " + getName()).color(NamedTextColor.YELLOW));

        for (Component c: description) {
            builder
                    .append(Component.newline())
                    .append(c);
        }

        return HoverEvent.showText(op.apply(builder.build()));
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        if(json.has("description")) {
            setDescription(json.getArray("description", ChatUtils::fromJson, Component[]::new));
        }

        cancelVoteTask();
        voteTime = -1;

        if(json.has("voteTime")) {
            voteTime = json.getLong("voteTime");
            scheduleVoteTask();
        }

        houseRelations.clear();
        houseRelations.putAll(
                json.getMap("houseRelations",
                        s -> Registries.HOUSES.get(Keys.parse(s)),
                        e -> new Relation(e.getAsByte()),
                        true
                )
        );

        relations.clear();
        relations.putAll(
                json.getMap("relations",
                        UUID::fromString,
                        e -> new Relation(e.getAsByte()),
                        true
                )
        );

        matData.clear();
        if(json.has("materialData")) {
            JsonObject mat = json.getObject("materialData");

            for (Map.Entry<String, JsonElement> e: mat.entrySet()) {
                Material material = Material.matchMaterial(e.getKey());
                HouseMaterialData data = new HouseMaterialData(e.getValue(), material);

                matData.put(material, data);
            }
        }

        readProperties(json.get("properties"));
    }

    public JsonElement serializeFull() {
        JsonWrapper json = JsonWrapper.empty();

        if(!houseRelations.isEmpty()) json.addMap("houseRelations", houseRelations, House::toString, Relation::serialize);
        if(relations.isEmpty()) json.addMap("relations", relations, UUID::toString, Relation::serialize);
        if(matData.isEmpty()) json.addMap("materialData", matData, d -> d.name().toLowerCase(), HouseMaterialData::serialize);
        if(description != null) json.addArray("description", description, ChatUtils::toJson);
        if(voteTime != -1) json.add("voteTime", voteTime);

        json.add("properties", writeProperties());

        return json.getSource();
    }

    public void readProperties(JsonElement element) {
        for (Map.Entry<String, JsonElement> e: element.getAsJsonObject().entrySet()) {
            Property property = Properties.get(e.getKey());
            Object val = property.deserialize(e.getValue());

            setProperty(property, val);
        }
    }

    public JsonElement writeProperties() {
        JsonWrapper json = JsonWrapper.empty();

        for (Map.Entry<Property, Object> e: properties.entrySet()) {
            JsonElement element = e.getKey().serialize(e.getValue());

            json.add(e.getKey().name, element);
        }

        return json.getSource();
    }

    @Override
    public String toString() {
        return key.asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        House house = (House) o;

        return new EqualsBuilder()
                .append(key, house.key)
                .isEquals();
    }

    @Override
    public JsonElement serialize() {
        return HouseUtil.write(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(key)
                .toHashCode();
    }
}
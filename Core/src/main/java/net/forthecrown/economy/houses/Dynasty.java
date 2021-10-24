package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.economy.FtcMaterial;
import net.forthecrown.economy.market.guild.GuildVoter;
import net.forthecrown.economy.market.guild.VoteState;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class Dynasty implements
        Keyed, Nameable, HoverEventSource<Component>, GuildVoter,
        JsonSerializable, JsonDeserializable
{
    private final Key key;
    private final String name;

    private Component[] description;

    private final Map<Dynasty, Relation> houseRelations = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, Relation> relations = new Object2ObjectOpenHashMap<>();

    private final Map<FtcMaterial, HouseMaterialData> matData = new Object2ObjectOpenHashMap<>();

    public Dynasty(String name) {
        this.name = name;
        this.key = Crown.coreKey("house_" + name.toLowerCase().replaceAll(" ", "_"));
    }

    public Relation getRelationWith(Dynasty dynasty) {
        return houseRelations.computeIfAbsent(dynasty, h -> new Relation());
    }

    public Relation getRelationWith(UUID id) {
        return relations.computeIfAbsent(id, uuid -> new Relation());
    }

    public HouseMaterialData getMatData(FtcMaterial material) {
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

    @Override
    public void vote(VoteState state) {
        VoteModifier modifier = state.getTopic().makeModifier(this);

        if(modifier.shouldVoteFor()) state.voteFor(this);
        else state.voteAgainst(this);
    }

    public Component displayName() {
        return name().hoverEvent(this);
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        if(ListUtils.isNullOrEmpty(description)) return HoverEvent.showText(op.apply(Component.text("Error: " + getName() + " has no description")));

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

        houseRelations.clear();
        houseRelations.putAll(
                json.getMap("houseRelations",
                        s -> Registries.DYNASTIES.get(FtcUtils.parseKey(s)),
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
                FtcMaterial material = FtcMaterial.fromString(e.getKey());
                HouseMaterialData data = new HouseMaterialData(e.getValue(), material);

                matData.put(material, data);
            }
        }
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        if(!houseRelations.isEmpty()) json.addMap("houseRelations", houseRelations, Dynasty::toString, Relation::serialize);
        if(relations.isEmpty()) json.addMap("relations", relations, UUID::toString, Relation::serialize);
        if(matData.isEmpty()) json.addMap("materialData", matData, d -> d.key().asString(), HouseMaterialData::serialize);
        if(description != null) json.addArray("description", description, ChatUtils::toJson);

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

        Dynasty dynasty = (Dynasty) o;

        return new EqualsBuilder()
                .append(key, dynasty.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(key)
                .toHashCode();
    }
}

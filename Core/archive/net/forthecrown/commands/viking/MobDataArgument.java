package net.forthecrown.commands.viking;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.commands.arguments.NBTArgument;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.valhalla.RaidGenerationContext;
import net.forthecrown.valhalla.VikingUtil;
import net.forthecrown.valhalla.data.EntitySpawnData;
import net.forthecrown.valhalla.data.MobData;
import net.forthecrown.valhalla.data.VikingRaid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import static net.forthecrown.commands.viking.CommandVRaid.get;
import static net.forthecrown.useables.InteractionUtils.*;

/**
 * This class is chaos
 */
public class MobDataArgument {

    static LiteralArgumentBuilder<CommandSource> arg() {
        return literal("mobData")
                .then(argument("part", EnumArgument.of(MobPart.class))
                        .then(literal("add")
                                .then(argument("pos", PositionArgument.position())
                                        .then(argument("key", KeyArgument.minecraft())
                                                .suggests((c, b) -> CompletionProvider.suggestEntityTypes(b))

                                                .executes(c -> {
                                                    VikingRaid raid = get(c);
                                                    MobData data = raid.getMobData();

                                                    return addMob(
                                                            part(c),
                                                            data,
                                                            PositionArgument.getLocation(c, "pos"),
                                                            KeyArgument.getKey(c, "key"),
                                                            null,
                                                            c.getSource()
                                                    );
                                                })

                                                .then(argument("nbt", NBTArgument.nbt())
                                                        .executes(c -> {
                                                            VikingRaid raid = get(c);
                                                            MobData data = raid.getMobData();

                                                            return addMob(
                                                                    part(c),
                                                                    data,
                                                                    PositionArgument.getLocation(c, "pos"),
                                                                    KeyArgument.getKey(c, "key"),
                                                                    NBTArgument.get(c, "nbt"),
                                                                    c.getSource()
                                                            );
                                                        })
                                                )
                                        )
                                )
                        )

                        .then(literal("remove")
                                .then(argument("index", IntegerArgumentType.integer(0))
                                        .executes(c -> {
                                            CommandInfo info = info(c);

                                            int index = c.getArgument("index", Integer.class);

                                            try {
                                                info.part.remove(info.data, index);
                                            } catch (ArrayIndexOutOfBoundsException e) {
                                                throw FtcExceptionProvider.create("Array index out of bounds, invalid index");
                                            }

                                            return 0;
                                        })
                                )
                        )

                        .then(literal("list")
                                .executes(c -> {
                                    CommandInfo info = info(c);

                                    TextComponent.Builder builder = Component.text()
                                            .append(Component.text(info.raid.key().asString() + " has the following " + info.part.name().toLowerCase() + " mobs:"));

                                    ObjectList<EntitySpawnData> dataList = info.part.getList(info.data);

                                    int index = 0;
                                    for (EntitySpawnData d: dataList) {
                                        builder
                                                .append(Component.newline())
                                                .append(Component.text(index + ": {"))
                                                .append(indent())
                                                .append(Component.text("Entity:" + d.getEntityKey().asString() + ", "))
                                                .append(indent())
                                                .append(Component.text("Pos: " + posToString(d.getPosition())))
                                                .append(indent())
                                                .append(Component.text("Tag: " + d.getTag()))
                                                .append(Component.text("}"));

                                        index++;
                                    }

                                    c.getSource().sendMessage(builder.build());
                                    return 0;
                                })
                        )

                        .then(literal("test")
                                .executes(c -> {
                                    CommandInfo info = info(c);
                                    RaidGenerationContext context = VikingUtil.createTestContext(info.raid);

                                    info.data.generate(context);

                                    c.getSource().sendAdmin("Attempted test mob spawning");
                                    return 0;
                                })
                        )
                );
    }

    static Component indent() {
        return Component.text("\n  ");
    }

    static String posToString(Position position) {
        return "X: " + position.x() + " Y: " + position.y() + " Z: " + position.z();
    }

    static void checkHasMobData(VikingRaid raid) throws CommandSyntaxException {
        if(!raid.hasMobData()) throw FtcExceptionProvider.create("Raid does not have mob data");
    }

    static void checkHasDataForPart(MobData data, MobPart part) throws CommandSyntaxException {
        if(!part.hasList(data)) throw FtcExceptionProvider.create("Raid does not have " + part.name().toLowerCase() + " mob list");
    }

    static int addMob(MobPart part, MobData mobData, Location loc, NamespacedKey key, CompoundTag tag, CommandSource source) {
        EntitySpawnData data = new EntitySpawnData(key, tag, new PositionImpl(loc.getX(), loc.getY(), loc.getZ()));

        part.add(mobData, data);

        source.sendAdmin("Added mob spawn");
        return 0;
    }

    static MobPart part(CommandContext<CommandSource> c) {
        return c.getArgument("part", MobPart.class);
    }

    private static class CommandInfo {
        private final VikingRaid raid;
        private final MobData data;
        private final MobPart part;

        public CommandInfo(VikingRaid raid, MobData data, MobPart part) {
            this.raid = raid;
            this.data = data;
            this.part = part;
        }
    }

    static CommandInfo info(CommandContext<CommandSource> c) throws CommandSyntaxException {
        VikingRaid raid = get(c);
        checkHasMobData(raid);

        MobData data = raid.getMobData();
        MobPart part = part(c);
        checkHasDataForPart(data, part);

        return new CommandInfo(raid, data, part);
    }

    private enum MobPart {
        PASSIVE (MobData::getPassive, MobData::addPassive, MobData::removePassive, MobData::hasPassive),
        HOSTILE (MobData::getHostile, MobData::addHostile, MobData::removeHostile, MobData::hasHostile),
        SPECIAL (MobData::getSpecial, MobData::addSpecial, MobData::removeSpecial, MobData::hasSpecial);

        final MobListSupplier supplier;
        final MobListModifier adder;
        final MobListRemover remover;
        final MobListExistenceValidator validator;

        MobPart(MobListSupplier supplier,
                MobListModifier adder,
                MobListRemover remover,
                MobListExistenceValidator validator
        ) {
            this.supplier = supplier;
            this.validator = validator;
            this.adder = adder;
            this.remover = remover;
        }

        private ObjectList<EntitySpawnData> getList(MobData data) {
            return supplier.getList(data);
        }

        private void add(MobData data, EntitySpawnData spawnData) {
            adder.add(data, spawnData);
        }

        private void remove(MobData data, int index) {
            remover.remove(data, index);
        }

        private boolean hasList(MobData data) {
            return validator.hasList(data);
        }
    }

    private interface MobListExistenceValidator {
        boolean hasList(MobData data);
    }

    private interface MobListSupplier {
        ObjectList<EntitySpawnData> getList(MobData data);
    }

    private interface MobListModifier {
        void add(MobData data, EntitySpawnData spawnData);
    }

    private interface MobListRemover {
        void remove(MobData data, int index);
    }
}

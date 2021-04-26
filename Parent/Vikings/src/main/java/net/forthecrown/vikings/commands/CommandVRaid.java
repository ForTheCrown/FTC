package net.forthecrown.vikings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.LiteralArgument;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.NbtType;
import net.forthecrown.core.commands.brigadier.types.Vector3DType;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.VikingRaid;
import net.forthecrown.vikings.valhalla.generation.RaidAreaCreator;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CommandVRaid extends CrownCommandBuilder {

    public CommandVRaid(){
        super("vraid", Vikings.inst());

        setPermission("ftc.vikings.admin");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("create")
                        .then(argument("name", StringArgumentType.word()).then(argument("location", Vector3DType.vec3()))
                                .executes(c -> {
                                    Location location = Vector3DType.getLocation(c, "location");
                                    String name = StringArgumentType.getString(c, "name");

                                    Vikings.getRaidManager().createRaid(name, location);
                                    broadcastAdmin(c.getSource(), "Created raid named " + name);
                                    return 0;
                                })
                        )
                )
                .then(argument("edit")
                         .then(argument(raidArg, StringArgumentType.word())
                               .then(argument("spawns")
                                     .then(spawnEditArg(SpawnCategory.HOSTILE))
                                     .then(spawnEditArg(SpawnCategory.PASSIVE))
                                     .then(spawnEditArg(SpawnCategory.SPECIAL))
                               )
                               .then(argument("test")
                                     .then(testArg(SpawnCategory.HOSTILE))
                                     .then(testArg(SpawnCategory.PASSIVE))
                                     .then(testArg(SpawnCategory.SPECIAL))
                               )
                         )
                );
    }

    private int editSpawn(VikingRaid raid, SpawnCategory category, Location location, NBTTagCompound nbt, boolean add){
        RaidAreaCreator creator = raid.getGenerator();

        Map<Location, NBTTagCompound> map = category.get(creator);
        if(add){
            Validate.notNull(nbt, "NbtTag was null in editSpawn");
            map.put(location, nbt);
        } else map.remove(location);

        category.set(creator, map);

        return 0;
    }

    private LiteralArgument testArg(SpawnCategory category){
        return argument(category.toString().toLowerCase())
                .executes(c -> {
                    VikingRaid raid = getRaid(c);
                    RaidAreaCreator creator = raid.getGenerator();
                    creator.spawn(category.get(creator));

                    broadcastAdmin(c.getSource(), "spawning " + category.toString().toLowerCase() + " mobs");
                    return 0;
                });
    }

    private LiteralArgument spawnEditArg(SpawnCategory category){
        return argument(category.toString().toLowerCase())
                .then(argument("add")
                      .then(argument("location", Vector3DType.vec3()).then(argument("nbt", NbtType.tag())
                            .executes(c -> editSpawn(
                                    getRaid(c),
                                    category,
                                    Vector3DType.getLocation(c, "location"),
                                    NbtType.getTag(c, "nbt"),
                                    true
                            ))
                      ))
                )
                .then(argument("remove")
                      .then(argument("location", Vector3DType.vec3())
                            .executes(c -> editSpawn(
                                    getRaid(c),
                                    category,
                                    Vector3DType.getLocation(c, "location"),
                                    null,
                                    false
                            ))
                      )
                )
                .then(argument("list")
                      .executes(c -> {
                          c.getSource().base.sendMessage(new ChatComponentText(category.get(getRaid(c).getGenerator()).toString()), null);
                          return 0;
                      })
                );
    }

    private static final String raidArg = "raid";
    private VikingRaid getRaid(CommandContext<CommandListenerWrapper> c) throws CrownCommandException {
        String asd = c.getArgument(raidArg, String.class);

        VikingRaid raid = Vikings.getRaidManager().fromName(asd);
        if(raid == null) throw new CrownCommandException("Invalid raid name");

        return raid;
    }

    private enum SpawnCategory {
        SPECIAL (RaidAreaCreator::getSpecials, RaidAreaCreator::setSpecials),
        PASSIVE (RaidAreaCreator::getPassives, RaidAreaCreator::setPassives),
        HOSTILE (RaidAreaCreator::getEnemies, RaidAreaCreator::setEnemies);

        private final VFunction func;
        private final VBiConsumer consumer;

        SpawnCategory(VFunction func, VBiConsumer consumer){
            this.func = func;
            this.consumer = consumer;
        }

        Map<Location, NBTTagCompound> get(RaidAreaCreator creator){
            return func.apply(creator);
        }

        void set(RaidAreaCreator creator, Map<Location, NBTTagCompound> map){
            consumer.accept(creator, map);
        }
    }

    //Made these so the above enum wouldn't have looooooonnnnnng variable types lol
    private interface VFunction extends Function<RaidAreaCreator, Map<Location, NBTTagCompound>>{}
    private interface VBiConsumer extends BiConsumer<RaidAreaCreator, Map<Location, NBTTagCompound>>{}
}

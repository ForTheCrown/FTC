package net.forthecrown.vikings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.Vector3DType;
import net.forthecrown.vikings.raids.valhalla.VikingLootTable;
import net.forthecrown.vikings.Vikings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.loot.LootContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class CommandVikingFunction extends CrownCommandBuilder {

    public CommandVikingFunction(){
        super("vfunc", Vikings.getInstance());

        setPermission("ftc.admin");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.then(argument("chest")
                .then(argument("location", Vector3DType.vec3(true))
                        .then(argument("loottable", StringArgumentType.word())
                                .suggests((c, b) -> suggestLoottables(b))

                                .executes(c -> {
                                    VikingLootTable table;
                                    try {
                                        table = VikingLootTable.valueOf(c.getArgument("loottable", String.class).toUpperCase());
                                    } catch (Exception e) { throw new CrownCommandException("Invalid loottable"); }
                                    Location location = Vector3DType.getLocation(c, "location");
                                    Block block = location.getBlock();

                                    if(!(block.getState() instanceof Chest)) block.setType(Material.CHEST);
                                    Chest chest = (Chest) block.getState();

                                    chest.clearLootTable();
                                    chest.getBlockInventory().clear();

                                    chest.setLootTable(table.getLootTable());
                                    table.getLootTable().fillInventory(chest.getBlockInventory(), new Random(), new LootContext.Builder(location).build());
                                    return 0;
                                })
                        )
                )
        );
    }

    private CompletableFuture<Suggestions> suggestLoottables(SuggestionsBuilder b){
        List<String> toReturn = new ArrayList<>();
        for (VikingLootTable l: VikingLootTable.values()){
            toReturn.add(l.toString());
        }
        return suggestMatching(b, toReturn);
    }
}

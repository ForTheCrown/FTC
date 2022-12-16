const S_Manager          = Java.type("net.forthecrown.core.script.ScriptManager");
const Worlds             = Java.type("net.forthecrown.core.Worlds");
const Block              = Java.type("org.bukkit.block.Block");
const Files              = Java.type("java.nio.file.Files");

const StringReader       = Java.type("com.mojang.brigadier.StringReader");
const BlockArgument      = Java.type("net.forthecrown.grenadier.types.block.BlockArgument");

const DIRECTORY          = S_Manager.getInstance().getDirectory();
const OUTPUT_TXT         = DIRECTORY.resolve("snow_gen_output.txt");

const WORLD              = Worlds.overworld();

if (!Files.exists(OUTPUT_TXT)) {
    logger.warn("Output directory {} does not exist in {}", OUTPUT_TXT.getFileName(), DIRECTORY);
} else {
    run();
}

function run() {
    let reader = Files.newBufferedReader(OUTPUT_TXT);
    let lines = reader.lines();

    lines.forEach(currentItem => {
        let sReader = new StringReader(currentItem);

        // Coordinates
        let x = sReader.readInt();
        sReader.skipWhitespace();

        let y = sReader.readInt();
        sReader.skipWhitespace();

        let z = sReader.readInt();
        sReader.skipWhitespace();

        // Block data
        let dataResult = BlockArgument.block().parse(sReader);
        dataResult.place(WORLD, x, y, z);
    });
}
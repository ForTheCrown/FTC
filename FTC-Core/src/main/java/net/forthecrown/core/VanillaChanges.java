package net.forthecrown.core;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;

class VanillaChanges {
    public static void softerDeepslate() {
        RotatedPillarBlock block = (RotatedPillarBlock) Blocks.DEEPSLATE;
        BlockBehaviour.Properties properties = properties(block);
        properties.strength(1.5F, 6F);

        reassign(block, properties);
        stateReassign(block.defaultBlockState(), 1.5F);
    }

    private static BlockBehaviour.Properties properties(BlockBehaviour behaviour) {
        try {
            Class<BlockBehaviour> clazz = BlockBehaviour.class;
            Field f = clazz.getDeclaredField("aP");
            f.setAccessible(true);

            return (BlockBehaviour.Properties) f.get(behaviour);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void stateReassign(BlockState state, float hardness) {
        try {
            Class<BlockBehaviour.BlockStateBase> clazz = BlockBehaviour.BlockStateBase.class;
            Field f = clazz.getDeclaredField("k");
            f.setAccessible(true);

            f.set(state, hardness);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void reassign(BlockBehaviour behaviour, BlockBehaviour.Properties properties) {
        try {
            Class<BlockBehaviour> clazz = BlockBehaviour.class;
            Field f = clazz.getDeclaredField("aP");
            f.setAccessible(true);

            f.set(behaviour, properties);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}

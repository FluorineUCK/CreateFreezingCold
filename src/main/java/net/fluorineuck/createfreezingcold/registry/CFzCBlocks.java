package net.fluorineuck.createfreezingcold.registry;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.fluorineuck.createfreezingcold.content.liquefaction.dewar.DewarFlaskBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CFzCBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CreateFreezingCold.MOD_ID);

    public static final DeferredBlock<DewarFlaskBlock> DEWAR_FLASK =
            BLOCKS.registerBlock(
                    "dewar_flask",
                    DewarFlaskBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
            );
}
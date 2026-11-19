package net.fluorineuck.createfreezingcold.content.liquefaction.dewar;

import net.minecraft.world.InteractionResult;
import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirEvaporation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DewarFlaskItem extends BlockItem {
    public DewarFlaskItem(Block block, Properties properties) {super(block, properties);}

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null){return InteractionResult.PASS;}
        if (player.isSecondaryUseActive()) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        Optional<IFluidHandler> target = FluidUtil.getFluidHandler(
                level,
                context.getClickedPos(),
                context.getClickedFace()
        );
        if (target.isEmpty()) {return InteractionResult.PASS;}
        if (!level.isClientSide) {
            if (FluidUtil.interactWithFluidHandler(player, context.getHand(), target.get())) {
                LiquidAirEvaporation.trackPlayer(player);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
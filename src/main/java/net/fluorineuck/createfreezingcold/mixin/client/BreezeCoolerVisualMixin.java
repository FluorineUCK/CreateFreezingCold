package net.fluorineuck.createfreezingcold.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.fluorineuck.createfreezingcold.client.SuperChilledModels;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlock.FrostLevel;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerVisual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BreezeCoolerVisual.class, remap = false)
public abstract class BreezeCoolerVisualMixin extends AbstractBlockEntityVisual<BreezeCoolerBlockEntity> {
    @Shadow private FrostLevel frostLevel;
    @Unique private boolean createfreezingcold$wasSuperChilled;

    protected BreezeCoolerVisualMixin(VisualizationContext context, BreezeCoolerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @Inject(method = "animate", at = @At("HEAD"))
    private void createfreezingcold$invalidateModel(float partialTick, CallbackInfo ci) {
        boolean superChilled = SuperChilledModels.isSuperChilled(blockEntity);
        if (superChilled != createfreezingcold$wasSuperChilled) {
            frostLevel = null;
            createfreezingcold$wasSuperChilled = superChilled;
        }
    }

    @ModifyExpressionValue(
            method = "animate",
            at = @At(value = "INVOKE", target = "Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerRenderer;getBreezeModel(Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerBlock$FrostLevel;Z)Ldev/engine_room/flywheel/lib/model/baked/PartialModel;")
    )
    private PartialModel createfreezingcold$selectModel(PartialModel original) {
        return SuperChilledModels.select(original, createfreezingcold$wasSuperChilled);
    }
}

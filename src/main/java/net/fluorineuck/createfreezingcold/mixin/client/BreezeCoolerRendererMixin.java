package net.fluorineuck.createfreezingcold.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.fluorineuck.createfreezingcold.client.SuperChilledModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BreezeCoolerRenderer.class, remap = false)
public abstract class BreezeCoolerRendererMixin {

    @Unique
    private static final ThreadLocal<Boolean> createfreezingcold$superChilled = new ThreadLocal<>();

    @WrapMethod(method = "renderSafe(Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V")
    private void createfreezingcold$withCoolerState(
            BreezeCoolerBlockEntity cooler, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay, Operation<Void> original
    ) {
        Boolean previous = createfreezingcold$superChilled.get();
        createfreezingcold$superChilled.set(SuperChilledModels.isSuperChilled(cooler));
        try {
            original.call(cooler, partialTicks, poseStack, buffer, light, overlay);
        } finally {
            if (previous == null) {
                createfreezingcold$superChilled.remove();
            } else {
                createfreezingcold$superChilled.set(previous);
            }
        }
    }

    @ModifyExpressionValue(
            method = "renderShared",
            at = @At(value = "INVOKE", target = "Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerRenderer;getBreezeModel(Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerBlock$FrostLevel;Z)Ldev/engine_room/flywheel/lib/model/baked/PartialModel;")
    )
    private static PartialModel createfreezingcold$selectModel(PartialModel original) {
        return SuperChilledModels.select(original, Boolean.TRUE.equals(createfreezingcold$superChilled.get()));
    }
}

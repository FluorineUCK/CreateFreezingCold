package net.fluorineuck.createfreezingcold.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.fluorineuck.createfreezingcold.content.cooling.SuperChilledCoolerState;
import net.fluorineuck.createfreezingcold.mixin.BreezeCoolerBlockEntityAccessor;
import net.ty.createcraftedbeginning.client.render.CCBPartialModels;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;

public final class SuperChilledModels {
    public static final PartialModel IDLE = PartialModel.of(
            CreateFreezingCold.asResource("block/breeze_cooler/super_chilled"));
    public static final PartialModel ACTIVE = PartialModel.of(
            CreateFreezingCold.asResource("block/breeze_cooler/super_chilled_active"));

    private SuperChilledModels() {}

    public static boolean isSuperChilled(BreezeCoolerBlockEntity cooler) {
        // The existing state serialization also restores this subclass on the client.
        return ((BreezeCoolerBlockEntityAccessor) cooler)
                .createfreezingcold$getCurrentState() instanceof SuperChilledCoolerState;
    }

    public static PartialModel select(PartialModel original, boolean superChilled) {
        if (!superChilled) {
            return original;
        }
        // Preserve upstream's active threshold and all non-chilled model choices.
        if (original == CCBPartialModels.BREEZE_CHILLED) {
            return IDLE;
        }
        if (original == CCBPartialModels.BREEZE_CHILLED_ACTIVE) {
            return ACTIVE;
        }
        return original;
    }
}

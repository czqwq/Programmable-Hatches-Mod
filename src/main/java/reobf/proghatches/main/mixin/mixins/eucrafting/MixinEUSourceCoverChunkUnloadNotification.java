package reobf.proghatches.main.mixin.mixins.eucrafting;

import org.spongepowered.asm.mixin.Mixin;

import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;

@Mixin(value = CoverableTileEntity.class, remap = true)
public abstract class MixinEUSourceCoverChunkUnloadNotification extends BaseTileEntity {
    /*
     * @Override
     * public void onChunkUnload() {
     * unloadCover();
     * super.onChunkUnload();
     * }
     * @Shadow
     * public abstract CoverInfo getCoverInfoAtSide(ForgeDirection side);
     * protected void unloadCover() {
     * try {
     * for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
     * final CoverInfo coverInfo = getCoverInfoAtSide(side);
     * if (coverInfo.isValid()) {
     * CoverBehaviorBase be = coverInfo.getCoverBehavior();
     * if (be != null && be instanceof AECover) {
     * ((AECover) be).chunkUnload((Data) coverInfo.getCoverData());
     * }
     * }
     * }
     * } catch (Exception e) {
     * MyMod.LOG.error("caught error in mixin", e);
     * }
     * }
     */
}

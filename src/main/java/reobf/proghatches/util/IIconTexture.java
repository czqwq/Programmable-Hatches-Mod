package reobf.proghatches.util;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.interfaces.ITexture;
import gregtech.api.util.LightingHelper;
import gregtech.common.render.GTTextureBase;


public class IIconTexture extends GTTextureBase implements ITexture {

	private final IIcon mBlock;
	// private final byte mSide, mMeta;
	private int rgb;

	public IIconTexture(IIcon aBlock, int rgb) {
		this.rgb=rgb;
		mBlock = aBlock;

	}

	@Override
	public boolean isOldTexture() {
		return false;
	}

	private IIcon getIcon(int ordinalSide) {

		return mBlock;
	}

	@Override
	public void renderXPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
		final IIcon aIcon = getIcon(ForgeDirection.EAST.ordinal());
		aRenderer.field_152631_f = true;
		startDrawingQuads(aRenderer, 1.0f, 0.0f, 0.0f);
		new LightingHelper(aRenderer).setupLightingXPos(aBlock, aX, aY, aZ).setupColor(ForgeDirection.EAST, rgb);
		aRenderer.renderFaceXPos(aBlock, aX, aY, aZ, aIcon);
		draw(aRenderer);
		aRenderer.field_152631_f = false;
	}

	@Override
	public void renderXNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
		startDrawingQuads(aRenderer, -1.0f, 0.0f, 0.0f);
		final IIcon aIcon = getIcon(ForgeDirection.WEST.ordinal());
		new LightingHelper(aRenderer).setupLightingXNeg(aBlock, aX, aY, aZ).setupColor(ForgeDirection.WEST, rgb);
		aRenderer.renderFaceXNeg(aBlock, aX, aY, aZ, aIcon);
		draw(aRenderer);
	}

	@Override
	public void renderYPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
		startDrawingQuads(aRenderer, 0.0f, 1.0f, 0.0f);
		final IIcon aIcon = getIcon(ForgeDirection.UP.ordinal());
		new LightingHelper(aRenderer).setupLightingYPos(aBlock, aX, aY, aZ).setupColor(ForgeDirection.UP, rgb);
		aRenderer.renderFaceYPos(aBlock, aX, aY, aZ, aIcon);
		draw(aRenderer);
	}

	@Override
	public void renderYNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
		startDrawingQuads(aRenderer, 0.0f, -1.0f, 0.0f);
		final IIcon aIcon = getIcon(ForgeDirection.DOWN.ordinal());
		new LightingHelper(aRenderer).setupLightingYNeg(aBlock, aX, aY, aZ).setupColor(ForgeDirection.DOWN, rgb);
		aRenderer.renderFaceYNeg(aBlock, aX, aY, aZ, aIcon);
		draw(aRenderer);
	}

	@Override
	public void renderZPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
		startDrawingQuads(aRenderer, 0.0f, 0.0f, 1.0f);
		final IIcon aIcon = getIcon(ForgeDirection.SOUTH.ordinal());
		new LightingHelper(aRenderer).setupLightingZPos(aBlock, aX, aY, aZ).setupColor(ForgeDirection.SOUTH, rgb);
		aRenderer.renderFaceZPos(aBlock, aX, aY, aZ, aIcon);
		draw(aRenderer);
	}

	@Override
	public void renderZNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
		startDrawingQuads(aRenderer, 0.0f, 0.0f, -1.0f);
		final IIcon aIcon = getIcon(ForgeDirection.NORTH.ordinal());
		aRenderer.field_152631_f = true;
		new LightingHelper(aRenderer).setupLightingZNeg(aBlock, aX, aY, aZ).setupColor(ForgeDirection.NORTH, rgb);
		aRenderer.renderFaceZNeg(aBlock, aX, aY, aZ, aIcon);
		draw(aRenderer);
		aRenderer.field_152631_f = false;
	}

	@Override
	public boolean isValidTexture() {
		return mBlock != null;
	}

}

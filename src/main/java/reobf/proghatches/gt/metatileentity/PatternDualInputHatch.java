package reobf.proghatches.gt.metatileentity;

import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.input.Keyboard;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomNameObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.DualInputHatch.Net;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;

public class PatternDualInputHatch extends BufferedDualInputHatch implements ICraftingProvider, IGridProxyable,
    ICustomNameObject, IInterfaceViewable, IPowerChannelState, IActionHost, IMultiplePatternPushable {

    public PatternDualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
        boolean mMultiFluid, int bufferNum) {
        super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public int rowSize() {
        return 9;
    }

    @Override
    public IInventory getPatterns() {
        return patternMapper;
    }

    IInventory patternMapper = new IInventory() {

        @Override
        public int getSizeInventory() {

            return pattern.length;
        }

        @Override
        public ItemStack getStackInSlot(int slotIn) {

            return pattern[slotIn];
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {

            try {
                if (pattern[index] != null) {
                    ItemStack itemstack;

                    if (pattern[index].stackSize <= count) {
                        itemstack = pattern[index];
                        pattern[index] = null;
                        this.markDirty();
                        return itemstack;
                    } else {
                        itemstack = pattern[index].splitStack(count);

                        if (pattern[index].stackSize == 0) {
                            pattern[index] = null;
                        }

                        this.markDirty();
                        return itemstack;
                    }
                } else {
                    return null;
                }
            } finally {

                onPatternChange();
            }
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int index) {

            return null;
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            pattern[index] = stack;
            onPatternChange();
        }

        @Override
        public String getInventoryName() {

            return "";
        }

        @Override
        public boolean hasCustomInventoryName() {

            return false;
        }

        // @Override
        // public int stack

        @Override
        public void markDirty() {

        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {

            return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {

            return true;
        }

        @Override
        public int getInventoryStackLimit() {

            return 1;
        }
    };

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {

        return new ITexture[] { aBaseTexture,
            TextureFactory.of(
                supportsFluids() ? BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER
                    : BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUS) };

    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture,
            TextureFactory.of(
                supportsFluids() ? BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER
                    : BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUS) };

    }

    public PatternDualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, int bufferNum,
        boolean sf, String... optional) {

        super(
            id,
            name,
            nameRegional,
            tier,
            mMultiFluid,
            bufferNum,
            (optional.length > 0 ? optional
                : reobf.proghatches.main.Config.get(
                    "PDIH" + (sf ? "" : "B"),
                    ImmutableMap.of(
                        "bufferNum",
                        bufferNum,
                        "fluidSlots",
                        16/* fluidSlots() */, /*
                                               * "cap",
                                               * format.format((int) (4000 * Math.pow(4, tier)
                                               * / (mMultiFluid ? 4 : 1))),
                                               */
                        "mMultiFluid",
                        mMultiFluid,
                        "slots",
                        Math.min(16, (1 + tier) * (tier + 1))/*
                                                              * , "stacksize",
                                                              * (int) (64 * Math.pow(2, Math.max(tier - 3, 0)))
                                                              */))

            ));
        if (sf != supportsFluids()) {

            throw new AssertionError();
        }
    }

    public int fluidSlots() {
        return 16;

    }

    ItemStack[] pattern = new ItemStack[36];

    ButtonWidget createRefundButton(IWidgetBuilder<?> builder) {

        Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {

            PatternDualInputHatch.this.dirty = true;
            try {
                PatternDualInputHatch.this.refundAll();
            } catch (Exception e) {

                // e.printStackTrace();
            }
        })
            .setPlayClickSound(true)
            .setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_EXPORT)

            .addTooltips(ImmutableList.of("Return all internally stored items back to AE"))

            .setPos(new Pos2d(getGUIWidth() - 18 - 3, 5 + 16 + 2))
            .setSize(16, 16);
        return (ButtonWidget) button;
    }

    MachineSource requestSource;

    private BaseActionSource getRequest() {

        if (requestSource == null) requestSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
        return requestSource;
    }

    private void refundAll() throws Exception {
        markDirty();
        dirty = true;
        BaseActionSource src = getRequest();
        IMEMonitor<IAEItemStack> sg = getProxy().getStorage()
            .getItemInventory();
        abstract class Inv {

            abstract ItemStack[] geti();

            abstract FluidTank[] getf();
        }
        Consumer<Inv> consumer = inv -> {
            try {
                for (ItemStack itemStack : inv.geti()) {
                    if (itemStack == null || itemStack.stackSize == 0) continue;
                    IAEItemStack rest = Platform.poweredInsert(
                        getProxy().getEnergy(),
                        sg,
                        AEApi.instance()
                            .storage()
                            .createItemStack(itemStack),
                        src);
                    itemStack.stackSize = rest != null && rest.getStackSize() > 0 ? (int) rest.getStackSize() : 0;
                }
                IMEMonitor<IAEFluidStack> fsg = getProxy().getStorage()
                    .getFluidInventory();
                for (FluidTank fluidStack : inv.getf()) {
                    if (fluidStack == null || fluidStack.getFluidAmount() == 0) continue;
                    IAEFluidStack rest = Platform.poweredInsert(
                        getProxy().getEnergy(),
                        fsg,
                        AEApi.instance()
                            .storage()
                            .createFluidStack(fluidStack.getFluid()),
                        src);
                    fluidStack.setFluid(
                        Optional.ofNullable(rest)
                            .map(IAEFluidStack::getFluidStack)
                            .orElse(null));
                } ;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        inv0.stream()
            .map(s -> new Inv() {

                @Override
                ItemStack[] geti() {
                    return s.mStoredItemInternal;
                }

                @Override
                FluidTank[] getf() {
                    return s.mStoredFluidInternal;
                }
            })
            .forEach(consumer);;
        consumer.accept(new Inv() {

            @Override
            ItemStack[] geti() {

                return mInventory;
            }

            @Override
            FluidTank[] getf() {

                return mStoredFluid;
            }
        });

    }

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        buildContext.addSyncedWindow(88, this::createPatternWindow);

        builder.widget(createRefundButton(builder));
        builder.widget(
            new ButtonWidget().setOnClick(
                (clickData, widget) -> {
                    if (widget.getContext()
                        .isClient() == false)
                        widget.getContext()
                            .openSyncedWindow(88);
                })
                .setPlayClickSound(true)
                .setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_PLUS_LARGE)
                .addTooltips(ImmutableList.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.pattern")))
                .setSize(16, 16)
                // .setPos(10 + 16 * 9, 3 + 16 * 2)
                .setPos(new Pos2d(getGUIWidth() - 18 - 3, 5 + 16 + 2 + 16 + 2)));

        super.addUIWidgets(builder, buildContext);
    }

    protected ModularWindow createPatternWindow(final EntityPlayer player) {
        final int WIDTH = 18 * 4 + 6;
        final int HEIGHT = 18 * 9 + 6;
        final int PARENT_WIDTH = getGUIWidth();
        final int PARENT_HEIGHT = getGUIHeight();

        ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
        builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
        builder.setGuiTint(getGUIColorization());
        builder.setDraggable(true);
        builder.setPos(
            (a, b) -> new Pos2d(
                PARENT_WIDTH + b.getPos()
                    .getX(),
                PARENT_HEIGHT * 0 + b.getPos()
                    .getY()));
        MappingItemHandler shared_handler = new MappingItemHandler(pattern, 0, 36);
        // use shared handler
        // or shift clicking a pattern in pattern slot will just transfer it to another pattern slot
        // instead of player inventory!
        for (int i = 0; i < 36; i++) {

            BaseSlot bs;

            builder.widget(new SlotWidget(bs = new BaseSlot(shared_handler, i)

            ) {

                @Override
                protected ItemStack getItemStackForRendering(Slot slotIn) {
                    ItemStack stack = slotIn.getStack();
                    if (stack == null || !(stack.getItem() instanceof ItemEncodedPattern)) {
                        return stack;
                    }
                    ItemStack output = ((ItemEncodedPattern) stack.getItem()).getOutput(stack);
                    return output != null ? output : stack;

                }
            }.setShiftClickPriority(-1)
                .setFilter(itemStack -> itemStack.getItem() instanceof ICraftingPatternItem)
                .setChangeListener(() -> { onPatternChange(); })
                .setPos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
                .setBackground(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_PATTERN_ME));

        }

        return builder.build();
    }

    boolean needPatternSync;

    private void onPatternChange() {
        if (!getBaseMetaTileEntity().isServerSide()) return;
        // we do not refund 'cause it's impossible to trace the item

        needPatternSync = true;
    }

    @Override
    public void gridChanged() {
        needPatternSync = true;
    }

    @Override
    public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {

        return false;
    }

    @Override
    public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {

        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection side, Fluid aFluid) {

        return false;
    }

    @Override
    public boolean canFill(ForgeDirection side, Fluid aFluid) {

        return false;
    }

    public class Inst extends PatternDualInputHatch {

        public Inst(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures, boolean mMultiFluid,
            int bufferNum) {
            super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
        }

        @Override
        public boolean supportsFluids() {
            return PatternDualInputHatch.this.supportsFluids();
        }

    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new Inst(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
    }

    @Override
    public void initTierBasedField() {
        if (supportsFluids()) super.initTierBasedField();
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        additionalConnection = aNBT.getBoolean("additionalConnection");
        NBTTagCompound tag = aNBT.getCompoundTag("patternSlots");
        if (tag != null) for (int i = 0; i < pattern.length; i++) {
            pattern[i] = Optional.ofNullable(tag.getCompoundTag("i" + i))
                .map(ItemStack::loadItemStackFromNBT)
                .orElse(null);
        }
        customName = aNBT.getString("customName");

        getProxy().readFromNBT(aNBT);

        super.loadNBTData(aNBT);
        updateValidGridProxySides();
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setBoolean("additionalConnection", additionalConnection);
        NBTTagCompound tag = new NBTTagCompound();// aNBT.getCompoundTag("patternSlots");

        for (int i = 0; i < pattern.length; i++) {
            final int ii = i;
            Optional.ofNullable(pattern[i])
                .map(s -> s.writeToNBT(new NBTTagCompound()))
                .ifPresent(s -> tag.setTag("i" + ii, s));
        }
        aNBT.setTag("patternSlots", tag);
        Optional.ofNullable(customName)
            .ifPresent(s -> aNBT.setString("customName", s));
        getProxy().writeToNBT(aNBT);

        super.saveNBTData(aNBT);
    }

    private void clearInv() {

        for (int i = 0; i < 16; i++) mInventory[i] = null;
        for (int i = 0; i < this.fluidSlots(); i++) mStoredFluid[i].setFluid(null);;

    }

    private boolean postMEPatternChange() {
        // don't post until it's active
        if (!getProxy().isActive()) return false;
        try {
            getProxy().getGrid()
                .postEvent(new MENetworkCraftingPatternChange(this, getProxy().getNode()));
        } catch (GridAccessException ignored) {
            return false;
        }
        return true;
    }

    long lastSync;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);

        if (getBaseMetaTileEntity().isServerSide()) {
            if (needPatternSync && aTimer > lastSync + 100) {
                needPatternSync = !postMEPatternChange();
                lastSync = aTimer;
            }
            if (aTimer % 20 == 0) {
                getBaseMetaTileEntity().setActive(isActive());
            }
        }
    }

    @Override
    public int getInventoryStackLimit() {

        return Integer.MAX_VALUE;
    }

    public boolean skipActiveCheck;

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (!isActive() && !skipActiveCheck) return false;
        if (!isEmpty()) return false;
        if (!supportsFluids()) {
            for (int i = 0; i < table.getSizeInventory(); ++i) {
                ItemStack itemStack = table.getStackInSlot(i);
                if (itemStack == null) continue;
                if (itemStack.getItem() instanceof ItemFluidPacket) return false;
            }
        }

        int items = 0;
        int fluids = 0;
        int size = table.getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = table.getStackInSlot(i);
            if (itemStack == null) continue;
            if (itemStack.getItem() instanceof ItemFluidPacket) {
                fluids++;
                if (fluids > this.fluidSlots()) {
                    clearInv();
                    return false;
                }

                mStoredFluid[fluids - 1].setFluidDirect(ItemFluidPacket.getFluidStack(itemStack));

            } else {
                items++;
                if (items > 16) {
                    clearInv();
                    return false;
                }
                mInventory[items - 1] = itemStack;

            }
        }
        markDirty();
        dirty = true;
        // inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory)
        // classify();

        /*
         * for (DualInvBuffer inv0 : this.sortByEmpty()) {
         * if (inv0.full() == false)
         * if(inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory)||
         * inv0.classify(this.mStoredFluid, mInventory,true))break;
         * }
         */

        /*
         * Integer check = detailmap.get(patternDetails);
         * if(check==null){
         * currentID++;
         * detailmap.put(patternDetails,currentID );
         * check=currentID;
         * }
         */

        DualInvBuffer theBuffer = /* ((BufferedDualInputHatch) master). */classifyForce();
        if(theBuffer!=null)
        recordRecipe(theBuffer);

        justHadNewItems = true;
        return true;
    }

    private boolean isEmpty() {
        for (ItemStack is : mInventory) {
            if (is != null && is.stackSize > 0) return false;
        }
        for (FluidTank is : mStoredFluid) {
            if (is.getFluidAmount() > 0) return false;
        }
        return true;
    }

    @Override
    public boolean isBusy() {
        return !isEmpty();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return getProxy().getNode();
    }

    private AENetworkProxy gridProxy;

    @Override
    public AENetworkProxy getProxy() {
        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(
                this,
                "proxy",
                new ItemStack(
                    GregTechAPI.sBlockMachines,
                    1,
                    this.getBaseMetaTileEntity()
                        .getMetaTileID()),
                true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    @Override
    public void securityBreak() {
        // no op
    }

    @Override
    public String getName() {

        if (hasCustomName()) {
            return getCustomName();
        }
        StringBuilder name = new StringBuilder();
        if (getCrafterIcon() != null) {
            name.append(getCrafterIcon().getDisplayName());
        } else {
            name.append(getLocalName());// getinventoryname()
        }

        /*
         * if (mInventory[SLOT_CIRCUIT] != null) {
         * name.append(" - ");
         * name.append(mInventory[SLOT_CIRCUIT].getItemDamage());
         * }
         */

        for (ItemStack is : this.shared.getDisplayItems()) {
            name.append(" - ");

            if (is.getItem() != GTUtility.getIntegratedCircuit(0)
                .getItem()) {
                name.append(is.getDisplayName());
                if (is.getItemDamage() > 0) {
                    name.append("@" + is.getItemDamage());
                }
            } else {
                name.append(is.getItemDamage());
            }

            // if(is.stackSize>0){name.append("*"+is.stackSize);}
        }

        for (FluidStack is : this.shared.getDisplayFluid()) {
            name.append(" - ");
            name.append(is.getLocalizedName());
            // if(is.amount>0){name.append("*"+is.amount);}
        }

        return name.toString();
    }

    @Override
    public TileEntity getTileEntity() {
        return (TileEntity) getBaseMetaTileEntity();
    }

    @Override
    public boolean shouldDisplay() {

        return true;
    }

    String customName;
    private boolean additionalConnection;

    @Override
    public String getCustomName() {

        return customName;
    }

    @Override
    public boolean hasCustomName() {

        return customName != null && (!customName.equals(""));
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
        float aX, float aY, float aZ) {
        final ItemStack is = aPlayer.inventory.getCurrentItem();
        if (is != null && is.getItem() instanceof ToolQuartzCuttingKnife) {
            if (ForgeEventFactory.onItemUseStart(aPlayer, is, 1) <= 0) return false;
            IGregTechTileEntity te = getBaseMetaTileEntity();
            aPlayer.openGui(
                AppEng.instance(),
                GuiBridge.GUI_RENAMER.ordinal() << 5 | (side.ordinal()),
                te.getWorld(),
                te.getXCoord(),
                te.getYCoord(),
                te.getZCoord());
            return true;
        }
        return super.onRightclick(aBaseMetaTileEntity, aPlayer, side, aX, aY, aZ);
    }

    @Override
    public void setCustomName(String name) {
        customName = name;

    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord(getTileEntity());
    }

    @Override
    public boolean isActive() {
        return getProxy() != null && getProxy().isActive();
    }

    ItemStack[] patternItemCache = new ItemStack[36];
    ICraftingPatternDetails[] patternDetailCache = new ICraftingPatternDetails[36];

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if (!isActive()) return;

        for (int index = 0; index < pattern.length; index++) {
            ItemStack slot = pattern[index];

            if (slot == null) {
                patternItemCache[index] = null;
                patternDetailCache[index] = null;
                continue;
            }

            if (patternItemCache[index] == pattern[index]) {// just compare object id
                craftingTracker.addCraftingOption(this, patternDetailCache[index]);
                continue;
            }

            ICraftingPatternDetails details = null;
            try {
                details = ((ICraftingPatternItem) slot.getItem()).getPatternForItem(
                    slot,
                    this.getBaseMetaTileEntity()
                        .getWorld());
            } catch (Exception e) {}
            if (details == null) {
                GTMod.GT_FML_LOGGER.warn(
                    "Found an invalid pattern at " + getBaseMetaTileEntity().getCoords()
                        + " in dim "
                        + getBaseMetaTileEntity().getWorld().provider.dimensionId);
                continue;
            }
            patternItemCache[index] = pattern[index];
            patternDetailCache[index] = details;
            craftingTracker.addCraftingOption(this, details);
        }

    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
        return isOutputFacing(forgeDirection) ? AECableType.SMART : AECableType.NONE;
    }

    private void updateValidGridProxySides() {
        if (additionalConnection) {
            getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));
        } else {
            getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
        }
        // getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

    }

    @Override
    public void onFacingChange() {
        updateValidGridProxySides();
    }

    @Override
    public boolean isPowered() {
        return getProxy() != null && getProxy().isPowered();
    }

    @Override
    public ItemStack getCrafterIcon() {
        ItemStack is = this.getMachineCraftingIcon();
        return is == null ? new ItemStack(GregTechAPI.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID()) : is;
    }

    @Override
    public void onBlockDestroyed() {
        try {
            refundAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBlockDestroyed();

        IGregTechTileEntity te = this.getBaseMetaTileEntity();
        World aWorld = te.getWorld();
        int aX = te.getXCoord();
        short aY = te.getYCoord();
        int aZ = te.getZCoord();

        for (int i = 0; i < pattern.length; i++) {
            final ItemStack tItem = pattern[i];
            if ((tItem != null) && (tItem.stackSize > 0)) {
                final EntityItem tItemEntity = new EntityItem(
                    aWorld,
                    aX + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    aY + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    aZ + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    new ItemStack(tItem.getItem(), tItem.stackSize, tItem.getItemDamage()));
                if (tItem.hasTagCompound()) {
                    tItemEntity.getEntityItem()
                        .setTagCompound(
                            (NBTTagCompound) tItem.getTagCompound()
                                .copy());
                }
                tItemEntity.motionX = (XSTR_INSTANCE.nextGaussian() * 0.05D);
                tItemEntity.motionY = (XSTR_INSTANCE.nextGaussian() * 0.25D);
                tItemEntity.motionZ = (XSTR_INSTANCE.nextGaussian() * 0.05D);
                aWorld.spawnEntityInWorld(tItemEntity);
                tItem.stackSize = 0;
                pattern[i] = null;
            }
        }
    }

    public int fluidLimit() {

        return Integer.MAX_VALUE;
    }

    public int itemLimit() {

        return Integer.MAX_VALUE;
    }

    boolean createInsertion() {
        return false;
    }

    boolean showFluidLimit() {

        return false;
    }

    @Override
    public int getInventoryFluidLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ) {
        additionalConnection = !additionalConnection;
        updateValidGridProxySides();
        aPlayer.addChatComponentMessage(
            new ChatComponentTranslation("GT5U.hatch.additionalConnection." + additionalConnection));
        return true;
    }

    public Net getNetwork() {

        return new Net(
            this.getGridNode(ForgeDirection.UP)
                .getGrid(),
            this);
    }

    @Override
    public IGridNode getActionableNode() {

        return this.getGridNode(ForgeDirection.UP);
    }

    public Object getTile() {
        return this.getBaseMetaTileEntity();
    }

    @Override
    public boolean allowsPatternOptimization() {
        // TODO Auto-generated method stub
        return IInterfaceViewable.super.allowsPatternOptimization();
    }

    @Override
    public int[] pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table, int maxTodo) {
        if (Config.fastPatternDualInput == false) return AZERO;
        if (maxTodo <= 0) return AZERO;
        if (!isActive() && !skipActiveCheck) return AZERO;
        if (!isEmpty()) return AZERO;
        if (!supportsFluids()) {
            for (int i = 0; i < table.getSizeInventory(); ++i) {
                ItemStack itemStack = table.getStackInSlot(i);
                if (itemStack == null) continue;
                if (itemStack.getItem() instanceof ItemFluidPacket) return AZERO;
            }
        }

        int items = 0;
        int fluids = 0;
        int size = table.getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = table.getStackInSlot(i);
            if (itemStack == null) continue;
            if (itemStack.getItem() instanceof ItemFluidPacket) {
                fluids++;
                if (fluids > this.fluidSlots()) {
                    clearInv();
                    return  AZERO;
                }

                mStoredFluid[fluids - 1].setFluidDirect(ItemFluidPacket.getFluidStack(itemStack));

            } else {
                items++;
                if (items > 16) {
                    clearInv();
                    return  AZERO;
                }
                mInventory[items - 1] = itemStack;

            }
        }
        markDirty();
        dirty = true;
        // classify();
        int suc = 0;

        // DualInvBuffer theBuffer=classifyForce();

        /*
         * Integer check = detailmap.get(patternDetails);
         * if(check==null){
         * currentID++;
         * detailmap.put(patternDetails,currentID );
         * check=currentID;
         * }
         */

        DualInvBuffer theBuffer = /* ((BufferedDualInputHatch) master). */classifyForce();
        if(theBuffer!=null)
        recordRecipe(theBuffer);

        // if(theBuffer!=null){
        suc++;
        maxTodo--;
        // }
        /*
         * for (DualInvBuffer inv0 : this.sortByEmpty()) {
         * if (inv0.full() == false)
         * if(inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory)||
         * inv0.classify(this.mStoredFluid, mInventory, true)
         * ){
         * theBuffer=inv0;suc++;maxTodo--;
         * break;}
         * }
         */
        if (theBuffer != null) {// if succeeded, it's safe to simply add to stacksize to push more patterns
            int todo = Math.min(theBuffer.space()
            /*
             * space() will return correct result here
             * it assumes item/fluid type is correct
             */
                , maxTodo);

            if (todo > 0) {
                for (int ix = 0; ix < theBuffer.i; ix++) {
                    if (theBuffer.mStoredItemInternalSingle[ix] != null) {
                        if (theBuffer.mStoredItemInternal[ix] == null) {
                            theBuffer.mStoredItemInternal[ix] = theBuffer.mStoredItemInternalSingle[ix].copy();
                            theBuffer.mStoredItemInternal[ix].stackSize = 0;// circuit?
                        }
                        theBuffer.mStoredItemInternal[ix].stackSize += theBuffer.mStoredItemInternalSingle[ix].stackSize
                            * todo;
                    }
                }

                for (int ix = 0; ix < theBuffer.f; ix++) {
                    if (theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount() > 0) {
                        if (theBuffer.mStoredFluidInternal[ix].getFluidAmount() <= 0) {
                            theBuffer.mStoredFluidInternal[ix].setFluid(
                                theBuffer.mStoredFluidInternalSingle[ix].getFluid()
                                    .copy());
                            theBuffer.mStoredFluidInternal[ix].getFluid().amount = 0;
                        }
                        theBuffer.mStoredFluidInternal[ix].getFluid().amount += theBuffer.mStoredFluidInternalSingle[ix]
                            .getFluidAmount() * todo;
                    }
                }
                suc += todo;
            }

        }

        saved += suc;

        justHadNewItems = true;
        return new int[]{suc};
    }

    long saved;

    @SideOnly(Side.CLIENT)
    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {

        super.getWailaBody(itemStack, currenttip, accessor, config);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            currenttip.add("Saved pushPattern calls since chunk load:" +

                accessor.getNBTData()
                    .getLong("saved"));
        }

    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {

        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.setLong("saved", saved);
    }

    @Override
    public boolean isInfBuffer() {

        return true;
    }

}

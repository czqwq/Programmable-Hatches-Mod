package reobf.proghatches.eucrafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tile.TileFluidInterface;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.item.AEItemStack;
import cofh.api.energy.IEnergyReceiver;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;
import ic2.api.energy.tile.IEnergySink;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import reobf.proghatches.eucrafting.IEUManager.IDrain;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.SISOPatternDetail;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.WrappedPatternDetail;
import reobf.proghatches.gt.metatileentity.util.IDisallowOptimize;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

// TileFluidInterface_EU.class.getName().contains("TileFluidInterface")->true
public class TileFluidInterface_EU extends TileFluidInterface
    implements ITileWithModularUI, IInstantCompletable, IEnergyConnected, IDrain,

    IGridTickable, IGridHost {

    static public IWailaDataProvider provider = new IWailaDataProvider() {

        @Override
        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {

            return null;
        }

        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

            return currenttip;
        }

        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
            UUID id = ProghatchesUtil.deser(accessor.getNBTData(), "EUFI");
            if (id.equals(zero)) {
                return currenttip;
            }
            currenttip
                .add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.UUID", id.toString()));

            currenttip.add(
                StatCollector.translateToLocalFormatted(
                    "proghatches.eu.interface.waila.V",
                    accessor.getNBTData()
                        .getLong("V")));
            currenttip.add(
                StatCollector.translateToLocalFormatted(
                    "proghatches.eu.interface.waila.EA",
                    accessor.getNBTData()
                        .getLong("EA")));
            currenttip.add(
                StatCollector.translateToLocalFormatted(
                    "proghatches.eu.interface.waila.AA",
                    String.format(
                        "%.2f",
                        accessor.getNBTData()
                            .getDouble("AA")),
                    accessor.getNBTData()
                        .getLong("A")));

            boolean validtile = accessor.getNBTData()
                .getBoolean("validtile");
            boolean hasdir = accessor.getNBTData()
                .getBoolean("hasdir");
            if (!hasdir) {
                currenttip
                    .add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.is_machine.dir"));
            } else if (!validtile) {
                currenttip.add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.is_machine.no"));
            }

            if (validtile) {
                currenttip.add(
                    StatCollector.translateToLocalFormatted(
                        "proghatches.eu.interface.waila.pass",
                        accessor.getNBTData()
                            .getInteger("pass")));
                /*
                 * currenttip.add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.halt_count",
                 * accessor.getNBTData().getInteger("succs")));
                 * currenttip.add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.fail_count",
                 * accessor.getNBTData().getInteger("fails")));
                 */
            }

            /*
             * System.out.println( StatCollector.translateToLocal(
             * "proghatches.eu.interface.waila.AA"));
             */

            return currenttip;
        }

        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

            return currenttip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
            int y, int z) {
            ProghatchesUtil.ser(tag, ((TileFluidInterface_EU) te).id, "EUFI");
            TileFluidInterface_EU pt = ((TileFluidInterface_EU) te);
            tag.setLong("V", pt.voltage);
            tag.setLong("A", pt.amp);
            tag.setLong("EA", pt.expectedamp);
            tag.setDouble("AA", pt.averageamp);
            tag.setInteger("succs", pt.succs);
            tag.setInteger("fails", pt.fails);
            tag.setBoolean("validtile", pt.getTargetTile() != null);
            tag.setBoolean(
                "hasdir",
                pt.getUp()
                    .getOpposite() != ForgeDirection.UNKNOWN);
            tag.setInteger("pass", pt.pass);
            tag.setInteger("passReason", pt.passReason);
            return tag;
        }
    };

    public TileFluidInterface_EU() {
        super();
        id = UUID.randomUUID();
        initTokenTemplate();
    }

    @Override
    public ItemStack getSelfRep() {

        return new ItemStack(MyMod.block_euinterface);
    }

    private UUID id;
    final static UUID zero = new UUID(0, 0);

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void read(NBTTagCompound data) {
        id = ProghatchesUtil.deser(data, "EUFI");
        if (id.equals(zero)) {
            id = UUID.randomUUID();
        }
        customName = data.getString("customName");
        amp = data.getLong("amp");
        voltage = data.getLong("voltage");
        accepted = data.getLong("accepted");
        averageamp = data.getDouble("averageamp");
        redstoneticks = data.getInteger("redstoneticks");
        expectedamp = data.getLong("expectedamp");
        succs = data.getInteger("succs");
        fails = data.getInteger("fails");
        pass = data.getInteger("pass");
        passReason = data.getInteger("passReason");
        is.clear();
        IntStream.range(0, data.getInteger("pending_size"))
            .forEach(s -> {

                is.add(ItemStack.loadItemStackFromNBT((NBTTagCompound) data.getTag("pending_" + s)));

            });

        initTokenTemplate();

    }

    private void initTokenTemplate() {
        token = Optional.of(new ItemStack(MyMod.eu_token, 1, 1))
            .map(s -> {
                s.stackTagCompound = new NBTTagCompound();
                ProghatchesUtil.ser(s.stackTagCompound, id, "EUFI");
                s.stackTagCompound.setLong("voltage", voltage);

                return s;
            })
            .get();

        blank_token = Optional.of(new ItemStack(MyMod.eu_token, 1, 0))
            .map(s -> {
                s.stackTagCompound = new NBTTagCompound();
                // ProghatchesUtil.ser(s.stackTagCompound, id, "EUFI");
                s.stackTagCompound.setLong("voltage", voltage);

                return s;
            })
            .get();

    }

    private boolean postEvent() {
        try {
            this.getProxy()
                .getGrid()
                .postEvent(new MENetworkCraftingPatternChange(this, getGridNode(ForgeDirection.UNKNOWN)));
            return true;
        } catch (GridAccessException ignored) {
            return false;
        }
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void write(NBTTagCompound data) {
        ProghatchesUtil.ser(data, id, "EUFI");
        if (customName != null) data.setString("customName", customName);
        data.setLong("amp", amp);
        data.setLong("voltage", voltage);
        data.setLong("accepted", accepted);
        data.setInteger("pending_size", is.size());
        data.setDouble("averageamp", averageamp);
        data.setInteger("redstoneticks", redstoneticks);
        data.setLong("expectedamp", expectedamp);
        data.setInteger("fails", fails);
        data.setInteger("succs", succs);
        data.setInteger("pass", pass);
        data.setInteger("passReason", passReason);
        for (int i = 0; i < is.size(); i++) {
            data.setTag(
                "pending_" + i,
                is.get(i)
                    .writeToNBT(new NBTTagCompound()));
        } ;

    }

    @Override
    public void setPriority(int newValue) {
        if (newValue >= Integer.MAX_VALUE - 16) newValue = Integer.MAX_VALUE - 16;
        if (newValue == Integer.MIN_VALUE) newValue = Integer.MIN_VALUE + 1;

        super.setPriority(newValue);

    }

    public ArrayList<ItemStack> phantomis = new ArrayList<>();
    public ArrayList<ItemStack> is = new ArrayList<>();
    boolean prevPower;

    public IMetaTileEntity getTargetTile() {

        TileEntity te;
        {
            int x = getTile().xCoord;
            int y = getTile().yCoord;
            int z = getTile().zCoord;
            ForgeDirection fd = this.getUp()
                .getOpposite();
            if (fd == ForgeDirection.UNKNOWN) return null;
            te = getTile().getWorldObj()
                .getTileEntity(x + fd.offsetX, y + fd.offsetY, z + fd.offsetZ);
        }
        if (te == null) return null;

        if (te instanceof IGregTechTileEntity) {

            return ((IGregTechTileEntity) te).getMetaTileEntity();

        }
        return null;

    }

    private static int getTick() {
        if (MinecraftServer.getServer() != null) return MinecraftServer.getServer()
            .getTickCounter();
        return 0;
    }

    private void resetIdleCheckStatus() {
        fails = 0;
        succs = 0;
        pass = -1;
        passReason = 0;
        if (MinecraftServer.getServer() != null) pushtick = getTick();
    }

    int pass = 2;
    // -2 new
    // -1 prepare
    // 0 checking
    // 1 pass
    // 2 pass-refunded

    int fails, succs;
    int passReason;

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(1, 1, false, false);
        // return super.tickingRequest(node, ticksSinceLastCall);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        super.tickingRequest(node, ticksSinceLastCall);
        tick();
        return TickRateModulation.SAME;
    }

    long pushtick;

    // @TileEvent(TileEventType.TICK)
    public void tick() {
        if (this.worldObj.isRemote) {
            return;
        }
        returnItems();

        /*
         * boolean pw = this.getWorldObj().isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
         * boolean downedge = pw == false && prevPower == true;
         * prevPower = pw;
         */
        boolean ok = false;
        IMetaTileEntity t = null;
        try {
            t = getTargetTile();
            if (t != null && t instanceof IIdleStateProvider) {

                boolean order = pushtick == getTick();
                // is pushpatten() executed first, or tick()?
                // same tick means pushpatten() goes first,
                // and we should wait for the next tick
                // or info collected might be obsolete
                if (pass == -1 && !order) {
                    pass = 0;
                }
                if (pass == 0) {
                    /*
                     * if(((IIdleStateProvider) t).getIdle()==1){
                     * pass=1;succs++;
                     * if(passReason==0)passReason=1;
                     * }
                     */
                    if (((IIdleStateProvider) t).failThisTick()) {
                        if (fails++ == 0) {
                            pass = 1;
                            if (passReason == 0) passReason = 2;
                        } ;// fail 1 time, assume no valid inputs, just pass it
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();;
        }

        if (pass == 1) {
            pass = 2;
            ok = true;
        }

        if (ok || redstoneticks > 0) {
            // resetIdleCheckStatus(false);
            try {

                IMEMonitor<IAEItemStack> store = getProxy().getStorage()
                    .getItemInventory();

                for (ICraftingCPU cluster : getProxy().getCrafting()
                    .getCpus()) {
                    if (cluster instanceof CraftingCPUCluster == false) {
                        continue;
                    }

                    IMEInventory<IAEItemStack> inv = ((CraftingCPUCluster) cluster).getInventory();
                    // long prevamp = amp;

                    if (refund(inv, store)) {

                        /*
                         * ((CraftingCPUCluster)cluster).addCrafting(new
                         * PatternDetail(blank_token.copy(), token.copy()),
                         * prevamp);
                         */

                        // ((CraftingCPUCluster)cluster).addEmitable(AEItemStack.create(blank_token.copy()).setStackSize(prevamp));
                        redstoneticks = 0;
                        amp = 0;
                        break;
                    }
                }

                if (refund(store, store)) {
                    amp = 0;
                    redstoneticks = 0;
                }
            } catch (GridAccessException e) {
                e.printStackTrace();
            }

        }

    }

    public boolean refund(IMEInventory<IAEItemStack> inv, IMEInventory<IAEItemStack> recver) {
        IAEItemStack ret = inv.extractItems(
            AEItemStack.create(token)
                .setStackSize(amp)

            ,
            Actionable.SIMULATE,
            new MachineSource(this));

        if (ret != null) {

            if (ret.getStackSize() == amp) {
                inv.extractItems(
                    AEItemStack.create(token)
                        .setStackSize(amp),
                    Actionable.MODULATE,
                    new MachineSource(this));

                recver.injectItems(
                    AEItemStack.create(blank_token)
                        .setStackSize(amp),
                    Actionable.MODULATE,
                    new MachineSource(this));

                return true;
            }

        }
        return false;
    }

    int redstoneticks;

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {

        returnItems();
        if (patternDetails instanceof SISOPatternDetail) {
            is.add(((SISOPatternDetail) patternDetails).out);

            // do not call returnItems() here, or items returned will not be
            // considered
            // as output
            return true;
        }
        if (patternDetails instanceof WrappedPatternDetail) {

            WrappedPatternDetail p = (WrappedPatternDetail) patternDetails;
            InventoryCrafting neo = new InventoryCrafting(new ContainerNull(), table.getSizeInventory(), 1);
            int[] count = new int[1];
            int size = table.getSizeInventory();
            for (int i = 0; i < size; i++) {
                ItemStack is = table.getStackInSlot(i);
                if (is != null && is.getItem() == MyMod.eu_token && is.stackSize > 0) {
                    count[0] += is.stackSize;
                    is = is.copy();
                    is.stackSize = 0;
                    neo.setInventorySlotContents(i, is);

                } else neo.setInventorySlotContents(i, is);;
            }

            boolean succ = super.pushPattern(p.original, neo);
            if (succ) {

                resetIdleCheckStatus();

                is.add(p.extraOut0);
            }

            return succ;
        }
        return super.pushPattern(patternDetails, table);

    }

    private void returnItems() {

        is.removeIf(s -> {

            try {
                return Optional.ofNullable(

                    this.getProxy()
                        .getStorage()
                        .getItemInventory()
                        .injectItems(AEItemStack.create(s)

                            , Actionable.MODULATE, new MachineSource(this))

                )
                    .map(IAEItemStack::getStackSize)
                    .orElse(0L) == 0L

                ;

            } catch (GridAccessException e) {
                return false;
            }
        });

        phantomis.removeIf(s -> {

            try {
                boolean suc;
                suc = Optional.ofNullable(

                    this.getProxy()
                        .getStorage()
                        .getItemInventory()
                        .injectItems(AEItemStack.create(s)

                            , Actionable.MODULATE, new MachineSource(this))

                )
                    .map(IAEItemStack::getStackSize)
                    .orElse(0L) == 0L;
                // inject items to inv to clear waitingFor
                if (suc) {

                    for (ICraftingCPU x : this.getProxy()
                        .getCrafting()
                        .getCpus()) {
                        ok: if (x instanceof CraftingCPUCluster) {
                            CraftingCPUCluster cc = (CraftingCPUCluster) x;
                            IAEItemStack all = cc.getInventory()
                                .extractItems(
                                    AEItemStack.create(s)
                                        .setStackSize(Integer.MAX_VALUE),
                                    Actionable.SIMULATE,
                                    new MachineSource(this));
                            if (all != null && all.getStackSize() > this.expectedamp) {
                                cc.getInventory()
                                    .extractItems(
                                        AEItemStack.create(s)
                                            .setStackSize(1),
                                        Actionable.MODULATE,
                                        new MachineSource(this));
                                break ok;
                            }
                            // remove from cpu internal inv
                        }
                    }

                    this.getProxy()
                        .getStorage()
                        .getItemInventory()
                        .injectItems(
                            AEItemStack.create(this.blank_token),
                            Actionable.MODULATE,
                            new MachineSource(this));
                    // inject items to inv for eusource to recycle

                }

                return suc;
            } catch (GridAccessException e) {
                return false;
            }
        });
    }

    public static ICraftingPatternDetails wrap(ICraftingPatternDetails d, ItemStack extraIn, ItemStack extraOut,
        int priority) {
        if (d.isCraftable()) return d;
        return new WrappedPatternDetail(d, extraIn, extraOut, priority);
    }

    public static class WrappedPatternDetail implements ICraftingPatternDetails, IDisallowOptimize {

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof WrappedPatternDetail)) {
                return false;
            }
            return extraIn.equals(((WrappedPatternDetail) obj).extraIn)
                && extraOut.equals(((WrappedPatternDetail) obj).extraOut)
                && original.equals(((WrappedPatternDetail) obj).original);
        }

        @Override
        public int hashCode() {

            return original.hashCode() ^ extraIn.hashCode() ^ extraOut.hashCode();
        }

        public final ICraftingPatternDetails original;
        public final IAEItemStack extraIn;
        public final IAEItemStack extraOut;
        public final ItemStack extraIn0;
        public final ItemStack extraOut0;

        public WrappedPatternDetail(ICraftingPatternDetails i, ItemStack extraIn, ItemStack extraOut, int priority) {
            this.priority = priority;
            Objects.requireNonNull(extraIn);
            Objects.requireNonNull(extraOut);
            Objects.requireNonNull(i);
            if (i.isCraftable()) {
                throw new IllegalArgumentException("workbench crafting");
            }
            original = i;
            this.extraIn = AEItemStack.create(extraIn);
            extraIn0 = extraIn;
            this.extraOut = AEItemStack.create(extraOut);
            extraOut0 = extraOut;
        }

        @Override
        public ItemStack getPattern() {

            return Optional.of(new ItemStack(MyMod.fakepattern))
                .map(s -> {
                    s.stackTagCompound = new NBTTagCompound();
                    s.stackTagCompound.setByte("type", (byte) 2);
                    s.stackTagCompound.setTag("i", extraIn0.writeToNBT(new NBTTagCompound()));
                    s.stackTagCompound.setTag("o", extraOut0.writeToNBT(new NBTTagCompound()));
                    s.stackTagCompound.setTag(
                        "p",
                        original.getPattern()
                            .writeToNBT(new NBTTagCompound()));
                    s.stackTagCompound.setInteger("pr", this.priority);

                    return s;
                })
                .get();
        }

        @Override
        public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {

            throw new IllegalStateException("Impossible");
        }

        @Override
        public boolean isCraftable() {

            return false;
        }

        @Override
        public IAEItemStack[] getInputs() {

            return concat(original.getInputs(), extraIn);
        }

        private static IAEItemStack[] concat(IAEItemStack[] a, IAEItemStack b) {
            IAEItemStack[] c = new IAEItemStack[a.length + 1];
            System.arraycopy(a, 0, c, 0, a.length);
            c[c.length - 1] = b;
            return c;
        }

        @Override
        public IAEItemStack[] getCondensedInputs() {

            return concat(original.getCondensedInputs(), extraIn);
        }

        @Override
        public IAEItemStack[] getCondensedOutputs() {

            return concat(original.getCondensedOutputs(), extraOut);
        }

        @Override
        public IAEItemStack[] getOutputs() {

            return concat(original.getOutputs(), extraOut);
        }

        @Override
        public boolean canSubstitute() {

            return original.canSubstitute();
        }

        @Override
        public boolean canBeSubstitute() {
            // TODO Auto-generated method stub
            return true;// priority==Integer.MAX_VALUE;
        }

        @Override
        public ItemStack getOutput(InventoryCrafting craftingInv, World world) {

            return original.getOutput(craftingInv, world);
        }

        @Override
        public int getPriority() {

            return Integer.MAX_VALUE;// priority;
        }

        int priority = 0;

        @Override
        public void setPriority(int priority) {
            if (priority >= Integer.MAX_VALUE - 16) priority = Integer.MAX_VALUE - 16;
            if (priority == Integer.MIN_VALUE) priority = Integer.MIN_VALUE + 1;
            original.setPriority(priority);
            this.priority = priority;
        }
    }

    public static class SISOPatternDetail implements ICraftingPatternDetails, IDisallowOptimize {

        public SISOPatternDetail(ItemStack in, ItemStack out) {
            Objects.requireNonNull(in);
            Objects.requireNonNull(out);

            this.in = in;
            this.out = out;
            i = new IAEItemStack[] { AEApi.instance()
                .storage()
                .createItemStack(in) };
            o = new IAEItemStack[] { AEApi.instance()
                .storage()
                .createItemStack(out) };

        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof SISOPatternDetail)) {
                return false;
            }
            SISOPatternDetail p = (SISOPatternDetail) obj;
            return i[0].equals(p.i[0]) && o[0].equals(p.o[0]);
        }

        @Override
        public int hashCode() {
            return i[0].hashCode() ^ o[0].hashCode();
        }

        public final ItemStack in;
        public final ItemStack out;
        public IAEItemStack[] i, o;

        @Override
        public ItemStack getPattern() {

            return Optional.of(new ItemStack(MyMod.fakepattern))
                .map(s -> {
                    s.stackTagCompound = new NBTTagCompound();
                    s.stackTagCompound.setByte("type", (byte) 1);
                    s.stackTagCompound.setTag("i", in.writeToNBT(new NBTTagCompound()));
                    s.stackTagCompound.setTag("o", out.writeToNBT(new NBTTagCompound()));
                    return s;
                })
                .get();
        }

        @Override
        public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
            throw new IllegalStateException("workbench crafting");
        }

        @Override
        public boolean isCraftable() {

            return false;
        }

        @Override
        public IAEItemStack[] getInputs() {

            return i;
        }

        @Override
        public IAEItemStack[] getCondensedInputs() {

            return i;
        }

        @Override
        public IAEItemStack[] getCondensedOutputs() {

            return o;
        }

        @Override
        public IAEItemStack[] getOutputs() {

            return o;
        }

        @Override
        public boolean canSubstitute() {

            return false;
        }

        @Override
        public ItemStack getOutput(InventoryCrafting craftingInv, World world) {

            return out;
        }

        @Override
        public int getPriority() {

            return Integer.MAX_VALUE - 1;
        }

        @Override
        public void setPriority(int priority) {

        }
    }

    private ItemStack token;
    private ItemStack blank_token;

    @Override
    public boolean isBusy() {
        // TODO Auto-generated method stub
        return super.isBusy();
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        ICraftingProviderHelper collector = new ICraftingProviderHelper() {

            @Override
            public void addCraftingOption(ICraftingMedium medium, ICraftingPatternDetails api) {
                ItemStack a = token.copy();

                ItemStack b = token.copy();
                ItemStack c = blank_token.copy();
                if (expectedamp > 0) {
                    c.stackSize = a.stackSize = b.stackSize = (int) expectedamp;

                    /*
                     * craftingTracker.addCraftingOption(TileFluidInterface_EU.
                     * this,wrap(api, c, b,0 ));
                     */

                    craftingTracker
                        .addCraftingOption(TileFluidInterface_EU.this, wrap(api, a, b, Integer.MAX_VALUE - 1));

                } else {
                    craftingTracker.addCraftingOption(TileFluidInterface_EU.this/* medium */, api);
                }
            }

            @Override
            public void setEmitable(IAEItemStack what) {
                craftingTracker.setEmitable(what);
            }
        };
        super.provideCrafting(collector);

        craftingTracker.addCraftingOption(this, new SISOPatternDetail(blank_token.copy(), token.copy()));

    }

    final static String NAME_OVERRIDE = "EU Interface";
    String customName;

    @Override
    public String getCustomName() {
        return this.hasCustomName() ? this.customName : NAME_OVERRIDE;
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && this.customName.length() > 0;
    }

    @Override
    public void setCustomName(String name) {
        this.customName = name;
    }

    @Override
    public void complete() {
        returnItems();

    }

    @Override
    public byte getColorization() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte setColorization(byte aColor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long injectEnergyUnits(ForgeDirection side, long aVoltage, long aAmperage) {

        return 0;
    }

    @Override
    public boolean inputEnergyFrom(ForgeDirection side) {

        return false;
    }

    @Override
    public boolean outputsEnergyTo(ForgeDirection side) {

        return true;
    }

    long minv, maxv;
    boolean updatev;

    long voltage;
    long amp;
    long expectedamp;
    double averageamp;
    long instantamp;

    @Override
    public long getVoltage() {

        return voltage;
    }

    long injectedamp;

    @Override
    public long doInject(long a, long v) {
        if (updatev) {
            minv = maxv = v;
            updatev = false;
        } else {
            minv = Math.min(minv, v);
            maxv = Math.max(maxv, v);
        }
        long olda = a;
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            long consume = doOutput(v, a, dir);
            a -= consume;
            injectedamp += consume;
            if (a == 0) break;
        }

        // doOutput()

        return olda - a;
    }

    @Override
    public long expectedAmp() {

        return amp - accepted;
    }

    @Override
    public void accept(long a) {

        accepted += a;
    }

    long accepted;

    @Override
    public void reset() {
        instantamp = injectedamp;
        averageamp = (injectedamp) / 32.0 + averageamp * 31 / 32;
        injectedamp = 0;
        accepted = 0;
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        updatev = true;
        ModularWindow.Builder builder = ModularWindow.builder(176, 107);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);

        builder.widget(
            ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(true, true))
                .setGetter(() -> (redstoneticks > 0) ? 1 : 0)
                .setSetter(s -> redstoneticks = s)
                .setLength(2)
                .setTextureGetter(i -> {
                    if (i == 1) return GTUITextures.OVERLAY_BUTTON_CHECKMARK;

                    return GTUITextures.OVERLAY_BUTTON_CROSS;
                })

                .addTooltip(0, LangManager.translateToLocal("proghatches.eucreafting.finish.false"))
                .addTooltip(1, LangManager.translateToLocal("proghatches.eucreafting.finish.true"))

                .setPos(8, 80)

        );/*
           * builder.widget( ((CycleButtonWidget) new
           * CoverCycleButtonWidget().setSynced(false, true)) .setGetter(() ->
           * onoff ? 1 : 0) .setSetter(s -> {onoff = s == 1;postEvent();})
           * .setLength(2) .setTextureGetter(i -> { if (i == 1) return
           * GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_ON; return
           * GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF; })
           * .addTooltip(0,
           * LangManager.translateToLocal("proghatches.part.onoff.false"))
           * .addTooltip(1,
           * LangManager.translateToLocal("proghatches.part.onoff.true"))
           * .setPos(8+16, 80)
           * );
           */
        builder.widget(
            TextWidget.dynamicString(() -> instantamp + "/" + String.format("%.2f", averageamp) + "/" + amp + "A")
                .setSynced(true)
                .setPos(58, 80)
                .addTooltips(

                    ImmutableList.of(
                        LangManager.translateToLocal("proghatches.eu.interface.hint.amp"),
                        LangManager.translateToLocal("proghatches.eu.interface.hint")

                    )));
        builder.widget(
            TextWidget.dynamicString(() -> String.format("[%d~%d]V", minv, maxv))
                .setSynced(true)
                .setPos(58, 90)
                .addTooltips(

                    ImmutableList.of(LangManager.translateToLocal("proghatches.eu.interface.hint.volt")
                    // ,
                    // LangManager.translateToLocal("proghatches.eu.interface.hint")

                    )));

        BiFunction<Supplier<Long>, Consumer<Long>, Widget> gen = (a, b) -> {

            TextFieldWidget o = Optional.of(new TextFieldWidget())
                .filter(s -> {
                    s.setText((a.get() + ""));
                    return true;
                })
                .get();
            o.setValidator(val -> {
                if (val == null) {
                    val = "";
                }
                return val;
            })
                .setSynced(true, true)
                .setGetter(() -> { return a.get() + ""; })
                .setSetter(s -> {
                    try {
                        b.accept(Math.max(Long.valueOf(s), 0l));
                    } catch (Exception e) {
                        b.accept(0l);
                    }
                    onChange();
                    o.notifyTooltipChange();
                })
                .setPattern(BaseTextFieldWidget.NATURAL_NUMS)
                .setMaxLength(50)
                .setScrollBar()
                .setFocusOnGuiOpen(false)
                .setTextColor(Color.WHITE.dark(1))

                .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
                .setPos(16, 16)
                .setSize(16 * 8, 16);

            return o;
        };
        builder.widget(
            gen.apply(() -> this.voltage, a -> { this.voltage = a; })
                .setPos(16, 16)
                .dynamicTooltip(() -> {
                    String a = GTValues.VN[0], b;
                    long v = 8;
                    for (int i = 0; i < GTValues.V.length - 1; i++) {

                        if (voltage > GTValues.V[i]) {
                            a = GTValues.VN[i + 1];
                            v = GTValues.V[i + 1];
                        }
                    }
                    b = Math.floor((100 * (float) voltage / v)) + "%";

                    return ImmutableList.of(
                        StatCollector.translateToLocal("proghatches.eu.interface.tooltip.volt.0"),
                        StatCollector.translateToLocalFormatted("proghatches.eu.interface.tooltip.volt.1", a, b));
                }

                ));
        builder.widget(
            gen.apply(
                () -> this.expectedamp,
                a -> this.expectedamp = Math.min(a, Integer.MAX_VALUE/* stacksize limit */))
                .setPos(16, 16 + 16 + 2)
                .addTooltip(StatCollector.translateToLocal("proghatches.eu.interface.tooltip.amp")));
        builder.widget(new TextWidget("V:").setPos(8, 16));
        builder.widget(new TextWidget("A:").setPos(8, 16 + 16 + 2));
        // builder.bindPlayerInventory(buildContext.getPlayer());

        return builder.build();

    }

    public void onChange() {
        IEUManager e;
        try {
            e = this.getProxy()
                .getGrid()
                .getCache(IEUManager.class);
            e.removeNode(
                this.getProxy()
                    .getNode(),
                this);
            e.addNode(
                this.getProxy()
                    .getNode(),
                this);

        } catch (GridAccessException e1) {}

        initTokenTemplate();
        postEvent();
    };/*
       * TileEntity cachedTarget; boolean isCachedTargetValid;
       * @Nullable private TileEntity getTarget() { TileEntity te; if
       * (this.isCachedTargetValid) { te = this.cachedTarget; if (te == null
       * || !te.isInvalid()) { return te; } }
       * this.isCachedTargetValid = true; te = this.getTile(); ForgeDirection
       * side = this.getSide(); return this.cachedTarget = te.getWorldObj()
       * .getTileEntity(te.xCoord + side.offsetX, te.yCoord + side.offsetY,
       * te.zCoord + side.offsetZ); }
       */

    TileEntity getTarget(ForgeDirection side) {
        TileEntity te = this.getTile();
        return te.getWorldObj()
            .getTileEntity(te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ);

    }

    private long injectEnergy(IEnergyConnected te, ForgeDirection oppositeSide, long aVoltage, long aAmperage) {

        return te.injectEnergyUnits(oppositeSide, aVoltage, aAmperage);

    }

    /* modified from PartP2PGT5Power */
    private long doOutput(long aVoltage, long aAmperage, ForgeDirection side) {

        {
            TileEntity te = this.getTarget(side);
            if (te == null) {
                return 0L;
            } else {
                ForgeDirection oppositeSide = side.getOpposite();
                if (te instanceof IEnergyConnected) {
                    return injectEnergy((IEnergyConnected) te, oppositeSide, aVoltage, aAmperage);
                } else {
                    if (te instanceof IEnergySink) {
                        if (((IEnergySink) te).acceptsEnergyFrom(this.getTile(), oppositeSide)) {
                            long rUsedAmperes = 0L;
                            while (aAmperage > rUsedAmperes && ((IEnergySink) te).getDemandedEnergy() > 0.0D
                                && ((IEnergySink) te).injectEnergy(oppositeSide, (double) aVoltage, (double) aVoltage)
                                    < (double) aVoltage)
                                ++rUsedAmperes;

                            return rUsedAmperes;
                        }
                    } else if (GregTechAPI.mOutputRF && te instanceof IEnergyReceiver) {
                        int rfOut = (int) (aVoltage * (long) GregTechAPI.mEUtoRF / 100L);
                        if (((IEnergyReceiver) te).receiveEnergy(oppositeSide, rfOut, true) == rfOut) {
                            ((IEnergyReceiver) te).receiveEnergy(oppositeSide, rfOut, false);
                            return 1L;
                        }

                        if (GregTechAPI.mRFExplosions && GregTechAPI.sMachineExplosions
                            && ((IEnergyReceiver) te).getMaxEnergyStored(oppositeSide) < rfOut * 600
                            && rfOut > 32 * GregTechAPI.mEUtoRF / 100) {
                            float tStrength = (long) rfOut < GTValues.V[0] ? 1.0F
                                : ((long) rfOut < GTValues.V[1] ? 2.0F
                                    : ((long) rfOut < GTValues.V[2] ? 3.0F
                                        : ((long) rfOut < GTValues.V[3] ? 4.0F
                                            : ((long) rfOut < GTValues.V[4] ? 5.0F
                                                : ((long) rfOut < GTValues.V[4] * 2L ? 6.0F
                                                    : ((long) rfOut < GTValues.V[5] ? 7.0F
                                                        : ((long) rfOut < GTValues.V[6] ? 8.0F
                                                            : ((long) rfOut < GTValues.V[7] ? 9.0F : 10.0F))))))));
                            int tX = te.xCoord;
                            int tY = te.yCoord;
                            int tZ = te.zCoord;
                            World tWorld = te.getWorldObj();
                            /*
                             * GTUtility
                             * .sendSoundToPlayers(tWorld, GregTechAPI.sSoundList.get(209), 1.0F, -1.0F, tX, tY, tZ);
                             */
                            tWorld.setBlock(tX, tY, tZ, Blocks.air);
                            if (GregTechAPI.sMachineExplosions) {
                                tWorld.createExplosion(
                                    null,
                                    (double) tX + 0.5D,
                                    (double) tY + 0.5D,
                                    (double) tZ + 0.5D,
                                    tStrength,
                                    true);
                            }
                        }
                    }

                    return 0L;
                }
            }
        }
    }

    @Override
    public UUID getUUID() {

        return this.id;
    }

    @Override
    public void refund(long amp) {
        this.amp -= amp;

    }

    @Override
    public ItemStack getCrafterIcon() {
        return new ItemStack(MyMod.block_euinterface);
    }

    @Override
    protected ItemStack getItemFromTile(Object obj) {
        // TODO Auto-generated method stub
        return getCrafterIcon();
    }

    public long getAmp() {
        return this.amp;
    };

    public boolean allowOvercommit() {
        return false;
    }

    @Override
    public boolean allowsPatternOptimization() {

        return expectedamp == 0;
    }
}

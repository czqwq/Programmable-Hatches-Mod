package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.oredict.OreDictionary;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.forge.ListItemHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.networking.crafting.ICraftingPatternDetails;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.ToolDictNames;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.util.GTTooltipDataCache.TooltipData;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.IDualInputInventory;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.gt.metatileentity.util.BaseSlotPatched;
import reobf.proghatches.gt.metatileentity.util.FirstObjectHolder;
import reobf.proghatches.gt.metatileentity.util.ICraftingV2;
import reobf.proghatches.gt.metatileentity.util.IInputStateProvider;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;
import reobf.proghatches.gt.metatileentity.util.ListeningFluidTank;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

public class BufferedDualInputHatch extends DualInputHatch implements IRecipeProcessingAwareDualHatch,IInputStateProvider
,ICraftingV2

{
	public Deque<Long> scheduled=new LinkedList<>();//no randomaccess, LinkedList will work fine
	

	
@Override
public int getInventoryFluidLimit() {
	/*long val= fluidBuff()*(int) (4000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1))*(mTier+1)
			;
			
		return	(int) Math.min(val, Integer.MAX_VALUE);	*/
	//return super.getInventoryFluidLimit();
	  
		return (int) ((int) (32000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1)));
}
	public int fluidLimit() {

		return (int) ((int) (128000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1)));
	}

	public int itemLimit() {

		return (int) (64 * Math.pow(4, Math.max(mTier - 3, 0)));
	}

	private static int fluidLimit(int mTier,boolean mMultiFluid) {

		return (int) ((int) (128000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1)));
	}

	private static int itemLimit(int mTier) {

		return (int) (64 * Math.pow(4, Math.max(mTier - 3, 0)));
	}
	public BufferedDualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid,
			int bufferNum, String... optional) {
		this(id, name, nameRegional, tier, getSlots(tier) + 1, mMultiFluid, bufferNum, optional);

	}

	public BufferedDualInputHatch(int id, String name, String nameRegional, int tier, int slot, boolean mMultiFluid,
			int bufferNum, String... optional) {
		super(id, name, nameRegional, tier, slot, mMultiFluid,

				(optional.length > 0 ? optional
						: reobf.proghatches.main.Config
								.get("BDH",
										ImmutableMap.<String,Object>builder()
										.put("bufferNum", bufferNum)
										.put("cap",format.format(fluidLimit(tier,mMultiFluid )))
										.put("mMultiFluid", mMultiFluid)
										.put("slots",Math.min(16, (1 + tier) * (tier + 1)))
										.put("stacksize",itemLimit(tier))
										.put("fluidSlots", fluidSlots(tier))
										//.put("supportFluid", fluid)
										.build())
												
															
				));/* ) */
		
		this.bufferNum = bufferNum;
		initBackend();

	}

	public void initTierBasedField() {
        
		if(supportsFluids())
		super.initTierBasedField();
		/*if (mMultiFluid) {
			mStoredFluid = new ListeningFluidTank[] {

					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this)

			};
		} else {

			mStoredFluid = new ListeningFluidTank[] { new ListeningFluidTank((int) (4000 * Math.pow(2, mTier)), this) };

		}
*/
	}

	public BufferedDualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
			boolean mMultiFluid, int bufferNum) {
		super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid);
		this.bufferNum = bufferNum;
		initBackend();
		this.disableSort = true;

	}

	public BufferedDualInputHatch(String aName, int aTier, int aSlots, String[] aDescription, ITexture[][][] aTextures,
			boolean mMultiFluid, int bufferNum) {
		super(aName, aTier, aSlots, aDescription, aTextures, mMultiFluid);
		this.bufferNum = bufferNum;
		initBackend();
		this.disableSort = true;

	}

	// public ItemStack[] dualItem(){return
	// filterStack.apply(inv0.mStoredItemInternal);}
	// public FluidStack[] dualFluid(){return
	// asFluidStack.apply(inv0.mStoredFluidInternal);}
	public ArrayList<DualInvBuffer> inv0 = new ArrayList<DualInvBuffer>();
	
	public class DualInvBuffer implements IDualInputInventory {
		public long tickFirstClassify=-1;
		
		protected FluidTank[] mStoredFluidInternal;
		protected ItemStack[] mStoredItemInternal;
		protected FluidTank[] mStoredFluidInternalSingle;
		protected ItemStack[] mStoredItemInternalSingle;
		public boolean recipeLocked;
		public int i;
		public int f;
		
		//public int ip=-1;
		//public int fp=-1;
		
		public boolean lock;
	
		// public boolean lock;
		public boolean full() {

			for (int index=0;index<mStoredItemInternalSingle.length;index++) {
				ItemStack i =mStoredItemInternal[index]; 
				ItemStack si =mStoredItemInternalSingle[index]; 
				if(i!=null){
				if (si!=null&&Integer.MAX_VALUE - i.stackSize <si.stackSize) {
					return true;//over flow! count as full
				}
				
				if (i.stackSize >= itemLimit()) {
					return true;
				}
				}
			}
			
			
			for (int index=0;index<mStoredFluidInternalSingle.length;index++) {
				FluidTank i =mStoredFluidInternal[index]; 
				FluidTank si =mStoredFluidInternalSingle[index]; 
				if (si!=null&&Integer.MAX_VALUE - i.getFluidAmount() <si.getFluidAmount()) {
					return true;//over flow! count as full
				}
				if (i.getFluidAmount() >= fluidLimit()) {
					return true;
				}
			
			}
			return false;

		}

		public void updateSlots() {
			for (int i = 0; i < this.i; i++)
				if (mStoredItemInternal[i] != null && mStoredItemInternal[i].stackSize <= 0) {
					mStoredItemInternal[i] = null;
				}
			for (int i = 0; i < this.f; i++)
				if (Optional.ofNullable(mStoredFluidInternal[i].getFluid()).filter(s -> s.amount == 0).isPresent()) {
					mStoredFluidInternal[i].setFluid(null);
				}

		}

		public NBTTagCompound toTag() {

			NBTTagCompound tag = new NBTTagCompound();

			for (int i = 0; i < mStoredFluidInternal.length; i++) {
				if (mStoredFluidInternal[i] != null)
					tag.setTag("mStoredFluidInternal" + i, mStoredFluidInternal[i].writeToNBT(new NBTTagCompound()));
			}
			for (int i = 0; i < mStoredFluidInternalSingle.length; i++) {
				if (mStoredFluidInternalSingle[i] != null)
					tag.setTag("mStoredFluidInternalSingle" + i,
							mStoredFluidInternalSingle[i].writeToNBT(new NBTTagCompound()));
			}
			for (int i = 0; i < mStoredItemInternal.length; i++) {
				if (mStoredItemInternal[i] != null)
					tag.setTag("mStoredItemInternal" + i, writeToNBT(mStoredItemInternal[i], new NBTTagCompound()));
			}
			for (int i = 0; i < mStoredItemInternalSingle.length; i++) {
				if (mStoredItemInternalSingle[i] != null)
					tag.setTag("mStoredItemInternalSingle" + i,
							writeToNBT(mStoredItemInternalSingle[i], new NBTTagCompound()));
			}

			tag.setInteger("i", i);
			tag.setInteger("f", f);
			tag.setBoolean("recipeLocked", recipeLocked);
			tag.setBoolean("lock", lock);
			tag.setInteger("unlockDelay",unlockDelay);
			return tag;
		}

		public void fromTag(NBTTagCompound tag) {

			if (mStoredFluidInternal != null) {
				for (int i = 0; i < mStoredFluidInternal.length; i++) {
					if (tag.hasKey("mStoredFluidInternal" + i)) {
						mStoredFluidInternal[i].readFromNBT(tag.getCompoundTag("mStoredFluidInternal" + i));
					}
				}
			}
			if (mStoredFluidInternalSingle != null) {
				for (int i = 0; i < mStoredFluidInternalSingle.length; i++) {
					if (tag.hasKey("mStoredFluidInternalSingle" + i)) {
						mStoredFluidInternalSingle[i].readFromNBT(tag.getCompoundTag("mStoredFluidInternalSingle" + i));
					}
				}
			}
			if (mStoredItemInternal != null) {
				for (int i = 0; i < mStoredItemInternal.length; i++) {
					if (tag.hasKey("mStoredItemInternal" + i)) {
						mStoredItemInternal[i] = loadItemStackFromNBT(tag.getCompoundTag("mStoredItemInternal" + i));
					}
				}
			}
			if (mStoredItemInternalSingle != null) {
				for (int i = 0; i < mStoredItemInternalSingle.length; i++) {
					if (tag.hasKey("mStoredItemInternalSingle" + i)) {
						mStoredItemInternalSingle[i] = loadItemStackFromNBT(
								tag.getCompoundTag("mStoredItemInternalSingle" + i));
					}
				}
			}
			if(i==0)
			if(tag.getInteger("i")>0)i = tag.getInteger("i");
			if(f==0)
			if(tag.getInteger("f")>0)f = tag.getInteger("f");
			recipeLocked = tag.getBoolean("recipeLocked");
			lock = tag.getBoolean("lock");
			unlockDelay=tag.getInteger("unlockDelay");
		}

		int v = 4;
		
		int unlockDelay=0;
		
		public void init(int item, int fluid) {
			i = item;
			f = fluid;
			mStoredFluidInternal = initFluidTack(new FluidTank[fluid]);
			mStoredFluidInternalSingle = initFluidTack(new FluidTank[fluid]);
			mStoredItemInternal = new ItemStack[item + v];
			mStoredItemInternalSingle = new ItemStack[item];
		}

		private FluidTank[] initFluidTack(FluidTank[] t) {
			for (int i = 0; i < t.length; i++) {
				t[i] = new FluidTank(Integer.MAX_VALUE);
			}
			return t;
		}
		public boolean isAccessibleForMulti() {
		
			/*return !isEmpty()&&
				tickFirstClassify+2<currentTick();*/
			return !isEmpty();
		}
		public long currentTick(){
			CoverableTileEntity obj = ((CoverableTileEntity)getBaseMetaTileEntity());
			return obj!=null?obj.mTickTimer:0;
			
		}
		public boolean isEmpty() {

			for (FluidTank f : mStoredFluidInternal) {
				if (f.getFluidAmount() > 0) {
					return false;
				}
			}
			for (ItemStack i : mStoredItemInternal) {

				if (i != null && i.stackSize > 0) {
					return false;
				}
			}
			return true;
		}

		public boolean clearRecipeIfNeeded() {
			if (lock) {
				unlockDelay=0;
				return !recipeLocked;
			}
			if (isEmpty()) {
				if(!recipeLocked){return true;}
				
				if(Config.delayUnlock){
					if(unlockDelay==0){unlockDelay=10;preventSleep=Math.max(preventSleep,25);return false;}
					if(unlockDelay>0){
						unlockDelay--;
						if(unlockDelay!=0)return false;
						
					}
				}
				
				for (FluidTank ft : mStoredFluidInternalSingle) {
					ft.setFluid(null);
				}
				for (int ii = 0; ii < i; ii++) {
					mStoredItemInternalSingle[ii] = null;

				}
				recipeLocked = false;
				return true;
			}else{unlockDelay=0;}
			return false;
		}
		private boolean fluidEqualsIngoreAmount(FluidTank a, FluidTank b) {
		
			if (a.getFluid() == null && a.getFluid() == null)
				return true;
			if (a.getFluid() != null && (!a.getFluid().equals(b.getFluid())))
				return false;

			return true;
		}
		   public  boolean areItemStacksEqualIngoreAmount(ItemStack p_77989_0_, ItemStack p_77989_1_)
		    {
		        return p_77989_0_ == null && p_77989_1_ == null ? true : (p_77989_0_ != null && p_77989_1_ != null ? isItemStackEqualIngoreAmount(p_77989_0_,p_77989_1_) : false);
		    }

		    /**
		     * compares ItemStack argument to the instance ItemStack; returns true if both ItemStacks are equal
		     */
		    private boolean isItemStackEqualIngoreAmount(ItemStack p_77959_1_,ItemStack thiz)
		    {
		        return false ? false : (thiz.getItem() != p_77959_1_.getItem() ? false : (thiz.getItemDamage() != p_77959_1_.getItemDamage() ? false : (thiz.stackTagCompound == null && p_77959_1_.stackTagCompound != null ? false : thiz.stackTagCompound == null || thiz.stackTagCompound.equals(p_77959_1_.stackTagCompound))));
		    }

	

		/**
		 * classify() with less check, for better performance
		 */
		public void firstClassify(ListeningFluidTank[] fin, ItemStack[] iin) {
			tickFirstClassify=currentTick();
			for (int ix = 0; ix < f; ix++) {
				mStoredFluidInternal[ix]
						.setFluid(Optional.ofNullable(fin[ix].getFluid()).map(FluidStack::copy).orElse(null));
				fin[ix].setFluidDirect(null);

			}
			for (int ix = 0; ix < i; ix++) {
				mStoredItemInternal[ix] = Optional.ofNullable(iin[ix]).map(ItemStack::copy).orElse(null);
				iin[ix] = null;
			}
			/*Long tick=tickFirstClassify+2;
			if(!tick.equals(scheduled.peekFirst()))
			{
			
				scheduled.push(tick);
			}*/
			
			markJustHadNewItems();
			onClassify();
			programLocal();
		}

		private void programLocal() {
			if (!program)
				return;
			ArrayList<ItemStack> isa = new ArrayList<>();
			for (int i = 0; i < mStoredItemInternal.length; i++) {
				ItemStack is = mStoredItemInternal[i];
				if (is == null)
					continue;
				if (is.getItem() != MyMod.progcircuit)
					continue;
				mStoredItemInternal[i] = null;
				// inv0.mStoredItemInternal[inv0.mStoredItemInternal.length-1]=

				isa.add(GTUtility.copyAmount(0, ItemProgrammingCircuit.getCircuit(is).orElse(null)));
			}

			int nums = Math.min(v, isa.size());
			if (nums == 0)
				return;

			for (int i = 0; i < v; i++) {
				if (i < nums) {
					mStoredItemInternal[this.i + i] = isa.get(i);
				} else {
					mStoredItemInternal[this.i + i] = null;
				}

			}

		}

		public boolean classify(ListeningFluidTank[] fin, ItemStack[] iin,boolean removeInputOnSuccess) {
		
			boolean hasJob = false;
			for (int ix = 0; ix < f; ix++) {
				if (fin[ix].getFluidAmount() > 0) {
					hasJob = true;
				}
				if (fluidEquals(mStoredFluidInternalSingle[ix], fin[ix])) {
					if((fin[ix].getFluidAmount()>0&&mStoredFluidInternal[ix].getFluidAmount()>0)&&!fluidEqualsIngoreAmount(mStoredFluidInternal[ix], fin[ix])){
					return false;
				}
				} else {
					return false;
				}
				
			}
			for (int ix = 0; ix < i; ix++) {
				if (iin[ix] != null && iin[ix].stackSize > 0) {
					hasJob = true;
				}
				if (ItemStack.areItemStacksEqual(mStoredItemInternalSingle[ix], iin[ix])) {
					if((iin[ix]!=null&&mStoredItemInternal[ix]!=null)&&!areItemStacksEqualIngoreAmount(mStoredItemInternal[ix], iin[ix])){
						return false;
					}
				} else {
					return false;
				}
				
			}
			if (!hasJob) {
				return false;
			}

			for (int ix = 0; ix < f; ix++) {
				mStoredFluidInternal[ix].fill(mStoredFluidInternalSingle[ix].getFluid(), true);
				if(removeInputOnSuccess)fin[ix].setFluidDirect(null);else
					if(fin[ix].getFluid()!=null)fin[ix].setFluidDirect(fin[ix].getFluid().copy());

			}
			for (int ix = 0; ix < i; ix++) {
				if (mStoredItemInternalSingle[ix] != null)
					if (mStoredItemInternal[ix] == null)
						mStoredItemInternal[ix] = mStoredItemInternalSingle[ix].copy();
					else
						mStoredItemInternal[ix].stackSize += mStoredItemInternalSingle[ix].stackSize;
				if(removeInputOnSuccess)iin[ix] = null;else
					if(iin[ix]!=null)iin[ix]=iin[ix].copy();
			}
			tickFirstClassify=-1;//make it instantly accessible
			markJustHadNewItems();
			onClassify();
			if (program)
				programLocal();
			return true;
		}

		public boolean recordRecipeOrClassify(ListeningFluidTank[] fin, ItemStack[] iin) {
			boolean readyToRecord = clearRecipeIfNeeded();
			// clearRecipeIfNeeded();
			if (recipeLocked == false && readyToRecord == true) {
				boolean actuallyFound = false;
				for (int ix = 0; ix < f; ix++) {
					if (fin[ix].getFluidAmount() > 0) {
						actuallyFound = true;
						mStoredFluidInternalSingle[ix].setFluid(fin[ix].getFluid());
					}
				}
				for (int ix = 0; ix < i; ix++) {
					if (iin[ix] != null) {
						actuallyFound = true;
						mStoredItemInternalSingle[ix] = iin[ix].copy();
					}
				}
				recipeLocked = actuallyFound;
				if (actuallyFound)
					firstClassify(fin, iin);
				return actuallyFound;
			}
			return false;
		}

		@Override
		public ItemStack[] getItemInputs() {
			/*ItemStack[] condensed = filterStack.apply(mStoredItemInternal);
		
			ItemStack additional = getStackInSlot(getCircuitSlot());
			if (additional == null)
				return condensed;

			int before_size = condensed.length;
			ItemStack[] bruh = new ItemStack[before_size + 1];
			bruh[before_size] = additional;
			System.arraycopy(condensed, 0, bruh, 0, before_size);
			return bruh;
			*/
			
			ItemStack[] condensed = filterStack.apply(mStoredItemInternal,shared.getItems());
			
			
			//if(!trunOffEnsure){condensed=ensureIntMax(condensed);}
			
			
			
			
			return condensed;
			
			
		}

		@Override
		public FluidStack[] getFluidInputs() {
			FluidStack[] condensed = asFluidStack.apply(mStoredFluidInternal,shared.getFluid());
			//if(!trunOffEnsure){condensed=ensureIntMax(condensed);}
			
			return condensed;
		}

		public int space() {
			int ret=Integer.MAX_VALUE;
			boolean found=false;
			for (int ix = 0; ix < i; ix++) {
				
				if(mStoredItemInternalSingle[ix]!=null&&mStoredItemInternalSingle[ix].stackSize>0){
					int now=0;
					if(mStoredItemInternal[ix]!=null)now=mStoredItemInternal[ix].stackSize;
					int tmp=(itemLimit()-now)/mStoredItemInternalSingle[ix].stackSize;
				if(tmp<ret){ret=tmp;found=true;}
				}
			}
			for (int ix = 0; ix < f; ix++) {
				
				if(mStoredFluidInternalSingle[ix].getFluidAmount()>0){
					int now=mStoredFluidInternal[ix].getFluidAmount();
					
					int tmp=(fluidLimit()-now)/mStoredFluidInternalSingle[ix].getFluidAmount();
				if(tmp<ret){ret=tmp;found=true;}
				}
			}
			
			if(found)return ret;
			return 0;
			
			
			
		}

	}

	int bufferNum;

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new BufferedDualInputHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
	}
	//private long lastMark;
	//avoid settting justHadNewItems to true every tick
	public void markJustHadNewItems() {
		/*long now=this.getBaseMetaTileEntity().getTimer();
		if(now>=lastMark-1){return;}
		lastMark=now;*/
	justHadNewItems =true;
		
	}
	public void initBackend() {
		for (int i = 0; i < bufferNum; i++)
			inv0.add(new DualInvBuffer());
		inv0.forEach(s -> s.init(this.mInventory.length - 1, this.mStoredFluid.length));

	}

	@SuppressWarnings("rawtypes")
	public static class CallerCheck {

		public static Supplier<Boolean> isMEInterface = () -> true;
		static Throwable t = new Throwable();
		static {
			isMEInterface = () -> {
				t.fillInStackTrace();
				// String name=t.getStackTrace()[4].getClassName();

				return t.getStackTrace()[4].getClassName().contains("appeng.util.inv.AdaptorIInventory")
						|| t.getStackTrace()[3].getClassName()
								.contains("com.glodblock.github.inventory.FluidConvertingInventoryAdaptor");
			};

			try {
				// sun.reflect.Reflection.getCallerClass(0);
				Class<?> u = Class.forName("sun.reflect.Reflection");
				Method m = u.getDeclaredMethod("getCallerClass", int.class);
				m.invoke(null, 0);
				MethodHandle mh = MethodHandles.lookup().unreflect(m);

				isMEInterface = () -> {
					try {
						Class c6 = (Class) mh.invoke(6);
						Class c5 = (Class) mh.invoke(5);

						return c6.getName().contains("appeng.util.inv.AdaptorIInventory") || c5.getName()
								.contains("com.glodblock.github.inventory.FluidConvertingInventoryAdaptor")

						;
					} catch (Throwable e) {
						e.printStackTrace();
						return true;
					}

				};

			} catch (Throwable any) {
				any.printStackTrace();
			}
		}
	}

	private boolean updateEveryTick;

	public boolean updateEveryTick() {
		return updateEveryTick;
	}
	private boolean sleep;
	private int sleepTime;
	private boolean isOnLastTick;
	// public boolean prevdirty;
	public int preventSleep;
	
	@Override
	public void startRecipeProcessingImpl() {

		if (isInputEmpty() == false&&getBaseMetaTileEntity().isAllowedToWork())
			for (DualInvBuffer inv0 : this.sortByEmpty()) {

				if (inv0.full() == false) {
					if (!inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory)) {
						if(inv0.classify(this.mStoredFluid, mInventory,true))break;;
					}
				}

				//inv0.clearRecipeIfNeeded();
			}

		super.startRecipeProcessingImpl();
	}
	public static class DeferredEvaluator{
		public DeferredEvaluator(Supplier<Boolean> provider){this.provider=provider;}
		Supplier<Boolean> provider;Boolean cache;
		public boolean get(){
			if(cache==null){cache=provider.get();}
			return cache;
		}
	}
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.onPostTick(aBaseMetaTileEntity, aTick);
		if (aBaseMetaTileEntity.getWorld().isRemote)
			return;
		//System.out.println(scheduled);
		//System.out.println(aTick+" "+scheduled.peekLast());
		Optional.ofNullable(scheduled.peekLast()).filter(s->s<aTick).ifPresent(s->{
			scheduled.removeLast();
			justHadNewItems=true;
			//inv0.forEach(st->System.out.println(st.isAccessibleForMulti()));
		});
		
		dirty = dirty || updateEveryTick();
		if (dirty) {
			updateSlots();
		}
		dirty = dirty || getBaseMetaTileEntity().hasInventoryBeenModified();
		// System.out.println(dirty);
		// dirty=dirty||(!highEfficiencyMode());
		boolean on = (this.getBaseMetaTileEntity().isAllowedToWork());
		if(isOnLastTick!=on){dirty=true;};
		isOnLastTick=on;
		
		//System.out.println("sleep);
		//Boolean inputEmpty=null;
		
		DeferredEvaluator inputEmpty=new DeferredEvaluator(this::isInputEmpty);
		if(dirty){
			sleep=false;//wake up
			sleepTime=0;
		}else if(!sleep){
			/*boolean inputEmpty=isInputEmpty();*///not dirty but awake, check if need to sleep
			if(inputEmpty.get()){
				if(preventSleep==0)
				if(Config.sleep)sleep=true;
				
			}//Zzz
		}
		if(sleep)sleepTime++;
		if(preventSleep>0){preventSleep--;sleep=false;}
		//System.out.println(sleep);
		
		
		// if(inputEmpty==null)inputEmpty=isInputEmpty();
		if(!sleep||updateEveryTick())
		 for (DualInvBuffer inv0 : this.sortByEmpty()) {
			if (on &&!inputEmpty.get()) {
				if (inv0.full() == false) {
					if (inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory)||
							inv0.classify(this.mStoredFluid, mInventory,true))break;;
					
				}
			}

			inv0.clearRecipeIfNeeded();
		}
		// prevdirty=dirty;
	
	
		if(autoAppend&&allFull&&!isInputEmpty()){
			DualInvBuffer append;
			inv0.add(append=new DualInvBuffer());
			append.init(this.mInventory.length - 1, this.mStoredFluid.length);
			allFull=false;
		}else{
			if(inv0.size()>bufferNum){
				boolean exfull=true;
				for(int i=bufferNum;i<inv0.size();i++){
					if(inv0.get(i).isEmpty()){inv0.remove(i);exfull=false;break;}
					
				}
				if(exfull/*&&inv0.size()>bufferNum*/)
				for(int i=0;i<bufferNum;i++){
					if(inv0.get(i).isEmpty()&& (!inv0.get(i).recipeLocked)){
						DualInvBuffer from = inv0.get(bufferNum);
						DualInvBuffer to = inv0.get(i);
						//to.fromTag(from.toTag());//TODO shallow copy instead
						moveTo(from.mStoredFluidInternal,to.mStoredFluidInternal);
						moveTo(from.mStoredFluidInternalSingle,to.mStoredFluidInternalSingle);
						moveTo(from.mStoredItemInternal,to.mStoredItemInternal);
						moveTo(from.mStoredItemInternalSingle,to.mStoredItemInternalSingle);
					to.f=from.f;
					to.i=from.i;
					//to.fp=from.fp;
					//to.ip=from.ip;
					to.lock=from.lock;
					to.v=from.v;
					to.recipeLocked=from.recipeLocked;
					to.tickFirstClassify=from.tickFirstClassify;
					to.unlockDelay=from.unlockDelay;
						inv0.remove(bufferNum);
						break;
					}
				}
				
			}
			
			
		}
	
	
	
	
		dirty = false;
	}
	private void moveTo(Object[] a,Object[] b){
		System.arraycopy(a, 0, b, 0, a.length);
	}
boolean autoAppend=false;
	@Override
	public ItemStack getStackInSlot(int aIndex) {
		// if(aIndex>=mInventory.length)return
		// inv0.mStoredItemInternal[aIndex-mInventory.length];
		return super.getStackInSlot(aIndex);
	}
    boolean allFull; 
	/**
	 * non-empty one fist, then append an empty one at last
	 */
	public ArrayList<DualInvBuffer> sortByEmpty() {
		ArrayList<DualInvBuffer> non_empty = new ArrayList<>();
		FirstObjectHolder<DualInvBuffer> empty = new FirstObjectHolder<>();
		inv0.forEach(s -> {
			(s.isEmpty()
					&& (!s.recipeLocked/* non-locked is considered not 'empty' */) ? empty : non_empty).add(s);
		});

		empty.opt().ifPresent(non_empty::add);
		if(!empty.opt().isPresent()){
		allFull=true;}
		// only one empty is needed, because only one buffer at maximum will be
		// filled one time

		return non_empty;
	}

	public void classify() {
		if (isRemote())
			return;
		for (DualInvBuffer inv0 : this.sortByEmpty()) {
			if (inv0.full() == false)
				if(inv0.classify(this.mStoredFluid, mInventory,true))break;
		}

	}
	public DualInvBuffer classifyForce() {
		if (isRemote())
			return null;
		for (DualInvBuffer inv0 : this.sortByEmpty()) {
			if (inv0.full() == false)
				if(inv0.classify(this.mStoredFluid, mInventory,true)||
						inv0.recordRecipeOrClassify(mStoredFluid, mInventory)
						)return inv0;
		}
		return null;

	}
	boolean dirty;

	@Override
	public void onFill() {
		// Thread.dumpStack();
		classify();
		markDirty();
		dirty = true;
	}

	@Override
	public void setInventorySlotContents(int aIndex, ItemStack aStack) {
		super.setInventorySlotContents(aIndex, aStack);

		classify();
		markDirty();
		dirty = true;
	}
final int offset=0;
	public void add1by1Slot(ModularWindow.Builder builder, int index, IDrawable... background) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.get(index).mStoredItemInternal,
				offset, 1).id(1);
		if (background.length == 0) {
			background = new IDrawable[] { getGUITextureSet().getItemSlot() };
		}
		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 1).startFromSlot(offset)
				  .slotCreator(BaseSlotPatched.newInst(inventoryHandler))
				.endAtSlot(offset).background(background).build().setPos(3, 3));
	}

	public void add2by2Slots(ModularWindow.Builder builder, int index, IDrawable... background) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.get(index).mStoredItemInternal,
				offset, 4).id(1);
		if (background.length == 0) {
			background = new IDrawable[] { getGUITextureSet().getItemSlot() };
		}
		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 2).startFromSlot(offset)
				  .slotCreator(BaseSlotPatched.newInst(inventoryHandler))
				.endAtSlot(offset+3).background(background).build().setPos(3, 3));
	}

	public void add3by3Slots(ModularWindow.Builder builder, int index, IDrawable... background) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.get(index).mStoredItemInternal,
				offset, 9).id(1);
		if (background.length == 0) {
			background = new IDrawable[] { getGUITextureSet().getItemSlot() };
		}
		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 3).startFromSlot(offset)
				  .slotCreator(BaseSlotPatched.newInst(inventoryHandler))
				.endAtSlot(offset+8).background(background).build().setPos(3, 3));
	}

	public void add4by4Slots(ModularWindow.Builder builder, int index, IDrawable... background) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.get(index).mStoredItemInternal,
				offset, 16).id(1);
		if (background.length == 0) {
			background = new IDrawable[] { getGUITextureSet().getItemSlot() };
		}
		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 4).startFromSlot(offset)
				  .slotCreator(BaseSlotPatched.newInst(inventoryHandler))
				.endAtSlot(offset+15).background(background).build().setPos(3, 3)

		);
	}



	private Widget createButtonBuffer(int id,int xoffset,int yoffset) {
		// for(int i=0;i<bufferNum;i++)
		return new ButtonWidget().setOnClick((clickData, widget) -> {
			if (clickData.mouseButton == 0) {
				if (!widget.isClient())
					widget.getContext().openSyncedWindow(BUFFER_0 + id);
			}
		}).setPlayClickSound(true).setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_PLUS_LARGE)
				.addTooltips(ImmutableList
						.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.buffer", "" + id)))
				.setSize(16, 16).setPos(xoffset + 16 * (id % 3), yoffset + 16 * (id / 3));

		/*
		 * return new ButtonWidget().setOnClick((clickData, widget) -> { if
		 * (clickData.mouseButton == 0) { widget.getContext()
		 * .openSyncedWindow(BUFFER_0); } }) .setPlayClickSound(true)
		 * .setBackground(GTUITextures.BUTTON_STANDARD,
		 * GTUITextures.OVERLAY_BUTTON_PLUS_LARGE)
		 * .addTooltips(ImmutableList.of("Place manual items")) .setSize(18, 18)
		 * .setPos(7 + offset*18, 62-18*2);
		 */

		/*
		 * return new CycleButtonWidget().setToggle(getter, setter)
		 * .setStaticTexture(picture)
		 * .setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
		 * .setTooltipShowUpDelay(TOOLTIP_DELAY) .setPos(7 + offset*18, 62-18*2)
		 * .setSize(18, 18) .setGTTooltip(tooltipDataSupplier);
		 */
	}

	static private final int BUFFER_0 = 1001;
	
	protected ModularWindow createWindow(final EntityPlayer player, int index) {
		DualInvBuffer inv0 = this.inv0.get(index);
		final int WIDTH = 18 * 6 + 6;
		final int HEIGHT = 18 * 4 + 6;
		final int PARENT_WIDTH = getGUIWidth();
		final int PARENT_HEIGHT = getGUIHeight();
		ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
		builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
		builder.setGuiTint(getGUIColorization());
		builder.setDraggable(true);
		// make sure the manual window is within the parent window
		// otherwise picking up manual items would toss them
		// See GuiContainer.java flag1

		builder.setPos((size, window) -> Alignment.Center.getAlignedPos(size, new Size(PARENT_WIDTH, PARENT_HEIGHT))
				.add(Alignment.TopRight.getAlignedPos(new Size(PARENT_WIDTH, PARENT_HEIGHT), new Size(WIDTH, HEIGHT))));
		switch (slotTierOverride(mTier)) {
		case 0:
			add1by1Slot(builder, index);
			break;
		case 1:
			add2by2Slots(builder, index);
			break;
		case 2:
			add3by3Slots(builder, index);
			break;
		default:
			add4by4Slots(builder, index);
			break;
		}

		Pos2d[] p = new Pos2d[] { new Pos2d(3 + 18 * 1, 7 - 4), new Pos2d(3 + 18 * 2, 7 - 4),
				new Pos2d(3 + 18 * 3, 7 - 4), new Pos2d(3 + 18 * 4, 7 - 4) };
		Pos2d position = p[Math.min(3, slotTierOverride(this.mTier))];

		Scrollable sc = new Scrollable().setVerticalScroll();

		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.mStoredItemInternal, 0,
				inv0.mStoredItemInternal.length).phantom();
		for (int i = 0; i < inv0.v; i++)

			sc.widget((i == 0 ? circuitSlot(inventoryHandler, inv0.i + i)
					: new SlotWidget(new BaseSlot(inventoryHandler, inv0.i + i) {

						public int getSlotStackLimit() {
							return 0;
						};

					}

					) {

						@Override
						public List<String> getExtraTooltip() {
							return Arrays
									.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"));
						}
					}.disableShiftInsert().setHandlePhantomActionClient(true).setGTTooltip(() -> new TooltipData(
							Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
									LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")),
							Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
									LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"))))).setPos(0,
											18 * i)
					
					
					);

		builder.widget(sc.setSize(18, 18 * 2).setPos(3 + 18 * 5, 3));

		{
			Pos2d position0 = new Pos2d(0, 0);

			final Scrollable scrollable = new Scrollable().setVerticalScroll();
			for (int i = 0; i < inv0.mStoredFluidInternal.length; i++) {
				position0 = new Pos2d((i % fluidSlotsPerRow()) * 18, (i / fluidSlotsPerRow()) * 18);
				scrollable.widget(new FluidSlotWidget(new LimitedFluidTank(inv0.mStoredFluidInternal[i]))
						.setBackground(ModularUITextures.FLUID_SLOT).setPos(position0));

			}

			builder.widget(
					scrollable.setSize(18 * fluidSlotsPerRow(), 18 * Math.min(4, inv0.mStoredFluidInternal.length)

					).setPos(position));
		}

		/*
		 * for (int i = 0; i < inv0.mStoredFluidInternal.length; i++) {
		 * builder.widget( new FluidSlotWidget(new
		 * LimitedFluidTank(inv0.mStoredFluidInternal[i])).setBackground(
		 * ModularUITextures.FLUID_SLOT) .setPos(position)); position=new
		 * Pos2d(position.getX(),position.getY()).add(0, 18); }
		 */

		builder.widget(TextWidget.dynamicString(() -> inv0.recipeLocked ? "§4Lock" : "§aIdle").setSynced(true)
				.setPos(3 + 18 * 5, 3 + 18 * 2));

		builder.widget(new CycleButtonWidget().setToggle(() -> !inv0.lock, (s) -> {
			inv0.lock = !s;
			inv0.clearRecipeIfNeeded();
		}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_RECIPE_LOCKED_DISABLED)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * 5, 3 + 18 * 3).setSize(18, 18)
				.setGTTooltip(() -> mTooltipCache.getData("programmable_hatches.gt.lockbuffer"))

		);
		/*
		 * builder.widget(new FakeSyncWidget.BooleanSyncer(()->
		 * inv0.recipeLocked, s->inv0.recipeLocked=s ));
		 */
		builder.widget(new FakeSyncWidget.StringSyncer(() -> inv0.toTag().toString(), s -> inv0.fromTag(cv(s))));
 ModularWindow wd = builder.build();

 wd.addInteractionListener(new Interactable() {
			@SideOnly(Side.CLIENT)
			public boolean onKeyPressed(char character, int keyCode) {
				if (!wd.isClientOnly()) {

					if ((keyCode == Keyboard.KEY_ESCAPE
							|| Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode() == keyCode)
							&& Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						ArrayList<ModularWindow> tmp = new ArrayList<>();

						wd.getContext().getMainWindow().getContext().getOpenWindows().forEach(tmp::add);
						//return true will not prevent further check(not properly implemented to me)
						//so close all other sync windows
						//and let it proceed, it will close this window
						tmp.forEach(wdd -> {
							if (wdd == wd)
								return;
							if (wdd == wd.getContext().getMainWindow())
								return;
							wdd.getContext().sendClientPacket(ModularUIContext.DataCodes.CLOSE_WINDOW, null, wdd,
									NetworkUtils.EMPTY_PACKET);
							wdd.tryClose();
						});

						return false;
					}
				}

				return false;
			}
		 
		 
		 
		 
});
		return wd;
	}
static int EX_CONFIG=985211;
	private NBTTagCompound cv(String s) {
		try {
			return (NBTTagCompound) JsonToNBT.func_150315_a(s);
		} catch (NBTException e) {
			return new NBTTagCompound();
		}
	}

	ButtonWidget createPowerSwitchButton(IWidgetBuilder<?> builder) {
		IGregTechTileEntity thiz = this.getBaseMetaTileEntity();
		Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {
			if(clickData.shift==true){
				if(widget.getContext().isClient()==false)widget.getContext().openSyncedWindow(EX_CONFIG);
				return;
				
			}
			if (thiz.isAllowedToWork()) {
				thiz.disableWorking();
			} else {
				thiz.enableWorking();
				// BufferedDualInputHatch bff =(BufferedDualInputHatch)
				// (thiz).getMetaTileEntity();
				BufferedDualInputHatch.this.dirty = true;
			}
		}).setPlayClickSoundResource(() -> thiz.isAllowedToWork() ? SoundResource.GUI_BUTTON_UP.resourceLocation
				: SoundResource.GUI_BUTTON_DOWN.resourceLocation).setBackground(() -> {
					if (thiz.isAllowedToWork()) {
						return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
								GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_ON };
					} else {
						return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
								GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF };
					}
				}).attachSyncer(new FakeSyncWidget.BooleanSyncer(thiz::isAllowedToWork, val -> {
					if (val)
						thiz.enableWorking();
					else
						thiz.disableWorking();
				}), builder)
				.addTooltip(LangManager.translateToLocal("GT5U.gui.button.power_switch"))
				.addTooltip(LangManager.translateToLocal("proghatch.gui.button.power_switch.ex"))
				.setTooltipShowUpDelay(TOOLTIP_DELAY).setPos(new Pos2d(getGUIWidth() - 18 - 3, 5)).setSize(16, 16);
		return (ButtonWidget) button;
	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		
		Scrollable sc = new Scrollable().setVerticalScroll();
		for (int i = 0; i < bufferNum; i++) {
			final int ii = i;
			buildContext.addSyncedWindow(BUFFER_0 + i, (s) -> createWindow(s, ii));
			sc.widget(createButtonBuffer(i,0,0));
		}
		
		
		buildContext.addSyncedWindow(EX_CONFIG, (s) -> createWindowEx(s));
	
	
		//.setPos(new Pos2d(getGUIWidth() - 18 - 3, 5)).setSize(16, 16)
		builder.widget(sc.setSize(16*3,16*2).setPos(3,3));
		
		builder.widget(createPowerSwitchButton(builder));
		builder.widget(new SyncedWidget() {

			@SuppressWarnings("unchecked")
			public void detectAndSendChanges(boolean init) {
				// player operation is more complicated, always set to true when
				// GUI open
				BufferedDualInputHatch.this.dirty = true;
				markDirty();
				// flush changes to client
				// sometimes vanilla detection will fail so sync it manually
				// System.out.println(last-getBaseMetaTileEntity().getTimer());
				if(getBaseMetaTileEntity()!=null)
				if (last >= getBaseMetaTileEntity().getTimer())
					getWindow().getContext().getContainer().inventorySlots.forEach(s -> ((Slot) s).onSlotChanged());

			};

			@Override
			public void readOnClient(int id, PacketBuffer buf) throws IOException {
			}

			@Override
			public void readOnServer(int id, PacketBuffer buf) throws IOException {
			}
		});
		ProghatchesUtil.attachZeroSizedStackRemover(builder, buildContext);
		super.addUIWidgets(builder, buildContext);
		// builder.widget(widget);

	}

	public int moveButtons() {
		return 0;

	}

	public void onClassify() {
		last = getBaseMetaTileEntity().getTimer();
	}

	private long last;

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		if(aNBT.hasKey("x")==false)return;
		dirty = aNBT.getBoolean("dirty");	
		int iex=aNBT.getInteger("exinvlen");
		for (int i = 0; i < bufferNum+iex; i++) {
			final int ii = i;
			if(i<bufferNum)
			inv0.get(i).fromTag((NBTTagCompound) aNBT.getTag("BUFFER_" + ii));
			else
			{
				DualInvBuffer append;
				inv0.add(append=new DualInvBuffer());
				append.init(this.mInventory.length - 1, this.mStoredFluid.length);
				inv0.get(i).fromTag((NBTTagCompound) aNBT.getTag("BUFFER_" + ii));
			}
		}
		CMMode=aNBT.getBoolean("CMMode");
		merge = aNBT.getBoolean("merge");
		justHadNewItems = aNBT.getBoolean("justHadNewItems");
		updateEveryTick = aNBT.getBoolean("updateEveryTick");
		preventSleep=aNBT.getInteger("preventSleep");
		super.loadNBTData(aNBT);
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		aNBT.setBoolean("dirty", dirty);
		for (int i = 0; i < inv0.size(); i++)

			aNBT.setTag("BUFFER_" + i, inv0.get(i).toTag());
		aNBT.setInteger("exinvlen",inv0.size()-bufferNum );
		aNBT.setBoolean("CMMode", CMMode);
		aNBT.setBoolean("merge", merge);
		aNBT.setBoolean("justHadNewItems", justHadNewItems);
		aNBT.setBoolean("updateEveryTick", updateEveryTick);
		aNBT.setInteger("preventSleep",preventSleep );
		super.saveNBTData(aNBT);
	}

	public void program() {

		for (DualInvBuffer inv0 : this.inv0) {
			inv0.programLocal();
		}
		/*
		 * for(int i=0;i<inv0.mStoredItemInternal.length-1;i++){ ItemStack
		 * is=inv0.mStoredItemInternal[i]; if(is==null)continue;
		 * if(is.getItem()!=MyMod.progcircuit)continue;
		 * inv0.mStoredItemInternal[i]=null;
		 * inv0.mStoredItemInternal[inv0.mStoredItemInternal.length-1]=
		 * GTUtility .copyAmount(0,ItemProgrammingCircuit.getCircuit(is)
		 * .orElse(null)) ; }
		 */

	}

	public class LimitedFluidTank implements IFluidTank {

		IFluidTank inner;

		public LimitedFluidTank(IFluidTank i) {
			inner = i;
		}

		@Override
		public FluidStack getFluid() {

			return inner.getFluid();
		}

		@Override
		public int getFluidAmount() {

			return inner.getFluidAmount();
		}

		@Override
		public int getCapacity() {

			return fluidLimit();
		}

		@Override
		public FluidTankInfo getInfo() {

			return inner.getInfo();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {

			return inner.fill(resource, doFill);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {

			return inner.drain(maxDrain, doDrain);
		}

	}

	boolean justHadNewItems;

	@Override
	public boolean justUpdated() {
		boolean ret = justHadNewItems;
		justHadNewItems = false;
		return ret;
	}
	class PiorityBuffer implements Comparable<PiorityBuffer>{
		PiorityBuffer(DualInvBuffer buff){this.buff=buff;
		this.piority=getPossibleCopies(buff);
		}
		DualInvBuffer buff;
		int piority;
		@Override
		public String toString() {
			return ""+piority;
		}
		@Override
		public int compareTo(PiorityBuffer o) {
		
			return -piority+o.piority;
		}
		
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Optional</* ? extends */IDualInputInventory> getFirstNonEmptyInventory() {
		markDirty();
		dirty=true;
		
		if(Config.experimentalOptimize){
		
	
		
		return (Optional) inv0.stream().filter((DualInvBuffer::isAccessibleForMulti))
				.map(s->new PiorityBuffer(s))
				.sorted().map(s->{return s.buff;})
				.findFirst();
		}else{
			
		return (Optional) inv0.stream().filter((DualInvBuffer::isAccessibleForMulti))
					.findFirst();
			
			
		}
		
		
	}

	private Predicate<DualInvBuffer> not(Predicate<DualInvBuffer> s) {
		return s.negate();
	}
boolean merge;
	@Override
	public Iterator<? extends IDualInputInventory> inventories() {
		markDirty();
		dirty=true;
		
		
	
		if(merge){
		return mergeSame();
			
		}
		
		
		if(Config.experimentalOptimize){
			
			return	inv0.stream().filter(DualInvBuffer::isAccessibleForMulti)
			.map(s->new PiorityBuffer(s))
			.sorted().map(s->{return s.buff;}).iterator();
		}
		return inv0.stream().filter(DualInvBuffer::isAccessibleForMulti).iterator();
		
	}
	
	
	

	@Override
	public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
			int z) {

		tag.setInteger("exinvlen",inv0.size()-bufferNum );
		tag.setBoolean("sleep", sleep);
		tag.setInteger("sleepTime", sleepTime);
		tag.setInteger("inv_size", bufferNum);
		IntStream.range(0, bufferNum).forEach(s -> {
			DualInvBuffer inv = inv0.get(s);
			NBTTagCompound sub = new NBTTagCompound();
			tag.setTag("No" + s, sub);
			sub.setBoolean("full", inv.full());
			sub.setBoolean("noClear", inv.lock);
			sub.setBoolean("locked", inv.recipeLocked);
			sub.setBoolean("empty", inv.isEmpty());
			RecipeTracker rt = new RecipeTracker();

			sub.setString("lock_item",
					IntStream.range(0, inv.mStoredItemInternalSingle.length)
							.mapToObj(ss -> new IndexedObject<>(ss, inv.mStoredItemInternalSingle[ss]))
							.filter(ss -> ss.holded != null).map(ss -> {
								rt.track(ss.holded, inv.mStoredItemInternal[ss.index]);
								return "#" + ss.index + ":" + ss.holded.getDisplayName() + "x" + ss.holded.stackSize;
							}).collect(StringBuilder::new, (a, b) -> a.append(((a.length() == 0) ? "" : "\n") + b),
									(a, b) -> a.append(b))
							.toString());
			sub.setString("lock_fluid",
					IntStream.range(0, inv.mStoredFluidInternalSingle.length)
							.mapToObj(ss -> new IndexedObject<>(ss, inv.mStoredFluidInternalSingle[ss]))
							.filter(ss -> ss.holded.getFluidAmount() > 0).map(ss -> {
								rt.track(ss.holded, inv.mStoredFluidInternal[ss.index]);
								return "#" + ss.index + ":" + ss.holded.getFluid().getLocalizedName() + "x"
										+ ss.holded.getFluidAmount();
							}).collect(StringBuilder::new, (a, b) -> a.append(((a.length() == 0) ? "" : "\n") + b),
									(a, b) -> a.append(b))
							.toString());

			sub.setInteger("possibleCopies", (rt.broken || (!rt.onceCompared && !inv.isEmpty())) ? -1 : rt.times);
		});

		super.getWailaNBTData(player, tile, tag, world, x, y, z);
	}private static class IndexedObject<T> {
		private T holded;
		private int index;

		IndexedObject(int i, T obj) {
			this.holded = obj;
			this.index = i;
		}
	}
	private static class RecipeTracker {
		boolean broken;
		int times;
		boolean first = true;
		boolean onceCompared;

		public void track(@Nonnull ItemStack recipe, @Nullable ItemStack storage) {
			if (recipe.getItem() instanceof ItemProgrammingCircuit) {
				onceCompared = true;
				return;
			}
			if (recipe.getItem() != (storage == null ? null : storage.getItem())) {
				broken = true;
				onceCompared = true;
				return;
			}
			int a = recipe.stackSize;
			int b = Optional.ofNullable(storage).map(s -> s.stackSize).orElse(0);
			track(a, b, false);
		}

		public void track(@Nonnull FluidTank recipe, @Nonnull FluidTank storage) {
			if (recipe.getFluid().getFluid() != Optional.of(storage).map(FluidTank::getFluid)
					.map(FluidStack::getFluid).orElse(null)) {
				broken = true;
				onceCompared = true;
				return;
			}

			int a = recipe.getFluidAmount();
			int b = storage.getFluidAmount();
			track(a, b, false);
		}

		public void track(int a, int b, boolean ignoreEmpty) {
			int t = 0;
			if (a == 0) {
				broken = true;
				return;
				/* Actually impossible */}
			if (b == 0) {
				if (!ignoreEmpty)
					broken = true;
				return;
			}
			if (b % a != 0) {
				broken = true;
				return;
			}
			t = b / a;
			if (t != times) {
				onceCompared = true;
				if (first) {
					first = false;
					times = t;
					return;
				} else {
					broken = true;
					return;
				}
			}

		}
	}
public int getPossibleCopies(DualInvBuffer toCheck){
	DualInvBuffer inv = toCheck;
	RecipeTracker rt = new RecipeTracker();

	
			IntStream.range(0, inv.mStoredItemInternalSingle.length)
					.mapToObj(ss -> new IndexedObject<>(ss, inv.mStoredItemInternalSingle[ss]))
					.filter(ss -> ss.holded != null).forEach(ss -> {
						rt.track(ss.holded, inv.mStoredItemInternal[ss.index]);
						});

			IntStream.range(0, inv.mStoredFluidInternalSingle.length)
					.mapToObj(ss -> new IndexedObject<>(ss, inv.mStoredFluidInternalSingle[ss]))
					.filter(ss -> ss.holded.getFluidAmount() > 0).forEach(ss -> {
						rt.track(ss.holded, inv.mStoredFluidInternal[ss.index]);
					});
return (rt.broken || (!rt.onceCompared && !inv.isEmpty())) ? -1 : rt.times;
	
	
	
}
	@Override
	public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {

		super.getWailaBody(itemStack, currenttip, accessor, config);
		NBTTagCompound tag = accessor.getNBTData();
		if(Config.debug||Config.dev)
		currenttip.add(
				
				"sleep:"+tag.getBoolean("sleep")+" "
				+tag.getInteger("sleepTime")
				
				);
		int idle[]=new int[1];
		IntStream.range(0, tag.getInteger("inv_size")).forEach(s -> {
			NBTTagCompound sub = (NBTTagCompound) tag.getTag("No" + s);
			boolean noClear = sub.getBoolean("noClear");
			int st = (sub.getBoolean("full") ? 1 : 0) + (sub.getBoolean("empty") ? 2 : 0)
					+ (sub.getBoolean("locked") ? 4 : 0);
			String info = "";
			switch (st) {
			case 0b000:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.000");
				break;
			case 0b001:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.001");
				break;
			case 0b010:
				idle[0]++;
				if(idle[0]>5)return;
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.010");
				break;
			case 0b011:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.011");
				break;
			case 0b100:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.100");
				break;
			case 0b101:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.101");
				break;
			case 0b110:
				info = noClear ? LangManager.translateToLocal("programmable_hatches.buffer.waila.110.0")
						: LangManager.translateToLocal("programmable_hatches.buffer.waila.110.1");
				break;
			case 0b111:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.111");
				break;

			}
			String cpinfo = "";
			int copies = sub.getInteger("possibleCopies");
			if (copies == -1 && (sub.getBoolean("locked"))// if not locked, do
															// not warn about
															// the copies
					&& (!sub.getBoolean("empty"))// if empty, actual copies will
													// be zero but will count as
													// broken, so do not warn.
			)
				cpinfo = cpinfo + LangManager.translateToLocal("programmable_hatches.buffer.waila.broken");
			if (copies > 0) {
				cpinfo = cpinfo + LangManager.translateToLocalFormatted("programmable_hatches.buffer.waila.copies",
						copies + "");
				if (!sub.getBoolean("locked")) {
					cpinfo += "???STRANGE SITUATION???";
				}
			}
			currenttip.add("#" + s + " " + info + " " + cpinfo);
			String lock_item = sub.getString("lock_item");
			String lock_fluid = sub.getString("lock_fluid");
			if ((!lock_item.isEmpty()) && (!lock_item.isEmpty())) {
				// currenttip.add();
				currenttip.add(" " + LangManager.translateToLocal("programmable_hatches.buffer.waila.present"));

			}
			if (!lock_item.isEmpty())
				Arrays.stream(lock_item.split("\n")).map(ss -> " " + ss).forEach(currenttip::add);
			if (!lock_fluid.isEmpty())
				Arrays.stream(lock_fluid.split("\n")).map(ss -> " " + ss).forEach(currenttip::add);

		});
		;
		if(idle[0]>5)
		currenttip.add(LangManager.translateToLocalFormatted("programmable_hatches.buffer.waila.hidden",(idle[0]-5)+""));
		if(tag.getInteger("exinvlen")>0)
		currenttip.add("Extra buffer:"+tag.getInteger("exinvlen"));
	}

	private Boolean isRemote;

	public boolean isRemote() {
		if (isRemote == null)
			isRemote = FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;// this.getBaseMetaTileEntity().getWorld().isRemote;
		return isRemote;
	}

	@Override
	public void updateSlots() {
		inv0.forEach(DualInvBuffer::updateSlots);
		super.updateSlots();
	}

	
	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		
		/*mergeSame().forEachRemaining(s->{
			
			System.out.println(Arrays.toString(s.getItemInputs()));
			
			
		});*/
		BaseMetaTileEntity tile = (BaseMetaTileEntity) this.getBaseMetaTileEntity();
		if (tile.isServerSide()) {
			if (!tile.privateAccess() || aPlayer.getDisplayName().equalsIgnoreCase(tile.getOwnerName())) {
				final ItemStack tCurrentItem = aPlayer.inventory.getCurrentItem();
				if (tCurrentItem != null) {
					boolean suc = false;
					for (int id : OreDictionary.getOreIDs(tCurrentItem)) {
						if (OreDictionary.getOreName(id).equals(ToolDictNames.craftingToolFile.toString())) {
							suc = true;
							break;
						}
						;
					}
					if (suc) {
						GTModHandler.damageOrDechargeItem(tCurrentItem, 1, 1000, aPlayer);
						GTUtility.sendSoundToPlayers(tile.getWorld(), SoundResource.IC2_TOOLS_WRENCH, 1.0F, -1,
								tile.getXCoord(), tile.getYCoord(), tile.getZCoord());
						updateEveryTick = !updateEveryTick;

						GTUtility.sendChatToPlayer(aPlayer, "updateEveryTick:" + updateEveryTick);
						/*
						 * GTUtility .sendChatToPlayer(aPlayer,
						 * LangManager.translateToLocal(
						 * "programmable_hatches.gt.updateEveryTick") );
						 */
						aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.updateEveryTick"));

						markDirty();
						return true;
					}

				}
				if (tCurrentItem != null) {
					boolean suc = false;
					for (int id : OreDictionary.getOreIDs(tCurrentItem)) {
						if (OreDictionary.getOreName(id).equals(ToolDictNames.craftingToolSaw.toString())) {
							suc = true;
							break;
						}
						;
					}
					if (suc) {
						GTModHandler.damageOrDechargeItem(tCurrentItem, 1, 1000, aPlayer);
						GTUtility.sendSoundToPlayers(tile.getWorld(), SoundResource.IC2_TOOLS_CHAINSAW_CHAINSAW_USE_TWO, 1.0F, -1,
								tile.getXCoord(), tile.getYCoord(), tile.getZCoord());
						/*merge = !merge;

						GTUtility.sendChatToPlayer(aPlayer, "merge:" + merge);
						
						aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.merge"));
*/
						markDirty();
						return true;
					}

				}
			}
		}

		return super.onRightclick(aBaseMetaTileEntity, aPlayer);
	}
	


	
	@Override
	public CheckRecipeResult endRecipeProcessingImpl(MTEMultiBlockBase controller) {
		dirty = true;
		return super.endRecipeProcessingImpl(controller);
	}

	@Override
	public void onBlockDestroyed() {
		IGregTechTileEntity te = this.getBaseMetaTileEntity();
		World aWorld = te.getWorld();
		int aX = te.getXCoord();
		short aY = te.getYCoord();
		int aZ = te.getZCoord();
		for (DualInvBuffer inv : this.inv0)
			for (int i = 0; i < inv.mStoredItemInternal.length; i++) {
				final ItemStack tItem = inv.mStoredItemInternal[i];
				if ((tItem != null) && (tItem.stackSize > 0)) {
					final EntityItem tItemEntity = new EntityItem(aWorld, aX + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
							aY + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F, aZ + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
							new ItemStack(tItem.getItem(), tItem.stackSize, tItem.getItemDamage()));
					if (tItem.hasTagCompound()) {
						tItemEntity.getEntityItem().setTagCompound((NBTTagCompound) tItem.getTagCompound().copy());
					}
					tItemEntity.motionX = (XSTR_INSTANCE.nextGaussian() * 0.05D);
					tItemEntity.motionY = (XSTR_INSTANCE.nextGaussian() * 0.25D);
					tItemEntity.motionZ = (XSTR_INSTANCE.nextGaussian() * 0.05D);
					aWorld.spawnEntityInWorld(tItemEntity);
					tItem.stackSize = 0;
					inv.mStoredItemInternal[i] = null;
				}
			}
		super.onBlockDestroyed();
	}
	public boolean isInputEmpty() {

		for (FluidTank f : mStoredFluid) {
			if (f.getFluidAmount() > 0) {
				return false;
			}
		}
		for (ItemStack i : mInventory) {

			if (i != null && i.stackSize > 0) {
				return false;
			}
		}
		return true;
	}
	/*@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
			float aX, float aY, float aZ) {
	
		return super.onRightclick(aBaseMetaTileEntity, aPlayer, side, aX, aY, aZ);
	}*/
	@SuppressWarnings("unchecked")
	public Iterator<? extends IDualInputInventory> mergeSame(){
		
		
		 class Wrapper{
			 DualInvBuffer d;
			 public Wrapper(DualInvBuffer s) {
			d=s;
			}
           boolean fast=true;;
           private int sft(int i,int t){
        	  /* i=(i*17)%32;
        	   return (i<<t)|(i>>>(32-t));*/return i^t;
           }
			@Override
			public int hashCode() {
				if(fast){
					int c=0;
					int hash=0;
					 for(int i=0;i<d.mStoredFluidInternalSingle.length;i++){
							FluidStack f=d.mStoredFluidInternalSingle[i].getFluid();
							if(f!=null)
							{hash=hash^sft(f.getFluidID(),c);}c++;
						 }
					 for(int i=0;i<d.mStoredItemInternalSingle.length;i++){
						 ItemStack f=d.mStoredItemInternalSingle[i];
							if(f!=null){
							{hash=hash^sft(
									Item.getIdFromItem(f.getItem())|f.getItemDamage()
									
									,c);}c++;
							}
					}
					return hash;
				}
				
				
				
				int hash=0;
				 for(int i=0;i<d.mStoredFluidInternalSingle.length;i++){
					FluidStack f=d.mStoredFluidInternalSingle[i].getFluid();
					if(f!=null)
					hash^= f.hashCode();
					int a=hash&1;
					hash=hash>>>1;
					if(a!=0)hash|=0x80000000;
				 }
				 for(int i=0;i<d.mStoredItemInternalSingle.length;i++){
					 ItemStack f=d.mStoredItemInternalSingle[i];
						if(f!=null){
						hash^= 
						f.stackSize*31+
						+Item.getIdFromItem(f.getItem());
						if(f.getTagCompound()!=null){
							hash^=f.getTagCompound().hashCode();
						}
						}
						int a=hash&1;
						hash=hash>>>1;
						if(a!=0)hash|=0x80000000;
				 }
				
				return hash;
			}
			 
			 @Override
			public boolean equals(Object obj) {
				 if(obj==this){return true;}
				 boolean empty=true;
				 DualInvBuffer a=d;
				 DualInvBuffer b=((Wrapper) obj).d;
				 for(int i=0;i<a.mStoredFluidInternalSingle.length;i++){
					 if(!fluidEquals(
					 a.mStoredFluidInternalSingle[i],
					 b.mStoredFluidInternalSingle[i])	 
					){return false;}
					 if(a.mStoredFluidInternalSingle[i].getFluidAmount()>0)
					 empty=false;
					 
				 }
				 for(int i=0;i<a.mStoredItemInternalSingle.length;i++){
					 if(!ItemStack.areItemStacksEqual(
					 a.mStoredItemInternalSingle[i],
					 b.mStoredItemInternalSingle[i])	 
					){return false;}
					 if(a.mStoredItemInternalSingle[i]!=null)
					 empty=false;
					 
				 }
				 if(empty)return false;
				return true;
			}
			 
		 }
		 
		 
		 
		  Multimap<Wrapper,DualInvBuffer> a=HashMultimap
				 .create();
		inv0.stream().filter((DualInvBuffer::isAccessibleForMulti))
		 .forEach(
				 s->{
					 a.put(new Wrapper(s), s);
				 }
		);
		return (Iterator<? extends IDualInputInventory>) a.asMap().values().stream().map(
				s->{
					if(s.size()==1){return s.iterator().next();}
				return	new IDualInputInventory(){
					void init(){
						Iterator<DualInvBuffer> itr = s.iterator();
						int icount=0;
						ItemStack[][] idata=new ItemStack[s.size()][];
						int fcount=0;
						FluidStack[][] fdata=new FluidStack[s.size()][];
						for(int i=0;i<s.size();i++){
							DualInvBuffer e = itr.next();
							idata[i]=filterStack.apply(e.mStoredItemInternal);
							icount+=idata[i].length;
							fdata[i]=asFluidStack.apply(e.mStoredFluidInternal);
							fcount+=fdata[i].length;
						}
						i=new ItemStack[icount];
						f=new FluidStack[fcount];
						int ic=0;
						for(ItemStack[] ii:idata){
							for(ItemStack iii:ii){
								i[ic]=iii;
								ic++;
							}
						}
						ic=0;
						for(FluidStack[] ii:fdata){
							for(FluidStack iii:ii){
								f[ic]=iii;
								ic++;
							}
						}
						i=filterStack.apply(i,shared.getItems());
						if(!shared.isDummy())//dummy->no extra fluid
						f=asFluidStack.apply(f,shared.getFluid());
					}
					ItemStack[] i;
					FluidStack[] f;
					@Override
					public ItemStack[] getItemInputs() {
						if(i==null)init();
						
						return i;
					}
				
					@Override
					public FluidStack[] getFluidInputs() {
						if(f==null)init();
						return f;
					}};
				}
				).iterator();
	}	
	
	
	
	static public  boolean fluidEquals(FluidTank a, FluidTank b) {
		// if(a==b)return false;
		// if(a==null||b==null)return false;
		if (a.getFluidAmount() != b.getFluidAmount())
			return false;
		if (a.getFluid() == null && a.getFluid() == null)
			return true;
		if (a.getFluid() != null && (!a.getFluid().equals(b.getFluid())))
			return false;

		return true;
	}
	protected ModularWindow createWindowEx(final EntityPlayer player) {
		
		final int WIDTH = 18 * 6 + 6;
		final int HEIGHT = 18 * 4 + 6;
		final int PARENT_WIDTH = getGUIWidth();
		final int PARENT_HEIGHT = getGUIHeight();
		ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
		builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
		builder.setGuiTint(getGUIColorization());
		builder.setDraggable(true);
	

		builder.setPos((size, window) -> Alignment.Center.getAlignedPos(size, new Size(PARENT_WIDTH, PARENT_HEIGHT))
				.add(Alignment.TopRight.getAlignedPos(new Size(PARENT_WIDTH, PARENT_HEIGHT), new Size(WIDTH, HEIGHT))));
		
		builder.widget(new CycleButtonWidget().setToggle(() -> updateEveryTick, (s) -> {
			updateEveryTick = s;
			
		}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * 0, 3 + 18 * 0).setSize(18, 18)
				.setGTTooltip(() -> mTooltipCache.getData("programmable_hatches.gt.forcecheck"))

		);
		/*builder.widget(new CycleButtonWidget().setToggle(() ->!trunOffEnsure , (s) -> {
			trunOffEnsure =! s;
			
		}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * 1, 3 + 18 * 0).setSize(18, 18)
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.ensureintmax.0"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.ensureintmax.1"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.ensureintmax.2"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.ensureintmax.3"))
		);
		*/
		builder.widget(new CycleButtonWidget().setToggle(() ->CMMode , (s) -> {
			CMMode = s;
			
		}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * 1, 3 + 18 * 0).setSize(18, 18)
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.cmmode.0"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.cmmode.1"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.cmmode.2"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.cmmode.3"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.cmmode.4"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.cmmode.5"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.cmmode.6"))
		);
		
		builder.widget(new CycleButtonWidget().setToggle(() ->merge , (s) -> {
			merge = s;
			
		}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * 2, 3 + 18 * 0).setSize(18, 18)
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.merge.0"))
						.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.merge.1"))
						
		);
		if(isInfBuffer()||shared.infbufUpgrades>0)
		builder.widget(new CycleButtonWidget().setToggle(() ->autoAppend , (s) -> {
			autoAppend = s;
			
		}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * 3, 3 + 18 * 0).setSize(18, 18)
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.elasticbuffer.0"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.elasticbuffer.1"))	
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.elasticbuffer.2"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.elasticbuffer.3"))	
						
					
						
		);
		
		
		
		
		return builder.build();
		
	}public boolean isInfBuffer(){
		return false;
	}
	public boolean isInputEmpty(BufferedDualInputHatch master) {

		for (FluidTank f : master.mStoredFluid) {
			if (f.getFluidAmount() > 0) {
				return false;
			}
		}
		for (ItemStack i : master.mInventory) {

			if (i != null && i.stackSize > 0) {
				return false;
			}
		}
		return true;
	}

	public void clearInv(BufferedDualInputHatch master) {

		for (FluidTank f : master.mStoredFluid) {
			f.setFluid(null);
		}
		for (int i = 0; i < master.mInventory.length; i++) {

			if (master.isValidSlot(i)) {
				master.mInventory[i] = null;
			}
		}

	}
	boolean CMMode=false;
	@Override
	public boolean pushPatternCM(ICraftingPatternDetails patternDetails, InventoryCrafting table,
			ForgeDirection ejectionDirection) {
		BufferedDualInputHatch master = this;
		if (this instanceof PatternDualInputHatch) {
			PatternDualInputHatch dih = ((PatternDualInputHatch) this);
			try{
			dih.skipActiveCheck=true;
			return dih.pushPattern(patternDetails, table);
			}finally{dih.skipActiveCheck=false;}
		}
		if (master != null) {
			if (!isInputEmpty(master)) {
				return false;
			}

			int i = 0;
			int f = 0;
			int ilimit = master.getInventoryStackLimit();
			int flimit = master.getInventoryFluidLimit();
			boolean isplit = master.disableLimited;
			boolean fsplit = master.fluidLimit==0;
			for (int index = 0; index < table.getSizeInventory(); index++) {
				ItemStack is = (table.getStackInSlot(index));
				if (is == null)
					continue;
				is = is.copy();
				if (is.getItem() instanceof ItemFluidPacket) {
					FluidStack fs = ItemFluidPacket.getFluidStack(is);
					if (fs == null) {
						continue;
					}
					while (fs.amount > 0) {
						if (f >= master.mStoredFluid.length) {
							clearInv(master);
							return false;
						}
						int tosplit = Math.min(fs.amount, flimit);
						fs.amount -= tosplit;
						if ((!fsplit) && fs.amount > 0) {
							clearInv(master);
							return false;
						}
						FluidStack splitted = new FluidStack(fs.getFluid(), tosplit);
						master.mStoredFluid[f].setFluidDirect(splitted);
						f++;
					}

				} else {
					while (is.stackSize > 0) {
						if (master.isValidSlot(i) == false) {
							clearInv(master);
							return false;
						}
						ItemStack splitted = is.splitStack(Math.min(is.stackSize, ilimit));
						if ((!isplit) && is.stackSize > 0) {
							clearInv(master);
							return false;
						}
						master.mInventory[i] = splitted;
						i++;
					}
				}

			}
			if(master instanceof BufferedDualInputHatch){
				((BufferedDualInputHatch) master).classifyForce();
			}
			return true;// hoo ray
		}

		return false;
	}
	@Override
	public boolean acceptsPlansCM() {
		
		return CMMode;
	}
	@Override
	public boolean enableCM() {
	
		return CMMode;
	}
	
	
	/*@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if(CMMode)return EMPTY_TK;
		return super.getTankInfo(from);
	}
	private FluidTankInfo[] EMPTY_TK=new FluidTankInfo[0];
	private int[] EMPTY_INT=new int[0];
	@Override
	public int[] getAccessibleSlotsFromSide(int ordinalSide) {
		if(CMMode)return EMPTY_INT;
		return super.getAccessibleSlotsFromSide(ordinalSide);
	}*/
}

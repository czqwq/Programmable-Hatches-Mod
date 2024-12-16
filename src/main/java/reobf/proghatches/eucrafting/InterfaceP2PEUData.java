package reobf.proghatches.eucrafting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.exceptions.FailedConnection;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.SelectedPart;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.DualityInterface;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.GridConnection;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.gui.modularui.CoverUIBuildContext;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IMachineProgress;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.ISerializableObject;

import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import reobf.proghatches.eucrafting.AECover.Data;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;

public class InterfaceP2PEUData implements AECover.IMemoryCardSensitive, Data, IInterfaceHost, IGridTickable,
		IUpgradeableHost, ICustomNameObject, IConfigurableObject, IPriorityHost, IActualSideProvider {
	public void setTag(NBTTagCompound tagCompound) {
		tag = tagCompound;
	}
	public IInterfaceHost getInterfaceOrNull(){return duality;};
	public NBTTagCompound getTag() {
		return tag;
	}

	NBTTagCompound tag;

	public boolean memoryCard(EntityPlayer entityPlayer) {
		entityPlayer.addChatComponentMessage(new ChatComponentTranslation("programmable_hatches.cover.ae.memorycard"));

		return false;
	};

	@Override
	public ISerializableObject readFromPacket(ByteArrayDataInput aBuf, EntityPlayerMP aPlayer) {
		tmpout = aBuf.readBoolean();
		return Data.super.readFromPacket(aBuf, aPlayer);
	}

	boolean tmpout;// tell client if this is output side, nested interface
					// itself(including in/out state) is not written to packet

	@Override
	public void writeToByteBuf(ByteBuf aBuf) {
		aBuf.writeBoolean(tmpout = duality.isOutput());
		Data.super.writeToByteBuf(aBuf);
	}

	public InterfaceP2PEUData() {
	}

	private TileEntity faketile = new TileEntity();

	public boolean supportFluid() {
		return true;
	}

	IPartHost fakehost = new Host();

	public class Host implements IPartHost, IActualSideProvider, InterfaceData.DisabledInventory {

		@Override
		public SelectedPart selectPart(Vec3 pos) {
			
			return null;
		}

		@Override
		public void removePart(ForgeDirection side, boolean suppressUpdate) {
			

		}

		@Override
		public void partChanged() {
			

		}

		@Override
		public void notifyNeighbors() {
			

		}

		@Override
		public void markForUpdate() {
			

		}

		@Override
		public void markForSave() {
			

		}

		@Override
		public boolean isInWorld() {
			
			return false;
		}

		@Override
		public boolean isEmpty() {
			
			return false;
		}

		@Override
		public boolean isBlocked(ForgeDirection side) {
			
			return false;
		}

		@Override
		public boolean hasRedstone(ForgeDirection side) {
			
			return false;
		}

		@Override
		public TileEntity getTile() {
			
			return InterfaceP2PEUData.this.getTileEntity();
		}

		@Override
		public IPart getPart(ForgeDirection side) {
			
			return null;
		}

		@Override
		public DimensionalCoord getLocation() {
			
			return null;
		}

		@Override
		public Set<LayerFlags> getLayerFlags() {
			
			return null;
		}

		@Override
		public IFacadeContainer getFacadeContainer() {
			
			return null;
		}

		@Override
		public AEColor getColor() {
			
			return null;
		}

		@Override
		public void clearContainer() {
			

		}

		@Override
		public void cleanup() {
			

		}

		@Override
		public boolean canAddPart(ItemStack part, ForgeDirection side) {
			
			return false;
		}

		@Override
		public ForgeDirection addPart(ItemStack is, ForgeDirection side, EntityPlayer owner) {
			
			return null;
		}

		@Override
		public ForgeDirection getActualSide() {
			
			return side;
		}
	};;

	AENetworkProxy gridProxy;
	ForgeDirection side = ForgeDirection.UNKNOWN;
	DimensionalCoord pos = new DimensionalCoord(0, 0, 0, -1000);

	public AENetworkProxy getGridProxy() {
		return gridProxy;
	}

	public void setGridProxy(AENetworkProxy gridProxy) {
		this.gridProxy = gridProxy;
	}

	public ForgeDirection getSide() {
		return side;
	}

	public void setSide(ForgeDirection side) {
		this.side = side;
	}

	public DimensionalCoord getPos() {
		return pos;
	}

	public void setPos(DimensionalCoord pos) {
		this.pos = pos;
	}

	public void onReady() {
		duality.addToWorld();
	};

	PartEUP2PInterface duality = new PartEUP2PInterface(new ItemStack(MyMod.euinterface_p2p)).markCoverData(this);

	{
		duality.setPartHostInfo(ForgeDirection.UNKNOWN, fakehost, getTileEntity());
	}

	/*
	 * { public TileEntity getTileEntity() {return
	 * InterfaceP2PData.this.getTileEntity();}; public TileEntity getTile() {
	 * return this.getTileEntity();}; public IPartHost getHost() { return
	 * fakehost;};
	 * 
	 * };
	 */
	@MENetworkEventSubscribe
	public void stateChange(final MENetworkChannelsChanged c) {
		this.duality.getInterfaceDuality().notifyNeighbors();
	}

	@Override
	public int getInstalledUpgrades(final Upgrades u) {
		return this.duality.getInstalledUpgrades(u);
	}

	@MENetworkEventSubscribe
	public void stateChange(final MENetworkPowerStatusChange c) {
		this.duality.getInterfaceDuality().notifyNeighbors();
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		this.duality.provideCrafting(craftingTracker);

	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		
		return this.duality.pushPattern(patternDetails, table);
	}

	@Override
	public boolean isBusy() {
		
		return this.duality.isBusy();
	}

	@Override
	public void gridChanged() {
		this.duality.gridChanged();
	}

	@Override
	public TileEntity getTile() {
		
		return getTileEntity();
	}

	@Override
	public IConfigManager getConfigManager() {
		
		return this.duality.getConfigManager();
	}

	@Override
	public IInventory getInventoryByName(String name) {
		
		return this.duality.getInventoryByName(name);
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs() {
		
		return this.duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
		
		return this.duality.injectCraftedItems(link, items, mode);
	}

	@Override
	public void jobStateChange(ICraftingLink link) {
		this.duality.jobStateChange(link);

	}

	@Override
	public IGridNode getActionableNode() {

		return this.getProxy().getNode();
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {

		return this.getProxy().getNode();
	}

	@Override
	public void securityBreak() {

	}

	@Override
	public DimensionalCoord getLocation() {

		return this.pos;
	}

	@Override
	public IInventory getPatterns() {

		return this.duality.getPatterns();
	}

	@Override
	public String getName() {

		return getCustomName();
	}

	@Override
	public boolean shouldDisplay() {
		
		return true;
	}

	@Override
	public DualityInterface getInterfaceDuality() {
		
		return this.duality.getInterfaceDuality();
	}

	@Override
	public EnumSet<ForgeDirection> getTargets() {
		
		return EnumSet.of(ForgeDirection.UNKNOWN);
	}

	@Override
	public TileEntity getTileEntity() {

		if (faketile.getWorldObj() == null) {

			faketile.setWorldObj(pos.getWorld());
			faketile.xCoord = pos.x;
			faketile.yCoord = pos.y;
			faketile.zCoord = pos.z;
		}
		;

		return faketile;
	}

	@Override
	public void saveChanges() {
		duality.saveChanges();

	}

	@Override
	public NBTBase saveDataToNBT() {
		if(isWailaCall())return new NBTTagCompound();
		NBTTagCompound t = (NBTTagCompound) Data.super.saveDataToNBT();
		((NBTTagCompound) t).setInteger("p", p);
		duality.writeToNBT((NBTTagCompound) t);
		t.setBoolean("inputdetect", inputdetect);
		t.setBoolean("delay", delay);
		t.setBoolean("worksensitive", worksensitive);
		if(duality.hasCustomName())((NBTTagCompound) t).setString("realName", duality.getCustomName());
		
		return t;
	}

	@Override
	public void loadDataFromNBT(NBTBase aNBT) {
		delay = ((NBTTagCompound) aNBT).getBoolean("delay");
		inputdetect = ((NBTTagCompound) aNBT).getBoolean("inputdetect");
		worksensitive = ((NBTTagCompound) aNBT).getBoolean("worksensitive");

		Data.super.loadDataFromNBT(aNBT);
		// System.out.println(pos.getWorld());
		p = ((NBTTagCompound) aNBT).getInteger("p");
		faketile.xCoord = pos.x;
		faketile.yCoord = pos.y;
		faketile.zCoord = pos.z;
		faketile.setWorldObj(pos.getWorld());
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {

			duality.readFromNBT((NBTTagCompound) aNBT);

		}
		if(((NBTTagCompound) aNBT).hasKey("realName"))duality.setCustomName(((NBTTagCompound) aNBT).getString("realName"));
		
		/*
		 * else{ duality.delay=((NBTTagCompound) aNBT).getBoolean("delay");
		 * duality.inputdetect=((NBTTagCompound)
		 * aNBT).getBoolean("inputdetect");
		 * 
		 * };
		 */

	}

	public void update(ICoverable aTileEntity) {

		Iterator<IGridConnection> it = getProxy().getNode().getConnections().iterator();
		IGridConnection item = null;
		IGridNode thenode = this.duality.getProxy().getNode();
		boolean found = false;
		while (it.hasNext()) {
			item = it.next();
			if (item.b() == thenode || item.a() == thenode) {
				found = true;
				break;
			}
			;

		}

		if (found == false) {
			try {
				new GridConnection(getProxy().getNode(), thenode, side);

				MyMod.LOG.info("Internal Node connect@" + getPos());
			} catch (FailedConnection e) {
				e.printStackTrace();
			}

		}
		;
/*
		if (worksensitive && (aTileEntity instanceof IMachineProgress)) {
			boolean on = (((IMachineProgress) aTileEntity).hasThingsToDo());
			boolean delayon = value > 0;
			boolean inputon = false;
			if (delayon) {
				value--;
			}
			if (on && delay) {
				value = 101;
			}
			if (inputdetect) {
				inputon = true;
				if ((aTileEntity instanceof IGregTechTileEntity)) {
					IGregTechTileEntity mt = (IGregTechTileEntity) aTileEntity;
					IMetaTileEntity meta = mt.getMetaTileEntity();
					if (meta != null && meta instanceof MTEMultiBlockBase) {
						MTEMultiBlockBase multi = (MTEMultiBlockBase) meta;

						if (multi.getStoredFluids().isEmpty()) {
							ArrayList<ItemStack> in = multi.getStoredInputs();
							in.removeIf(s -> s.stackSize <= 0);
							if (in.isEmpty()) {
								if(multi.mDualInputHatches.stream().map(s->s.getFirstNonEmptyInventory().orElse(null))
								.count()==0)
								inputon = false;
							
							}
						}

					}

				}
			}

			duality.redstoneOverride = on || delayon || inputon;

		} else {

			duality.redstoneOverride = false;
		}
*/
	};

	private int value;
/*
	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		//
		return duality.getTickingRequest(node);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
		if (first)
			return TickRateModulation.SAME;

		return duality.tickingRequest(node, TicksSinceLastCall);
	}*/
	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		
		return new TickingRequest(100, 100, false, false);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
		return TickRateModulation.SAME;
	}
	public String getCustomName() {
		if(duality.hasCustomName())return duality.getCustomName();
		return nameOverride();
	}
	private String nameOverride(){
		return "P2P - EU Interface";
	}
	public boolean hasCustomName() {
		  return duality.hasCustomName();
	}



public void setCustomName(String name) {
		duality.setCustomName(name);
	}


	public int getPriority() {
		return p;
	};

	int p;

	public void setPriority(int newValue) {
		p = newValue;
	};

	@Override
	public void destroy() {
		final ArrayList<ItemStack> drops = new ArrayList<>();

		for (String s : new String[] { "patterns", "upgrades" }) {
			IInventory inv = duality.getInventoryByName(s);
			for (int l = 0; l < inv.getSizeInventory(); l++) {
				final ItemStack is = inv.getStackInSlot(l);
				inv.setInventorySlotContents(l, null);
				if (is != null) {
					drops.add(is);
				}
			}
		}

		Platform.spawnDrops(pos.getWorld(), pos.x, pos.y, pos.z, drops);

		Data.super.destroy();
	}

	@Override
	public boolean firstUpdate() {
		if (first) {
			first = false;
			return true;
		}
		return false;
	}

	boolean first = true;

	@Override
	public boolean accept(ForgeDirection side, ICoverable aTileEntity,boolean b) {
		boolean ok=Data.super.accept(side, aTileEntity,b);
		this.duality.setPartHostInfo(ForgeDirection.UNKNOWN, fakehost, getTile());
		
		String s=tagName();
		if(s!=null&&!s.equals("")){
			duality.setCustomName(s);
			setTag(null);
		}	
		return ok;
	}

	public boolean click(EntityPlayer player) {
		final ItemStack is = player.inventory.getCurrentItem();

		if (is != null && is.getItem() instanceof IMemoryCard) {
			IMemoryCard mc = (IMemoryCard) is.getItem();
			if (ForgeEventFactory.onItemUseStart(player, is, 1) <= 0)
				return false;

			final NBTTagCompound data = mc.getData(is);

			final ItemStack newType = ItemStack.loadItemStackFromNBT(data);

			if (newType != null) {
				if (newType.getItem() instanceof IPartItem) {
					IPartItem partItem = (IPartItem) newType.getItem();
					final IPart testPart = partItem.createPartFromItemStack(newType);
					if (testPart != null && testPart instanceof PartEUP2PInterface) {
						// this.getHost().removePart(this.getSide(), true);
						// final ForgeDirection dir =
						// this.getHost().addPart(newType, this.getSide(),
						// player);
						// final IPart newBus = duality;

						/* if (newBus instanceof PartP2PTunnel<?>newTunnel) */ {

							ArrayList<ItemStack> drops = new ArrayList<>();
							for (int i = 0; i < duality.getPatterns().getSizeInventory(); i++) {
								if (duality.getPatterns().getStackInSlot(i) == null)
									continue;
								drops.add(duality.getPatterns().getStackInSlot(i));
								duality.getPatterns().setInventorySlotContents(i, null);
							}
							Platform.spawnDrops(pos.getWorld(), pos.x, pos.y, pos.z, drops);

							try {
								Field method;
								method = PartP2PTunnel.class.getDeclaredField("output");
								method.setAccessible(true);
								method.set(duality, true);
							} catch (Exception e1) {

								e1.printStackTrace();
							}

							// IConfigManager config =
							// duality.getConfigManager();

							try {
								duality.pasteMemoryCardData(duality, data);
							} catch (final GridAccessException e) {
								// :P
							}

							/*
							 * config.getSettings().forEach( setting ->
							 * duality.getConfigManager().putSetting(setting,
							 * config.getSetting(setting)));
							 */

							duality.onTunnelNetworkChange();
						}

						mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
						return true;
					}
				}
			}
			mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
		}
		return false;

	}

	@Override
	public boolean nonShiftClick(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity,
			EntityPlayer aPlayer) {

		// if(aPlayer.getHeldItem()!=null&&aPlayer.getHeldItem().getItem()!=null&&aPlayer.getHeldItem().getItem()
		// instanceof IMemoryCard){
		if (Optional.ofNullable(aPlayer.getHeldItem()).map(ItemStack::getItem).filter(S -> S instanceof IMemoryCard)
				.isPresent()) {
			if (click(aPlayer)) {
				return true;
			}
		}
		/*Optional.ofNullable(player.getHeldItem()).map(ItemStack::getItem)
		.orElse(null)==MyMod.eu_tool*/
		// if(!this.duality.isOutput())
	

		return false;
	}

	public long doOutput(long aVoltage, long aAmperage) {

		ICoverable cble = (ICoverable) this.pos.getWorld().getTileEntity(pos.x, pos.y, pos.z);

		if (cble.inputEnergyFrom(side))
			return cble.injectEnergyUnits(side, aVoltage, aAmperage);
		return 0;
	}

	public boolean getRedstone() {

		return duality.redstoneOverride;
	}

	private static final int startX = 10;
	private static final int startY = 25;
	private static final int spaceX = 18;
	private static final int spaceY = 18;
	public static final UITexture ICON = UITexture.fullImage("proghatches", "items/cover2");


	@Override
	public void addUIWidgets(Builder builder, CoverUIBuildContext ss) {

		
		
		
		if(ss.isAnotherWindow()&&
				 (hasAEGUI() && !ss.getPlayer().getEntityWorld().isRemote)
				 
				 ) {
					ss.getPlayer()
				.openGui(MyMod.instance, side.ordinal(), 	
						ss.getPlayer().getEntityWorld(), this.getPos().x,
						 this.getPos().y, this.getPos().z);}
		
		if (!tmpout)
			duality.addWidgets(builder, 16,ss);
		else
			duality.addWidgetsOut(builder, 16,ss);
		
		if(2>1)return;
		builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
		if (ss.isAnotherWindow() == false) {
			int rgb = ss.getGuiColorization();
			ss.addSyncedWindow(777, (s) -> createWindow(s, rgb));

			builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
				if (clickData.mouseButton == 0) {
					if (!widget.isClient())

						widget.getContext().openSyncedWindow(777);
				}
			}).setPlayClickSound(true).setBackground(GTUITextures.BUTTON_STANDARD, ICON)
					.addTooltips(ImmutableList
							.of(LangManager.translateToLocalFormatted("programmable_hatches.cover.ae.configure")))
					.setSize(16, 16).setPos(startX + spaceX * 6, startY + spaceY * 3));

		} // System.out.println("aaaaaaaaaaaaaa"+tmpout);
		/*
		 * if(duality.isOutput()) if(duality.getInput()!=null)
		 * duality.getInput().addWidgets(builder,16); else
		 * 
		 */
		/*if (!tmpout)
			duality.addWidgets(builder, 16);
		else
			duality.addWidgetsOut(builder, 16);
*/
		if (2 > 1)
			return;

		/*builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(true, true))
				.setGetter(() -> worksensitive ? 1 : 0).setSetter(s -> {
					worksensitive = s == 1;
				}).setLength(2).setTextureGetter(i -> {
					if (i == 1)
						return GTUITextures.OVERLAY_BUTTON_EXPORT;
					return GTUITextures.OVERLAY_BUTTON_IMPORT;
				})

				.addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.ae.worksensitive.false"))
				.addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.ae.worksensitive.true"))
				.setPos(startX, startY));

		builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(true, true))
				.setGetter(() -> delay ? 1 : 0).setSetter(s -> {
					delay = s == 1;
				}).setLength(2).setTextureGetter(i -> {
					if (i == 1)
						return GTUITextures.OVERLAY_BUTTON_EXPORT;
					return GTUITextures.OVERLAY_BUTTON_IMPORT;
				})

				.addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.ae.delay.false"))
				.addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.ae.delay.true"))
				.setPos(startX + spaceX, startY));

		builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(true, true))
				.setGetter(() -> inputdetect ? 1 : 0).setSetter(s -> {
					inputdetect = s == 1;
				}).setLength(2).setTextureGetter(i -> {
					if (i == 1)
						return GTUITextures.OVERLAY_BUTTON_EXPORT;
					return GTUITextures.OVERLAY_BUTTON_IMPORT;
				})

				.addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.ae.inputdetect.false"))
				.addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.ae.inputdetect.true"))
				.setPos(startX + spaceX + spaceX, startY));
*/
	}

	private ModularWindow createWindow(EntityPlayer ss, int rgb) {
		ModularWindow.Builder builder = ModularWindow.builder(176, 107);
		builder.setDraggable(false);
		builder.setGuiTint(rgb);
		builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
		builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(true, true))
				.setGetter(() -> worksensitive ? 1 : 0).setSetter(s -> {
					worksensitive = s == 1;
				}).setLength(2).setTextureGetter(i -> {
					if (i == 1)
						return GTUITextures.OVERLAY_BUTTON_EXPORT;
					return GTUITextures.OVERLAY_BUTTON_IMPORT;
				})

				.addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.ae.worksensitive.false"))
				.addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.ae.worksensitive.true"))
				.setPos(startX, startY));

		builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(true, true))
				.setGetter(() -> delay ? 1 : 0).setSetter(s -> {
					delay = s == 1;
				}).setLength(2).setTextureGetter(i -> {
					if (i == 1)
						return GTUITextures.OVERLAY_BUTTON_EXPORT;
					return GTUITextures.OVERLAY_BUTTON_IMPORT;
				})

				.addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.ae.delay.false"))
				.addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.ae.delay.true"))
				.setPos(startX + spaceX, startY));

		builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(true, true))
				.setGetter(() -> inputdetect ? 1 : 0).setSetter(s -> {
					inputdetect = s == 1;
				}).setLength(2).setTextureGetter(i -> {
					if (i == 1)
						return GTUITextures.OVERLAY_BUTTON_EXPORT;
					return GTUITextures.OVERLAY_BUTTON_IMPORT;
				})

				.addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.ae.inputdetect.false"))
				.addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.ae.inputdetect.true"))
				.setPos(startX + spaceX + spaceX, startY));

		return builder.build();
	}

	public boolean delay;
	public boolean inputdetect;
	public boolean worksensitive;

	@Override
	public ItemStack getVisual() {

		return new ItemStack(MyMod.cover, 1, 36);
	}

	@Override
	public TileEntity fakeTile() {
		return faketile;
	}

	public boolean requireChannel() {
		return false;
	}// internal node will require
	@Override
	public ForgeDirection getActualSide() {
		// TODO Auto-generated method stub
		return side;
	}
	public boolean hasModularGUI(){return true;}
}

package reobf.proghatches.main.mixin.mixins.part2;

import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.CommonMetaTileEntity;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;

@Mixin(value = BaseMetaTileEntity.class, remap = false)
public abstract class MixinOC /*extends CommonMetaTileEntity */implements Environment, SidedEnvironment {
private IMetaTileEntity getMetaTileEntity0(){
		
		BaseMetaTileEntity x=(BaseMetaTileEntity)(Object)this;
		return x.getMetaTileEntity();
	};
    @Override
    public Node sidedNode(ForgeDirection side) {
        IMetaTileEntity mte = getMetaTileEntity0();
        if (mte instanceof SidedEnvironment) {
            return ((SidedEnvironment) mte).sidedNode(side);
        }
        return null;
    }

    @Override
    public boolean canConnect(ForgeDirection side) {
        IMetaTileEntity mte = getMetaTileEntity0();
        if (mte instanceof SidedEnvironment) {
            return ((SidedEnvironment) mte).canConnect(side);
        }
        return false;
    }

    @Override
    public Node node() {

        IMetaTileEntity mte = getMetaTileEntity0();
        if (mte instanceof Environment) {
            return ((Environment) mte).node();
        }
        return null;
    }

    @Override
    public void onConnect(Node node) {
        IMetaTileEntity mte = getMetaTileEntity0();
        if (mte instanceof Environment) {
            ((Environment) mte).onConnect(node);
        }

    }

    @Override
    public void onDisconnect(Node node) {
        IMetaTileEntity mte = getMetaTileEntity0();
        if (mte instanceof Environment) {
            ((Environment) mte).onDisconnect(node);
        }
    }

    @Override
    public void onMessage(Message message) {
        IMetaTileEntity mte = getMetaTileEntity0();
        if (mte instanceof Environment) {
            ((Environment) mte).onMessage(message);
        }

    }

}

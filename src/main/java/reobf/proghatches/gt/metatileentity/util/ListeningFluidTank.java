package reobf.proghatches.gt.metatileentity.util;

import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class ListeningFluidTank extends FluidTank {

    public ListeningFluidTank(int capacity, IOnFillCallback thiz) {
        super(capacity);
        addListener(thiz::onFill);
    }

    public ListeningFluidTank(FluidStack stack, int capacity, IOnFillCallback thiz) {
        super(stack, capacity);
        addListener(thiz::onFill);
    }

    public ListeningFluidTank(Fluid fluid, int amount, int capacity, IOnFillCallback thiz) {
        super(fluid, amount, capacity);
        addListener(thiz::onFill);
    }

    Optional<Runnable> callback = Optional.empty();

    public ListeningFluidTank addListener(Runnable c) {
        if (callback.isPresent()) throw new RuntimeException("callback exists");
        callback = Optional.of(c);
        return this;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int filled = 0;
        try {
            return filled = super.fill(resource, doFill);
        } finally {
            if (filled > 0) callback.ifPresent(Runnable::run);
        }
    }

    public void onChange() {
        callback.ifPresent(Runnable::run);
    }

    @Override
    public void setFluid(FluidStack fluid) {

        super.setFluid(fluid);
        callback.ifPresent(Runnable::run);
    }

    public int fillDirect(FluidStack resource, boolean doFill) {

        return super.fill(resource, doFill);
    }

    public void setFluidDirect(FluidStack fluid) {

        super.setFluid(fluid);

    }

    @Override
    public FluidTank readFromNBT(NBTTagCompound nbt) {
        if (!nbt.hasKey("Empty")) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
            setFluidDirect(fluid);
        } else {
            setFluidDirect(null);
        }
        return this;
    }

}

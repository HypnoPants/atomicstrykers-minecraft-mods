package ic2.advancedmachines.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.EnergyNet;
import ic2.api.IElectricItem;
import ic2.api.IEnergySink;
import ic2.api.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityBaseMachine extends TileEntityMachine implements IEnergySink
{
    public int energy = 0;
    public int fuelslot;
    public int maxEnergy;
    public int maxInput;
    public int tier = 0;
    public boolean addedToEnergyNet = false;

    public TileEntityBaseMachine(int inventorySize, int maxEnergy, int maxInput)
    {
        super(inventorySize);
        this.fuelslot = 0;
        this.maxEnergy = maxEnergy;
        this.maxInput = maxInput;
        this.tier = 1;
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        this.energy = var1.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
        var1.setInteger("energy", this.energy);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (!this.addedToEnergyNet)
        {
            EnergyNet.getForWorld(this.worldObj).addTileEntity(this);
            this.addedToEnergyNet = true;
        }
    }

    @Override
    public void invalidate()
    {
        if (this.addedToEnergyNet)
        {
            EnergyNet.getForWorld(this.worldObj).removeTileEntity(this);
            this.addedToEnergyNet = false;
        }

        super.invalidate();
    }

    @Override
    public boolean isAddedToEnergyNet()
    {
        return this.addedToEnergyNet;
    }

    @Override
    public boolean demandsEnergy()
    {
        return this.energy <= this.maxEnergy - this.maxInput;
    }

    @Override
    public int injectEnergy(Direction var1, int var2)
    {
        if (var2 > this.maxInput)
        {
        	if (!AdvancedMachines.explodeMachineAt(worldObj, xCoord, yCoord, zCoord))
        	{
        		worldObj.createExplosion(null, xCoord, yCoord, zCoord, 2.0F, true);
        	}
        	invalidate();
            return 0;
        }
        else
        {
            this.energy += var2;
            int var3 = 0;
            if (this.energy > this.maxEnergy)
            {
                var3 = this.energy - this.maxEnergy;
                this.energy = this.maxEnergy;
            }

            return var3;
        }
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity var1, Direction var2)
    {
        return true;
    }

    public boolean isRedstonePowered()
    {
        return this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
    }
    
    protected boolean provideEnergy()
    {
        if (this.inventory[this.fuelslot] == null)
        {
            return false;
        }
        else
        {
            int var1 = this.inventory[this.fuelslot].itemID;
            if (Item.itemsList[var1] instanceof IElectricItem)
            {
                if (!((IElectricItem)Item.itemsList[var1]).canProvideEnergy())
                {
                    return false;
                }
                else
                {
                    int var2 = ElectricItem.discharge(this.inventory[this.fuelslot], this.maxEnergy - this.energy, this.tier, false, false);
                    this.energy += var2;
                    return var2 > 0;
                }
            }
            else if (var1 == Item.redstone.shiftedIndex)
            {
                this.energy += this.maxEnergy;
                --this.inventory[this.fuelslot].stackSize;
                if (this.inventory[this.fuelslot].stackSize <= 0)
                {
                    this.inventory[this.fuelslot] = null;
                }

                return true;
            }
            else if (var1 == Items.getItem("suBattery").itemID)
            {
                this.energy += 1000;
                --this.inventory[this.fuelslot].stackSize;
                if (this.inventory[this.fuelslot].stackSize <= 0)
                {
                    this.inventory[this.fuelslot] = null;
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }
}

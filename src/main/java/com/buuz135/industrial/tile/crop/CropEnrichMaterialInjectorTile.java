package com.buuz135.industrial.tile.crop;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.proxy.ItemRegistry;
import com.buuz135.industrial.tile.CustomColoredItemHandler;
import com.buuz135.industrial.tile.WorkingAreaElectricMachine;
import com.buuz135.industrial.tile.block.CustomOrientedBlock;
import com.buuz135.industrial.utils.BlockUtils;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.ItemStackHandler;
import net.ndrei.teslacorelib.containers.BasicTeslaContainer;
import net.ndrei.teslacorelib.containers.FilteredSlot;
import net.ndrei.teslacorelib.gui.BasicTeslaGuiContainer;
import net.ndrei.teslacorelib.gui.IGuiContainerPiece;
import net.ndrei.teslacorelib.gui.TiledRenderedGuiPiece;
import net.ndrei.teslacorelib.inventory.BoundingRectangle;
import net.ndrei.teslacorelib.inventory.ColoredItemHandler;

import java.util.List;

public class CropEnrichMaterialInjectorTile extends WorkingAreaElectricMachine {

    private static String NBT_POINTER = "pointer";

    private ItemStackHandler inFert;
    private int pointer;


    public CropEnrichMaterialInjectorTile() {
        super(CropEnrichMaterialInjectorTile.class.getName().hashCode(), 1, 0);
        pointer = 0;
    }

    @Override
    protected void initializeInventories() {
        super.initializeInventories();
        inFert = new ItemStackHandler(12);
        this.addInventory(new CustomColoredItemHandler(inFert, EnumDyeColor.GREEN, "Fertilizer input",18 * 5 + 3, 25,  4,  3) {
            @Override
            public boolean canInsertItem(int slot, ItemStack stack) {
                return (stack.getItem().equals(Items.DYE) && stack.getMetadata() == 15) || stack.getItem().equals(ItemRegistry.fertilizer);
            }

            @Override
            public boolean canExtractItem(int slot) {
                return false;
            }

        });
        this.addInventoryToStorage(inFert, "inFert");
    }

    @Override
    public AxisAlignedBB getWorkingArea() {
        BlockPos corner1 = new BlockPos(0, 0, 0).offset(this.getFacing().getOpposite(), getRadius() + 1);
        return this.getBlockType().getSelectedBoundingBox(this.world.getBlockState(this.pos), this.world, this.pos).offset(corner1).expand(getRadius(), 0, getRadius());
    }

    @Override
    protected float performWork() {
        if (((CustomOrientedBlock)this.getBlockType()).isWorkDisabled()) return 0;
        List<BlockPos> blockPos = BlockUtils.getBlockPosInAABB(getWorkingArea());
        ++pointer;
        if (pointer >= blockPos.size()) pointer = 0;
        if (pointer < blockPos.size()) {
            BlockPos pos = blockPos.get(pointer);
            if ((this.world.getBlockState(pos).getBlock() instanceof BlockCrops && this.world.getBlockState(pos).getValue(BlockCrops.AGE) < 7) || this.world.getBlockState(pos).getBlock().equals(Blocks.SAPLING)) {
                ItemStack stack = getFirstItem();
                if (!stack.isEmpty()) {
                    FakePlayer player = IndustrialForegoing.getFakePlayer(this.world);
                    ItemDye.applyBonemeal(stack, this.world, pos, player, EnumHand.MAIN_HAND);
                    return 1;
                }
            }
        } else {
            pointer = 0;
        }
        return 1;
    }

    private ItemStack getFirstItem() {
        for (int i = 0; i < inFert.getSlots(); ++i)
            if (!inFert.getStackInSlot(i).isEmpty()) return inFert.getStackInSlot(i);
        return ItemStack.EMPTY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound tagCompound = super.writeToNBT(compound);
        tagCompound.setInteger(NBT_POINTER, pointer);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (!compound.hasKey(NBT_POINTER)) pointer = 0;
        else pointer = compound.getInteger(NBT_POINTER);
    }

}

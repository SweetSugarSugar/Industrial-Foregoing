package com.buuz135.industrial.tile.misc;

import com.buuz135.industrial.proxy.BlockRegistry;
import com.buuz135.industrial.proxy.FluidsRegistry;
import com.buuz135.industrial.tile.CustomElectricMachine;
import com.buuz135.industrial.utils.ItemStackUtils;
import com.buuz135.industrial.utils.Reference;
import com.buuz135.industrial.utils.WorkUtils;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.ndrei.teslacorelib.gui.BasicRenderedGuiPiece;
import net.ndrei.teslacorelib.gui.BasicTeslaGuiContainer;
import net.ndrei.teslacorelib.gui.IGuiContainerPiece;
import net.ndrei.teslacorelib.inventory.BoundingRectangle;
import net.ndrei.teslacorelib.inventory.FluidTankType;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ResourcefulFurnaceTile extends CustomElectricMachine {

    public IFluidTank tank;
    public IItemHandler input;
    public IItemHandler output;

    public ResourcefulFurnaceTile() {
        super(ResourcefulFurnaceTile.class.getName().hashCode());
    }

    @Override
    protected void initializeInventories() {
        super.initializeInventories();
        input = addSimpleInventory(3, "input", EnumDyeColor.BLUE, "Input items", new BoundingRectangle(54, 25, 18, 18 * 3), (i, slot) -> !FurnaceRecipes.instance().getSmeltingResult(i).isEmpty(), (o, slot) -> false, false, null);
        output = addSimpleInventory(3, "Output", EnumDyeColor.ORANGE, "Output items", new BoundingRectangle(54 + 32 + 16, 25, 18, 18 * 3), (o, slot) -> false, (o, slot) -> true, false, null);
        tank = addSimpleFluidTank(8000, "Tank", EnumDyeColor.LIME, 128, 25, FluidTankType.OUTPUT, (o) -> false, (o) -> true);
    }

    @Override
    protected float performWork() {
        if (WorkUtils.isDisabled(this.getBlockType())) return 0;

        boolean operation = false;
        for (int i = 0; i < input.getSlots(); ++i) {
            if (input.getStackInSlot(i).isEmpty()) continue;
            if (output.getStackInSlot(i).isEmpty() || (FurnaceRecipes.instance().getSmeltingResult(input.getStackInSlot(i)).isItemEqual(output.getStackInSlot(i)) && output.getStackInSlot(i).getCount() < output.getStackInSlot(i).getMaxStackSize())) {
                output.insertItem(i, FurnaceRecipes.instance().getSmeltingResult(input.getStackInSlot(i)).copy(), false);
                input.getStackInSlot(i).shrink(1);
                tank.fill(new FluidStack(FluidsRegistry.ESSENCE, (int) (FurnaceRecipes.instance().getSmeltingExperience(output.getStackInSlot(i)) * BlockRegistry.resourcefulFurnaceBlock.getExperienceMultiplier())), true);
                operation = true;
            }
        }
        return operation ? 1 : 0;
    }

    @NotNull
    @Override
    public List<IGuiContainerPiece> getGuiContainerPieces(BasicTeslaGuiContainer<?> container) {
        List<IGuiContainerPiece> pieces = super.getGuiContainerPieces(container);
        pieces.add(new BasicRenderedGuiPiece(54 + 18 + 2, 26, 25, 18, new ResourceLocation(Reference.MOD_ID, "textures/gui/jei.png"), 24, 5));
        pieces.add(new BasicRenderedGuiPiece(54 + 18 + 2, 26 + 18, 25, 18, new ResourceLocation(Reference.MOD_ID, "textures/gui/jei.png"), 24, 5));
        pieces.add(new BasicRenderedGuiPiece(54 + 18 + 2, 26 + 18 * 2, 25, 18, new ResourceLocation(Reference.MOD_ID, "textures/gui/jei.png"), 24, 5));
        return pieces;
    }

    @Override
    protected boolean acceptsFluidItem(ItemStack stack) {
        return ItemStackUtils.acceptsFluidItem(stack);
    }

    @Override
    protected void processFluidItems(ItemStackHandler fluidItems) {
        ItemStackUtils.fillItemFromTank(fluidItems, tank);
    }
}

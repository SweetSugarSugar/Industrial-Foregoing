package com.buuz135.industrial.gui.conveyor;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.api.conveyor.ConveyorUpgrade;
import com.buuz135.industrial.api.conveyor.gui.IGuiComponent;
import com.buuz135.industrial.gui.component.FilterGuiComponent;
import com.buuz135.industrial.proxy.block.filter.IFilter;
import com.buuz135.industrial.proxy.network.ConveyorButtonInteractMessage;
import com.buuz135.industrial.utils.Reference;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiConveyor extends GuiContainer {

    public static final ResourceLocation BG_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/conveyor.png");

    private ConveyorUpgrade upgrade;
    private List<IGuiComponent> componentList;
    private int x;
    private int y;
    private List<IFilter.GhostSlot> ghostSlots;

    public GuiConveyor(Container inventorySlotsIn) {
        super(inventorySlotsIn);
        this.upgrade = getContainer().getConveyor().getUpgradeMap().get(getContainer().getFacing());
        this.componentList = new ArrayList<>();
        this.ghostSlots = new ArrayList<>();
    }

    @Override
    public void initGui() {
        super.initGui();
        componentList.clear();
        upgrade.addComponentsToGui(componentList);
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent instanceof FilterGuiComponent) {
                ghostSlots.addAll(Arrays.asList(((FilterGuiComponent) iGuiComponent).getFilter().getFilter()));
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(BG_TEXTURE);
        x = (width - xSize) / 2;
        y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        if (upgrade != null) {
            String localized = new TextComponentTranslation(String.format("conveyor.upgrade.%s.%s.name", upgrade.getFactory().getRegistryName().getResourceDomain(), upgrade.getFactory().getRegistryName().getResourcePath())).getFormattedText();
            fontRenderer.drawString(localized, x + xSize / 2 - fontRenderer.getStringWidth(localized) / 2, y + 6, 0x404040);
        }
        for (IGuiComponent iGuiComponent : componentList) {
            iGuiComponent.drawGuiBackgroundLayer(x, y, mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        x = (width - xSize) / 2;
        y = (height - ySize) / 2;
        for (IGuiComponent iGuiComponent : componentList) {
            iGuiComponent.drawGuiForegroundLayer(x, y, mouseX, mouseY);
        }
        renderHoveredToolTip(mouseX - x, mouseY - y);
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent.isInside(mouseX - x, mouseY - y)) {
                List<String> tooltips = iGuiComponent.getTooltip(x, y, mouseX, mouseY);
                if (tooltips != null) drawHoveringText(tooltips, mouseX - x, mouseY - y);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private ContainerConveyor getContainer() {
        return (ContainerConveyor) this.inventorySlots;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent.isInside(mouseX - x, mouseY - y)) iGuiComponent.handleClick(this, x, y, mouseX, mouseY);
        }
    }

    public void sendMessage(int id, NBTTagCompound compound) {
        IndustrialForegoing.NETWORK.sendToServer(new ConveyorButtonInteractMessage(upgrade.getPos(), id, upgrade.getSide(), compound));
    }

    public List<IFilter.GhostSlot> getGhostSlots() {
        return ghostSlots;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

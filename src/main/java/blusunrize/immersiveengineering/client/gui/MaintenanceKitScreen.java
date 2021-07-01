/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.gui.MaintenanceKitContainer;
import blusunrize.immersiveengineering.common.network.MessageMaintenanceKit;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class MaintenanceKitScreen extends ToolModificationScreen<MaintenanceKitContainer>
{
	public MaintenanceKitScreen(MaintenanceKitContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("maintenance_kit"));
		this.xSize = 195;
	}

	@Override
	protected void sendMessage(CompoundNBT data)
	{
		ImmersiveEngineering.packetHandler.sendToServer(new MessageMaintenanceKit(container.getEquipmentSlot(), data));
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		for(int i = 0; i < container.internalSlots; i++)
		{
			Slot s = container.getSlot(i);
			GuiHelper.drawSlot(guiLeft+s.xPos, guiTop+s.yPos, 16, 16, 0x44, transform);
		}
	}
}
/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.gui.CraftingTableContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class CraftingTableScreen extends IEContainerScreen<CraftingTableContainer>
{
	public CraftingTableScreen(CraftingTableContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("craftingtable"));
		this.ySize = 210;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY)
	{
		this.font.drawString(transform, container.tile.getDisplayName().getString(), 8, 6, 0x190b06);
	}
}
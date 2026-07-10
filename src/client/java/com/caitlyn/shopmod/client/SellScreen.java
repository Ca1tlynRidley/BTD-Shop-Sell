package com.caitlyn.shopmod.client;

import com.caitlyn.shopmod.SellScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SellScreen extends HandledScreen<SellScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier("minecraft", "textures/gui/container/generic_54.png");

    private static final int ROWS = 6;

    public SellScreen(SellScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 176;
        this.backgroundHeight = 114 + ROWS * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        int chestHeight = ROWS * 18 + 17;

        context.drawTexture(
                TEXTURE,
                x, y,
                0, 0,
                this.backgroundWidth, chestHeight,
                256, 256
        );

        context.drawTexture(
                TEXTURE,
                x, y + chestHeight,
                0, 126,
                this.backgroundWidth, 96,
                256, 256
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

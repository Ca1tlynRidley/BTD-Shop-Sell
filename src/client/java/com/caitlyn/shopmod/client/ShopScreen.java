package com.caitlyn.shopmod.client;

import com.caitlyn.shopmod.ShopScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ShopScreen extends HandledScreen<ShopScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier("minecraft", "textures/gui/container/generic_54.png");

    private static final int ROWS = 6;
    private static final int TOP_HEIGHT = 17 + ROWS * 18; // 125
    private static final int BOTTOM_BORDER_HEIGHT = 7;

    public ShopScreen(ShopScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 176;
        this.backgroundHeight = TOP_HEIGHT + BOTTOM_BORDER_HEIGHT; // 132

        this.playerInventoryTitleY = 10000; // hide inventory label
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Draw only the chest/shop slot area
        context.drawTexture(
                TEXTURE,
                x, y,
                0, 0,
                this.backgroundWidth, TOP_HEIGHT,
                256, 256
        );

        // Draw bottom border from the bottom of the vanilla texture
        context.drawTexture(
                TEXTURE,
                x, y + TOP_HEIGHT,
                0, 215,
                this.backgroundWidth, BOTTOM_BORDER_HEIGHT,
                256, 256
        );
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, 8, 6, 0x404040, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
package com.caitlyn.shopmod.client;

import com.caitlyn.shopmod.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ShopModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.SHOP_SCREEN_HANDLER, ShopScreen::new);
        HandledScreens.register(ModScreenHandlers.SELL_SCREEN_HANDLER, SellScreen::new);
    }
}

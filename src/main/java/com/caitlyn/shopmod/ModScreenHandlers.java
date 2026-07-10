package com.caitlyn.shopmod;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreenHandlers {
    public static ScreenHandlerType<ShopScreenHandler> SHOP_SCREEN_HANDLER;
    public static ScreenHandlerType<SellScreenHandler> SELL_SCREEN_HANDLER;

    public static void register() {
        SHOP_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                ShopMod.id("shop"),
                new ScreenHandlerType<>(ShopScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        SELL_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                ShopMod.id("sell"),
                new ScreenHandlerType<>(SellScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );
    }
}

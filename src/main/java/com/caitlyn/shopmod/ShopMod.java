package com.caitlyn.shopmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopMod implements ModInitializer {
    public static final String MOD_ID = "shop-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Shop Mod");

        ModScreenHandlers.register();
        ShopCommand.register();
        SellCommand.register();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}

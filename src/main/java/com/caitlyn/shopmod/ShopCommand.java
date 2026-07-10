package com.caitlyn.shopmod;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ShopCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommand(dispatcher);
        });
    }

    private static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("shop")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    openShop(player);
                    return 1;
                }));
    }

    private static void openShop(ServerPlayerEntity player) {
        player.openHandledScreen(new net.minecraft.screen.NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.literal("Shop");
            }

            @Override
            public ScreenHandler createMenu(
                    int syncId,
                    net.minecraft.entity.player.PlayerInventory inv,
                    net.minecraft.entity.player.PlayerEntity p
            ) {
                return new ShopScreenHandler(syncId, inv);
            }
        });
    }
}
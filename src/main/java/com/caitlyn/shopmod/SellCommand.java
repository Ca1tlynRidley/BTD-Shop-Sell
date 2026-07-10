package com.caitlyn.shopmod;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SellCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                registerCommand(dispatcher)
        );
    }

    private static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sell")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    openSellMenu(player);
                    return 1;
                }));
    }

    private static void openSellMenu(ServerPlayerEntity player) {
        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.literal("Sell");
            }

            @Override
            public ScreenHandler createMenu(
                    int syncId,
                    PlayerInventory inventory,
                    PlayerEntity playerEntity
            ) {
                return new SellScreenHandler(syncId, inventory);
            }
        });
    }
}

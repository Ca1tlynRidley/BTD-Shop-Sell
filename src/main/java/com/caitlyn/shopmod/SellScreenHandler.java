package com.caitlyn.shopmod;

import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import net.impactdev.impactor.api.economy.transactions.details.EconomyResultType;
import net.kyori.adventure.key.Key;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SellScreenHandler extends ScreenHandler {
    private static final Key CURRENCY_KEY = Key.key("impactor", "dollars");

    // Slots 0-52 accept items. Slot 53 is the lime Sell button.
    private static final int SELL_INPUT_SLOTS = 53;
    private static final int SELL_BUTTON_SLOT = 53;
    private static final int TOP_SLOT_COUNT = 54;

    private static final int PLAYER_INVENTORY_START = 54;
    private static final int PLAYER_INVENTORY_END = 90;

    private final SimpleInventory sellInventory = new SimpleInventory(SELL_INPUT_SLOTS);
    private final SimpleInventory buttonInventory = new SimpleInventory(1);

    public SellScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.SELL_SCREEN_HANDLER, syncId);

        // Double-chest area. The bottom-right position is reserved for Sell.
        for (int slot = 0; slot < SELL_INPUT_SLOTS; slot++) {
            int x = 8 + (slot % 9) * 18;
            int y = 18 + (slot / 9) * 18;
            this.addSlot(new Slot(sellInventory, slot, x, y));
        }

        buttonInventory.setStack(
                0,
                namedItem(Items.LIME_STAINED_GLASS, "Sell", Formatting.GREEN)
        );
        this.addSlot(new SellButtonSlot(buttonInventory, 0, 152, 108));

        // Player main inventory.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        139 + row * 18
                ));
            }
        }

        // Player hotbar.
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    197
            ));
        }
    }

    // ─── IMPACTOR ECONOMY ──────────────────────────────────────────────────

    private Currency getCurrency() {
        EconomyService service = EconomyService.instance();
        return service.currencies()
                .currency(CURRENCY_KEY)
                .orElse(service.currencies().primary());
    }

    private boolean deposit(PlayerEntity player, BigDecimal amount) {
        try {
            EconomyService service = EconomyService.instance();
            Currency currency = getCurrency();
            Account account = service.account(currency, player.getUuid()).join();

            EconomyTransaction transaction = account.deposit(amount);
            return transaction.result() == EconomyResultType.SUCCESS;
        } catch (Exception exception) {
            ShopMod.LOGGER.error(
                    "Failed to deposit sell money for " + player.getName().getString(),
                    exception
            );
            return false;
        }
    }

    // ─── SELLING ────────────────────────────────────────────────────────────

    private void sellEverything(PlayerEntity player) {
        List<Integer> sellableSlots = new ArrayList<>();

        long totalCents = 0;
        int soldItemCount = 0;
        int returnedItemCount = 0;

        // First calculate the transaction and return unsupported items.
        for (int slot = 0; slot < SELL_INPUT_SLOTS; slot++) {
            ItemStack stack = sellInventory.getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }

            Integer unitSellPrice = ShopItems.getSellPrice(stack.getItem());

            if (unitSellPrice == null) {
                ItemStack returned = sellInventory.removeStack(slot);
                returnedItemCount += returned.getCount();
                returnToPlayer(player, returned);
                continue;
            }

            sellableSlots.add(slot);
            soldItemCount += stack.getCount();
            totalCents += (long) unitSellPrice * stack.getCount();
        }

        if (sellableSlots.isEmpty()) {
            if (returnedItemCount > 0) {
                player.sendMessage(
                        Text.literal(
                                "Those items are not in /shop, so they were returned to you."
                        ).formatted(Formatting.YELLOW),
                        false
                );
            } else {
                player.sendMessage(
                        Text.literal("Put items into the sell chest first.")
                                .formatted(Formatting.YELLOW),
                        false
                );
            }

            syncInventories(player);
            return;
        }

        BigDecimal total = BigDecimal.valueOf(totalCents, 2);

        // Only delete sellable items after Impactor confirms the deposit.
        if (!deposit(player, total)) {
            player.sendMessage(
                    Text.literal("Sale failed. Your sellable items were not removed.")
                            .formatted(Formatting.RED),
                    false
            );
            syncInventories(player);
            return;
        }

        for (int slot : sellableSlots) {
            sellInventory.removeStack(slot);
        }

        String message = "Sold " + soldItemCount + " item"
                + (soldItemCount == 1 ? "" : "s")
                + " for " + ShopItems.formatMoney(total) + ".";

        if (returnedItemCount > 0) {
            message += " Returned " + returnedItemCount
                    + " unsupported item"
                    + (returnedItemCount == 1 ? "" : "s")
                    + ".";
        }

        player.sendMessage(
                Text.literal(message).formatted(Formatting.GREEN),
                false
        );

        syncInventories(player);
    }

    private void returnToPlayer(PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack remaining = stack.copy();
        player.getInventory().insertStack(remaining);

        if (!remaining.isEmpty()) {
            player.dropItem(remaining, false);
        }
    }

    private void syncInventories(PlayerEntity player) {
        sellInventory.markDirty();
        player.getInventory().markDirty();
        this.sendContentUpdates();

        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playerScreenHandler.sendContentUpdates();
            serverPlayer.currentScreenHandler.sendContentUpdates();
        }
    }

    // ─── CLICK / SHIFT-CLICK ────────────────────────────────────────────────

    @Override
    public void onSlotClick(
            int slotIndex,
            int button,
            SlotActionType actionType,
            PlayerEntity player
    ) {
        if (slotIndex == SELL_BUTTON_SLOT) {
            if (!player.getWorld().isClient) {
                sellEverything(player);
            }
            return;
        }

        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        if (slotIndex == SELL_BUTTON_SLOT) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack movingStack = slot.getStack();
        ItemStack originalStack = movingStack.copy();

        if (slotIndex < TOP_SLOT_COUNT) {
            if (!this.insertItem(
                    movingStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(movingStack, 0, SELL_INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (movingStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (movingStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, movingStack);
        return originalStack;
    }

    // Escape/close means cancel: every unsold item is returned.
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (!player.getWorld().isClient) {
            for (ItemStack stack : sellInventory.clearToList()) {
                returnToPlayer(player, stack);
            }

            syncInventories(player);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    private static ItemStack namedItem(Item item, String name, Formatting color) {
        ItemStack stack = new ItemStack(item);
        stack.setCustomName(Text.literal(name).formatted(color));
        return stack;
    }

    private static class SellButtonSlot extends Slot {
        public SellButtonSlot(
                SimpleInventory inventory,
                int index,
                int x,
                int y
        ) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return false;
        }
    }
}

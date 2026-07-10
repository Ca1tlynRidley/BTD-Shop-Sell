package com.caitlyn.shopmod;

import com.caitlyn.shopmod.ShopItems.Category;
import com.caitlyn.shopmod.ShopItems.ShopItem;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import net.impactdev.impactor.api.economy.transactions.details.EconomyResultType;
import net.kyori.adventure.key.Key;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopScreenHandler extends ScreenHandler {
    private final SimpleInventory shopInventory = new ShopInventory(54);
    private final Map<Integer, ShopItem> buyItems = new HashMap<>();

    private Page currentPage = Page.MAIN;
    private Category currentCategory = null;
    private int categoryPage = 0;

    private ShopItem selectedItem = null;
    private int selectedAmount = 1;
    private boolean stackMode = false;

    // The Impactor currency key defined in economy.conf
    private static final Key CURRENCY_KEY = Key.key("impactor", "dollars");

    private static final int SLOT_INGREDIENTS  = 20;
    private static final int SLOT_BLOCKS       = 21;
    private static final int SLOT_COMBAT       = 22;
    private static final int SLOT_FOOD         = 23;
    private static final int SLOT_REDSTONE     = 24;
    private static final int SLOT_FUNCTIONAL   = 31;

    private static final int SLOT_BACK_OR_BALANCE = 45;
    private static final int SLOT_PREVIOUS_PAGE   = 48;
    private static final int SLOT_PAGE_INFO        = 49;
    private static final int SLOT_NEXT_PAGE        = 50;
    private static final int SLOT_CLOSE            = 53;

    private static final int SLOT_MINUS_64     = 19;
    private static final int SLOT_MINUS_32     = 20;
    private static final int SLOT_MINUS_1      = 21;
    private static final int SLOT_SELECTED_ITEM = 22;
    private static final int SLOT_PLUS_1       = 23;
    private static final int SLOT_PLUS_32      = 24;
    private static final int SLOT_PLUS_64      = 25;
    private static final int SLOT_BUY          = 31;
    private static final int SLOT_STACK_MODE   = 40;

    private static final int ITEMS_PER_PAGE        = 45;
    private static final int MAX_NORMAL_BUY_AMOUNT = 4096;
    private static final int MAX_STACK_BUY_AMOUNT  = 64;

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.SHOP_SCREEN_HANDLER, syncId);

        for (int i = 0; i < 54; i++) {
            this.addSlot(new ShopDisplaySlot(shopInventory, i, 8 + (i % 9) * 18, 18 + (i / 9) * 18));
        }

        addHiddenPlayerInventory(playerInventory);
        showMainMenu(playerInventory.player);
    }

    private void addHiddenPlayerInventory(PlayerInventory playerInventory) {
        int hiddenX = -10000;
        int hiddenY = -10000;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, hiddenX, hiddenY));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, hiddenX, hiddenY));
        }
    }

    // ─── IMPACTOR ECONOMY HELPERS ───────────────────────────────────────────

    private Currency getCurrency() {
        EconomyService service = EconomyService.instance();
        return service.currencies()
                .currency(CURRENCY_KEY)
                .orElse(service.currencies().primary());
    }

    private BigDecimal getBalance(PlayerEntity player) {
        try {
            EconomyService service = EconomyService.instance();
            Currency currency = getCurrency();
            Account account = service.account(currency, player.getUuid()).join();
            return account.balance();
        } catch (Exception e) {
            ShopMod.LOGGER.error("Failed to get balance for " + player.getName().getString(), e);
            return BigDecimal.ZERO;
        }
    }

    private boolean withdraw(PlayerEntity player, BigDecimal amount) {
        try {
            EconomyService service = EconomyService.instance();
            Currency currency = getCurrency();
            Account account = service.account(currency, player.getUuid()).join();
            EconomyTransaction result = account.withdraw(amount);
            return result.result() == EconomyResultType.SUCCESS;
        } catch (Exception e) {
            ShopMod.LOGGER.error("Failed to withdraw from " + player.getName().getString(), e);
            return false;
        }
    }

    // ─── MENU BUILDERS ──────────────────────────────────────────────────────

    private void showMainMenu(PlayerEntity player) {
        currentPage = Page.MAIN;
        currentCategory = null;
        categoryPage = 0;
        selectedItem = null;
        selectedAmount = 1;
        stackMode = false;

        clearMenu();

        shopInventory.setStack(SLOT_INGREDIENTS, namedItem(Items.IRON_INGOT,       "Ingredients", Formatting.WHITE));
        shopInventory.setStack(SLOT_BLOCKS,      namedItem(Items.OXIDIZED_COPPER,     "Blocks",      Formatting.AQUA));
        shopInventory.setStack(SLOT_COMBAT,      namedItem(Items.GOLDEN_SWORD,     "Combat",      Formatting.GOLD));
        shopInventory.setStack(SLOT_FOOD,        namedItem(Items.APPLE,            "Food",        Formatting.RED));
        shopInventory.setStack(SLOT_REDSTONE,    namedItem(Items.REDSTONE,         "Redstone",    Formatting.DARK_RED));
        shopInventory.setStack(SLOT_FUNCTIONAL,  namedItem(Items.OAK_HANGING_SIGN, "Functional",  Formatting.YELLOW));

        BigDecimal balance = getBalance(player);
        shopInventory.setStack(
                SLOT_BACK_OR_BALANCE,
                playerHead(
                        player,
                        "Balance: " + ShopItems.formatMoney(balance),
                        Formatting.GREEN
                )
        );
        shopInventory.setStack(SLOT_CLOSE, namedItem(Items.BARRIER, "Close", Formatting.RED));

        sendContentUpdates();
    }

    private void showCategoryMenu(PlayerEntity player, Category category, int page) {
        currentPage = Page.CATEGORY;
        currentCategory = category;
        selectedItem = null;
        selectedAmount = 1;
        stackMode = false;

        clearMenu();

        List<ShopItem> items = ShopItems.get(category);
        int maxPage = Math.max(0, (items.size() - 1) / ITEMS_PER_PAGE);

        if (page < 0) page = 0;
        if (page > maxPage) page = maxPage;

        categoryPage = page;

        int start = categoryPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());

        for (int i = start; i < end; i++) {
            addBuyItem(i - start, items.get(i));
        }

        shopInventory.setStack(SLOT_BACK_OR_BALANCE, namedItem(Items.ARROW, "Back", Formatting.YELLOW));
        shopInventory.setStack(SLOT_PAGE_INFO, namedItem(
                Items.PAPER,
                ShopItems.displayName(category) + " Page " + (categoryPage + 1) + "/" + (maxPage + 1),
                Formatting.WHITE
        ));

        if (categoryPage > 0) {
            shopInventory.setStack(SLOT_PREVIOUS_PAGE, namedItem(Items.ARROW, "Previous Page", Formatting.YELLOW));
        }
        if (categoryPage < maxPage) {
            shopInventory.setStack(SLOT_NEXT_PAGE, namedItem(Items.ARROW, "Next Page", Formatting.GREEN));
        }

        shopInventory.setStack(SLOT_CLOSE, namedItem(Items.BARRIER, "Close", Formatting.RED));
        sendContentUpdates();
    }

    private void showPurchaseMenu(PlayerEntity player, ShopItem item) {
        currentPage = Page.PURCHASE;
        selectedItem = item;

        clearMenu();

        BigDecimal total = getTotalPrice();

        if (!stackMode) {
            selectedAmount = Math.max(1, Math.min(selectedAmount, MAX_NORMAL_BUY_AMOUNT));

            shopInventory.setStack(SLOT_MINUS_64, namedItem(Items.RED_STAINED_GLASS_PANE, "-64", Formatting.RED));
            shopInventory.setStack(SLOT_MINUS_32, namedItem(Items.RED_STAINED_GLASS_PANE, "-32", Formatting.RED));
            shopInventory.setStack(SLOT_MINUS_1,  namedItem(Items.RED_STAINED_GLASS_PANE, "-1",  Formatting.RED));
            shopInventory.setStack(SLOT_PLUS_1,   namedItem(Items.LIME_STAINED_GLASS_PANE, "+1",  Formatting.GREEN));
            shopInventory.setStack(SLOT_PLUS_32,  namedItem(Items.LIME_STAINED_GLASS_PANE, "+32", Formatting.GREEN));
            shopInventory.setStack(SLOT_PLUS_64,  namedItem(Items.LIME_STAINED_GLASS_PANE, "+64", Formatting.GREEN));

            ItemStack displayItem = namedItem(
                    item.item,
                    item.name + " x" + selectedAmount + " | " + ShopItems.formatMoney(total),
                    Formatting.WHITE
            );
            displayItem.setCount(selectedAmount);
            shopInventory.setStack(SLOT_SELECTED_ITEM, displayItem);
            shopInventory.setStack(SLOT_STACK_MODE, namedItem(Items.LIME_STAINED_GLASS, "Stack Mode", Formatting.GREEN));
        } else {
            selectedAmount = Math.max(1, Math.min(selectedAmount, MAX_STACK_BUY_AMOUNT));

            shopInventory.setStack(SLOT_MINUS_64, namedItem(Items.RED_STAINED_GLASS, "-64 stacks", Formatting.RED));
            shopInventory.setStack(SLOT_MINUS_32, namedItem(Items.RED_STAINED_GLASS, "-32 stacks", Formatting.RED));
            shopInventory.setStack(SLOT_MINUS_1,  namedItem(Items.RED_STAINED_GLASS, "-1 stack",   Formatting.RED));
            shopInventory.setStack(SLOT_PLUS_1,   namedItem(Items.LIME_STAINED_GLASS, "+1 stack",   Formatting.GREEN));
            shopInventory.setStack(SLOT_PLUS_32,  namedItem(Items.LIME_STAINED_GLASS, "+32 stacks", Formatting.GREEN));
            shopInventory.setStack(SLOT_PLUS_64,  namedItem(Items.LIME_STAINED_GLASS, "+64 stacks", Formatting.GREEN));

            ItemStack displayItem = namedItem(
                    item.item,
                    item.name + " x" + selectedAmount + " stacks | " + ShopItems.formatMoney(total),
                    Formatting.WHITE
            );
            displayItem.setCount(selectedAmount);
            shopInventory.setStack(SLOT_SELECTED_ITEM, displayItem);
            shopInventory.setStack(SLOT_STACK_MODE, namedItem(Items.LIME_STAINED_GLASS, "Stack Mode: ON", Formatting.GREEN));
        }

        shopInventory.setStack(SLOT_BUY, namedItem(Items.EMERALD,
                "Buy | Total: " + ShopItems.formatMoney(total), Formatting.GREEN));
        shopInventory.setStack(SLOT_BACK_OR_BALANCE, namedItem(Items.ARROW, "Back", Formatting.YELLOW));
        shopInventory.setStack(SLOT_CLOSE, namedItem(Items.BARRIER, "Close", Formatting.RED));

        sendContentUpdates();
    }

    private void addBuyItem(int slot, ShopItem shopItem) {
        ItemStack display = namedItem(shopItem.item,
                shopItem.name + " - " + ShopItems.formatMoney(shopItem.price), Formatting.WHITE);
        shopInventory.setStack(slot, display);
        buyItems.put(slot, shopItem);
    }

    private void clearMenu() {
        shopInventory.clear();
        buyItems.clear();
    }

    private ItemStack namedItem(Item item, String name, Formatting color) {
        ItemStack stack = new ItemStack(item);
        stack.setCustomName(Text.literal(name).formatted(color));
        return stack;
    }

    private ItemStack playerHead(PlayerEntity player, String name, Formatting color) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        NbtCompound skullOwner = new NbtCompound();
        NbtHelper.writeGameProfile(skullOwner, player.getGameProfile());

        stack.getOrCreateNbt().put("SkullOwner", skullOwner);
        stack.setCustomName(Text.literal(name).formatted(color));

        return stack;
    }

    // ─── CLICK HANDLING ─────────────────────────────────────────────────────

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < 0 || slotIndex >= 54) return;
        if (player.getWorld().isClient) return;

        if (slotIndex == SLOT_CLOSE) {
            if (player instanceof ServerPlayerEntity sp) sp.closeHandledScreen();
            return;
        }

        switch (currentPage) {
            case MAIN     -> handleMainMenuClick(slotIndex, player);
            case CATEGORY -> handleCategoryClick(slotIndex, player);
            case PURCHASE -> handlePurchaseClick(slotIndex, player);
        }
    }

    private void handleMainMenuClick(int slotIndex, PlayerEntity player) {
        if (slotIndex == SLOT_INGREDIENTS) { showCategoryMenu(player, Category.INGREDIENTS, 0); return; }
        if (slotIndex == SLOT_BLOCKS)      { showCategoryMenu(player, Category.BLOCKS,      0); return; }
        if (slotIndex == SLOT_COMBAT)      { showCategoryMenu(player, Category.COMBAT,      0); return; }
        if (slotIndex == SLOT_FOOD)        { showCategoryMenu(player, Category.FOOD,        0); return; }
        if (slotIndex == SLOT_REDSTONE)    { showCategoryMenu(player, Category.REDSTONE,    0); return; }
        if (slotIndex == SLOT_FUNCTIONAL)  { showCategoryMenu(player, Category.FUNCTIONAL,  0); }
    }

    private void handleCategoryClick(int slotIndex, PlayerEntity player) {
        if (slotIndex == SLOT_BACK_OR_BALANCE) { showMainMenu(player); return; }

        if (slotIndex == SLOT_PREVIOUS_PAGE && currentCategory != null) {
            showCategoryMenu(player, currentCategory, categoryPage - 1);
            return;
        }
        if (slotIndex == SLOT_NEXT_PAGE && currentCategory != null) {
            showCategoryMenu(player, currentCategory, categoryPage + 1);
            return;
        }

        ShopItem item = buyItems.get(slotIndex);
        if (item != null) {
            selectedAmount = 1;
            stackMode = false;
            showPurchaseMenu(player, item);
        }
    }

    private void handlePurchaseClick(int slotIndex, PlayerEntity player) {
        if (selectedItem == null) {
            if (currentCategory != null) showCategoryMenu(player, currentCategory, categoryPage);
            else showMainMenu(player);
            return;
        }

        if (slotIndex == SLOT_BACK_OR_BALANCE) {
            if (currentCategory != null) showCategoryMenu(player, currentCategory, categoryPage);
            else showMainMenu(player);
            return;
        }

        if (slotIndex == SLOT_STACK_MODE) {
            stackMode = !stackMode;
            selectedAmount = 1;
            showPurchaseMenu(player, selectedItem);
            return;
        }

        if (slotIndex == SLOT_MINUS_64) { changeAmount(-64); showPurchaseMenu(player, selectedItem); return; }
        if (slotIndex == SLOT_MINUS_32) { changeAmount(-32); showPurchaseMenu(player, selectedItem); return; }
        if (slotIndex == SLOT_MINUS_1)  { changeAmount(-1);  showPurchaseMenu(player, selectedItem); return; }
        if (slotIndex == SLOT_PLUS_1)   { setAmount(1);      showPurchaseMenu(player, selectedItem); return; }
        if (slotIndex == SLOT_PLUS_32)  { setAmount(32);     showPurchaseMenu(player, selectedItem); return; }
        if (slotIndex == SLOT_PLUS_64)  { setAmount(64);     showPurchaseMenu(player, selectedItem); return; }

        if (slotIndex == SLOT_BUY) buySelectedItem(player);
    }

    // ─── AMOUNT HELPERS ─────────────────────────────────────────────────────

    private void changeAmount(int change) {
        selectedAmount += change;
        if (selectedAmount < 1) selectedAmount = 1;
        int max = stackMode ? MAX_STACK_BUY_AMOUNT : MAX_NORMAL_BUY_AMOUNT;
        if (selectedAmount > max) selectedAmount = max;
    }

    private void setAmount(int amount) {
        selectedAmount = Math.max(1, amount);
        int max = stackMode ? MAX_STACK_BUY_AMOUNT : MAX_NORMAL_BUY_AMOUNT;
        if (selectedAmount > max) selectedAmount = max;
    }

    private int getTotalItems() {
        if (selectedItem == null) return 0;
        if (!stackMode) return selectedAmount;
        return selectedAmount * new ItemStack(selectedItem.item).getMaxCount();
    }

    private BigDecimal getTotalPrice() {
        if (selectedItem == null) return BigDecimal.ZERO;
        // selectedItem.price is stored as an int in cents; convert to a dollar
        // BigDecimal before doing money math against the Impactor economy.
        BigDecimal unitPrice = BigDecimal.valueOf(selectedItem.price, 2);
        return unitPrice.multiply(BigDecimal.valueOf(getTotalItems()));
    }

    // ─── BUY LOGIC ──────────────────────────────────────────────────────────

    private void buySelectedItem(PlayerEntity player) {
        if (selectedItem == null) return;

        int totalItems    = getTotalItems();
        BigDecimal total  = getTotalPrice();
        BigDecimal balance = getBalance(player);

        if (balance.compareTo(total) < 0) {
            player.sendMessage(Text.literal(
                    "Not enough money. Need " + ShopItems.formatMoney(total) + "."
            ).formatted(Formatting.RED), false);
            return;
        }

        boolean success = withdraw(player, total);

        if (!success) {
            player.sendMessage(Text.literal(
                    "Transaction failed. Please try again."
            ).formatted(Formatting.RED), false);
            return;
        }

        giveItems(player, selectedItem.item, totalItems);

        player.sendMessage(Text.literal(
                "Bought " + selectedItem.name + " x" + totalItems + " for " + ShopItems.formatMoney(total) + "."
        ).formatted(Formatting.GREEN), false);

        showPurchaseMenu(player, selectedItem);
    }

    private void giveItems(PlayerEntity player, Item item, int amount) {
        int remaining   = amount;
        int maxStack    = new ItemStack(item).getMaxCount();

        while (remaining > 0) {
            int give = Math.min(remaining, maxStack);
            ItemStack stack = new ItemStack(item, give);

            boolean inserted = player.getInventory().insertStack(stack);
            if (!inserted || !stack.isEmpty()) {
                player.dropItem(stack, false);
            }

            remaining -= give;
        }

        player.getInventory().markDirty();

        if (player instanceof ServerPlayerEntity sp) {
            sp.playerScreenHandler.sendContentUpdates();
            sp.currentScreenHandler.sendContentUpdates();
        }
    }

    // ─── BOILERPLATE ────────────────────────────────────────────────────────

    @Override
    public boolean canUse(PlayerEntity player) { return true; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }

    private enum Page { MAIN, CATEGORY, PURCHASE }

    private static class ShopInventory extends SimpleInventory {
        public ShopInventory(int size) { super(size); }

        @Override
        public int getMaxCountPerStack() { return MAX_NORMAL_BUY_AMOUNT; }
    }

    private static class ShopDisplaySlot extends Slot {
        public ShopDisplaySlot(SimpleInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override public boolean canInsert(ItemStack stack) { return false; }
        @Override public boolean canTakeItems(PlayerEntity player) { return false; }
        @Override public int getMaxItemCount() { return MAX_NORMAL_BUY_AMOUNT; }
    }
}
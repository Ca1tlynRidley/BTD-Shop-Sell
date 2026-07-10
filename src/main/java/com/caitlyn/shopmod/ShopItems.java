package com.caitlyn.shopmod;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ShopItems {
    private ShopItems() {}

    public enum Category {
        INGREDIENTS,
        BLOCKS,
        COMBAT,
        FOOD,
        REDSTONE,
        FUNCTIONAL
    }

    public static class ShopItem {
        public final Item item;
        public final String name;
        public final int price;

        public ShopItem(Item item, String name, int price) {
            this.item = item;
            this.name = name;
            this.price = price;
        }
    }

    // Sell foundation (per 1 item):
    // Copper  = $2.50
    // Iron    = $5.00
    // Gold    = $5.00
    // Diamond = $10.00
    // Emerald = $5.00
    //
    // Buy prices are roughly 2-3x the sell value.
    // Prices stored as cents. price(12.50) = $12.50

    private static int price(double dollars) {
        return (int) Math.round(dollars * 100.0);
    }

    public static String formatMoney(int cents) {
        if (cents % 100 == 0) {
            return "$" + (cents / 100);
        } else if (cents % 10 == 0) {
            return "$" + String.format("%.1f", cents / 100.0);
        } else {
            return "$" + String.format("%.2f", cents / 100.0);
        }
    }

    // Overload for formatting Impactor's BigDecimal (dollars) balances/totals,
    // distinct from ShopItem.price which is stored as an int in cents.
    public static String formatMoney(BigDecimal dollars) {
        int cents = dollars.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        return formatMoney(cents);
    }

    public static String displayName(Category category) {
        return switch (category) {
            case INGREDIENTS -> "Ingredients";
            case BLOCKS -> "Blocks";
            case COMBAT -> "Combat";
            case FOOD -> "Food";
            case REDSTONE -> "Redstone";
            case FUNCTIONAL -> "Functional";
        };
    }

    public static List<ShopItem> get(Category category) {
        return switch (category) {
            case INGREDIENTS -> ingredients();
            case BLOCKS -> blocks();
            case COMBAT -> combat();
            case FOOD -> food();
            case REDSTONE -> redstone();
            case FUNCTIONAL -> functional();
        };
    }

    // ─────────────────────────────────────────────
    //  INGREDIENTS
    // ─────────────────────────────────────────────
    // Sell foundation used as anchor:
    //   copper  sell=$2.50  → buy ~$6.00  (2.4x)
    //   iron    sell=$5.00  → buy ~$12.00 (2.4x)
    //   gold    sell=$5.00  → buy ~$12.00 (2.4x)
    //   diamond sell=$10.00 → buy ~$25.00 (2.5x)
    //   emerald sell=$5.00  → buy ~$12.00 (2.4x)
    // Everything else is priced relative to iron as the baseline.

    private static List<ShopItem> ingredients() {
        List<ShopItem> list = new ArrayList<>();

        add(list, Items.COAL,         "Coal",          price(2.00));   // cheap fuel
        add(list, Items.CHARCOAL,     "Charcoal",      price(2.00));
        add(list, Items.COPPER_INGOT, "Copper Ingot",  price(6.00));   // sell=$2.50 → buy=$6.00
        add(list, Items.IRON_INGOT,   "Iron Ingot",    price(12.00));  // sell=$5.00 → buy=$12.00
        add(list, Items.GOLD_INGOT,   "Gold Ingot",    price(12.00));  // sell=$5.00 → buy=$12.00
        add(list, Items.DIAMOND,      "Diamond",       price(25.00));  // sell=$10.00 → buy=$25.00
        add(list, Items.EMERALD,      "Emerald",       price(12.00));  // sell=$5.00 → buy=$12.00
        add(list, Items.LAPIS_LAZULI, "Lapis Lazuli",  price(4.00));
        add(list, Items.QUARTZ,       "Nether Quartz", price(3.00));
        add(list, Items.STICK,        "Stick",         price(0.50));
        add(list, Items.FLINT,        "Flint",         price(1.50));
        add(list, Items.BONE,         "Bone",          price(2.00));
        add(list, Items.STRING,       "String",        price(2.00));
        add(list, Items.GUNPOWDER,    "Gunpowder",     price(5.00));
        add(list, Items.BLAZE_ROD,    "Blaze Rod",     price(20.00));  // nether grind
        add(list, Items.SLIME_BALL,   "Slimeball",     price(6.00));
        add(list, Items.ENDER_PEARL,  "Ender Pearl",   price(10.00));
        add(list, Items.NETHER_WART,  "Nether Wart",   price(2.00));

        // Dyes — all equal, cheap
        add(list, Items.WHITE_DYE,      "White Dye",      price(1.00));
        add(list, Items.LIGHT_GRAY_DYE, "Light Gray Dye", price(1.00));
        add(list, Items.GRAY_DYE,       "Gray Dye",       price(1.00));
        add(list, Items.BLACK_DYE,      "Black Dye",      price(1.00));
        add(list, Items.BROWN_DYE,      "Brown Dye",      price(1.00));
        add(list, Items.RED_DYE,        "Red Dye",        price(1.00));
        add(list, Items.ORANGE_DYE,     "Orange Dye",     price(1.00));
        add(list, Items.YELLOW_DYE,     "Yellow Dye",     price(1.00));
        add(list, Items.LIME_DYE,       "Lime Dye",       price(1.00));
        add(list, Items.GREEN_DYE,      "Green Dye",      price(1.00));
        add(list, Items.CYAN_DYE,       "Cyan Dye",       price(1.00));
        add(list, Items.LIGHT_BLUE_DYE, "Light Blue Dye", price(1.00));
        add(list, Items.BLUE_DYE,       "Blue Dye",       price(1.00));
        add(list, Items.PURPLE_DYE,     "Purple Dye",     price(1.00));
        add(list, Items.MAGENTA_DYE,    "Magenta Dye",    price(1.00));
        add(list, Items.PINK_DYE,       "Pink Dye",       price(1.00));

        add(list, Items.BOOK,         "Book",         price(5.00));
        add(list, Items.GLASS_BOTTLE, "Glass Bottle", price(0.50));

        return list;
    }

    // ─────────────────────────────────────────────
    //  COMBAT
    // ─────────────────────────────────────────────
    // Weapons/tools priced by material tier.
    // Iron = 3x iron ingot cost ($12 × 2 or 3 ingots = ~$30-40)
    // Gold = cheaper than iron (weak but fast)
    // Armor pieces scale by how many ingots they'd need + markup.

    private static List<ShopItem> combat() {
        List<ShopItem> list = new ArrayList<>();

        // Swords
        addByPath(list, "wooden_sword",  price(5.00));
        addByPath(list, "stone_sword",   price(10.00));
        addByPath(list, "golden_sword",  price(18.00));
        addByPath(list, "iron_sword",    price(35.00));  // ~3 iron ingots + markup

        // Pickaxes
        addByPath(list, "wooden_pickaxe",  price(6.00));
        addByPath(list, "stone_pickaxe",   price(12.00));
        addByPath(list, "golden_pickaxe",  price(22.00));
        addByPath(list, "iron_pickaxe",    price(45.00));

        // Axes
        addByPath(list, "wooden_axe",  price(6.00));
        addByPath(list, "stone_axe",   price(12.00));
        addByPath(list, "golden_axe",  price(20.00));
        addByPath(list, "iron_axe",    price(40.00));

        // Shovels
        addByPath(list, "wooden_shovel",  price(3.00));
        addByPath(list, "stone_shovel",   price(6.00));
        addByPath(list, "golden_shovel",  price(12.00));
        addByPath(list, "iron_shovel",    price(20.00));

        // Hoes
        addByPath(list, "wooden_hoe",  price(3.00));
        addByPath(list, "stone_hoe",   price(6.00));
        addByPath(list, "golden_hoe",  price(12.00));
        addByPath(list, "iron_hoe",    price(20.00));

        // Leather Armor — cheapest tier
        addByPath(list, "leather_helmet",     price(8.00));
        addByPath(list, "leather_chestplate", price(12.00));
        addByPath(list, "leather_leggings",   price(10.00));
        addByPath(list, "leather_boots",      price(8.00));

        // Chainmail Armor — mid tier, hard to obtain normally
        addByPath(list, "chainmail_helmet",     price(30.00));
        addByPath(list, "chainmail_chestplate", price(45.00));
        addByPath(list, "chainmail_leggings",   price(40.00));
        addByPath(list, "chainmail_boots",      price(30.00));

        // Golden Armor — weak but enchantable, cheaper than iron
        addByPath(list, "golden_helmet",     price(20.00));
        addByPath(list, "golden_chestplate", price(30.00));
        addByPath(list, "golden_leggings",   price(25.00));
        addByPath(list, "golden_boots",      price(20.00));

        // Iron Armor — 5-8 ingots per piece × $12 + markup
        addByPath(list, "iron_helmet",     price(70.00));
        addByPath(list, "iron_chestplate", price(100.00));
        addByPath(list, "iron_leggings",   price(90.00));
        addByPath(list, "iron_boots",      price(70.00));

        // Utility / Combat Items
        add(list, Items.BUCKET,        "Bucket",        price(30.00));
        add(list, Items.NAME_TAG,      "Name Tag",      price(150.00));
        add(list, Items.SADDLE,        "Saddle",        price(200.00));
        add(list, Items.WRITABLE_BOOK, "Book and Quill",price(15.00));
        add(list, Items.TOTEM_OF_UNDYING, "Totem of Undying", price(1000.00));
        add(list, Items.BOW,      "Bow",      price(40.00));
        add(list, Items.CROSSBOW, "Crossbow", price(70.00));
        add(list, Items.SHIELD,   "Shield",   price(60.00));

        // Boats
        addByPath(list, "oak_boat",       price(10.00));
        addByPath(list, "spruce_boat",    price(10.00));
        addByPath(list, "birch_boat",     price(10.00));
        addByPath(list, "jungle_boat",    price(10.00));
        addByPath(list, "acacia_boat",    price(10.00));
        addByPath(list, "dark_oak_boat",  price(10.00));
        addByPath(list, "mangrove_boat",  price(12.00));
        addByPath(list, "cherry_boat",    price(12.00));
        addByPath(list, "bamboo_raft",    price(10.00));

        // Chest Boats
        addByPath(list, "oak_chest_boat",       price(25.00));
        addByPath(list, "spruce_chest_boat",    price(25.00));
        addByPath(list, "birch_chest_boat",     price(25.00));
        addByPath(list, "jungle_chest_boat",    price(25.00));
        addByPath(list, "acacia_chest_boat",    price(25.00));
        addByPath(list, "dark_oak_chest_boat",  price(25.00));
        addByPath(list, "mangrove_chest_boat",  price(28.00));
        addByPath(list, "cherry_chest_boat",    price(28.00));
        addByPath(list, "bamboo_chest_raft",    price(25.00));

        // Rails
        add(list, Items.RAIL,          "Rail",          price(4.00));
        add(list, Items.POWERED_RAIL,  "Powered Rail",  price(20.00));
        add(list, Items.DETECTOR_RAIL, "Detector Rail", price(18.00));
        add(list, Items.ACTIVATOR_RAIL,"Activator Rail",price(18.00));

        // Minecarts
        add(list, Items.MINECART,         "Minecart",                price(60.00));
        add(list, Items.CHEST_MINECART,   "Minecart with Chest",     price(80.00));
        add(list, Items.FURNACE_MINECART, "Minecart with Furnace",   price(80.00));
        add(list, Items.HOPPER_MINECART,  "Minecart with Hopper",    price(150.00));
        add(list, Items.TNT_MINECART,     "Minecart with TNT",       price(120.00));

        return list;
    }

    // ─────────────────────────────────────────────
    //  FOOD
    // ─────────────────────────────────────────────
    // Dynamically priced by hunger + saturation.
    // Adjusted so bread (~iron-tier grind result) costs ~$3,
    // steak / cooked porkchop cost ~$8-10.

    private static List<ShopItem> food() {
        List<ShopItem> list = new ArrayList<>();

        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);

            // Automatically add vanilla Minecraft foods only.
            // Modded foods will not appear unless manually added elsewhere.
            if (!id.getNamespace().equals("minecraft")) {
                continue;
            }

            if (!item.isFood()) {
                continue;
            }

            if (item == Items.GOLDEN_APPLE ||
                    item == Items.ENCHANTED_GOLDEN_APPLE ||
                    item == Items.GOLDEN_CARROT ||
                    item == Items.CHORUS_FRUIT) {
                continue;
            }

            add(list, item, prettyName(item), foodPrice(item));
        }

        list.sort(Comparator.comparing(shopItem -> shopItem.name));
        return list;
    }

    private static int foodPrice(Item item) {
        if (item.getFoodComponent() == null) {
            return price(5.00);
        }

        int hunger = item.getFoodComponent().getHunger();
        float saturation = item.getFoodComponent().getSaturationModifier();

        // Base: hunger × 1.5 + saturation × 5.0, minimum $2.00
        double dollars = Math.max(2.00, hunger * 1.5 + saturation * 5.0);

        return price(dollars);
    }

    // ─────────────────────────────────────────────
    //  REDSTONE
    // ─────────────────────────────────────────────
    // Priced relative to iron ($12) since most redstone
    // components cost iron to craft.

    private static List<ShopItem> redstone() {
        List<ShopItem> list = new ArrayList<>();

        add(list, Items.REDSTONE,         "Redstone Dust",     price(2.00));
        add(list, Items.REDSTONE_BLOCK,   "Block of Redstone", price(18.00));
        add(list, Items.REDSTONE_TORCH,   "Redstone Torch",    price(1.50));
        add(list, Items.REPEATER,         "Repeater",          price(8.00));
        add(list, Items.COMPARATOR,       "Comparator",        price(15.00));
        add(list, Items.LEVER,            "Lever",             price(1.50));
        add(list, Items.PISTON,           "Piston",            price(20.00));
        add(list, Items.STICKY_PISTON,    "Sticky Piston",     price(30.00));
        add(list, Items.OBSERVER,         "Observer",          price(25.00));
        add(list, Items.DISPENSER,        "Dispenser",         price(25.00));
        add(list, Items.DROPPER,          "Dropper",           price(15.00));
        add(list, Items.HOPPER,           "Hopper",            price(80.00));   // 5 iron = $60 + markup
        add(list, Items.TARGET,           "Target",            price(12.00));
        add(list, Items.DAYLIGHT_DETECTOR,"Daylight Detector", price(25.00));
        add(list, Items.TRIPWIRE_HOOK,    "Tripwire Hook",     price(4.00));
        add(list, Items.SCULK_SENSOR,     "Sculk Sensor",      price(120.00));
        addByPath(list, "calibrated_sculk_sensor",             price(180.00));
        add(list, Items.TNT,              "TNT",               price(50.00));
        add(list, Items.NOTE_BLOCK,       "Note Block",        price(12.00));
        add(list, Items.JUKEBOX,          "Jukebox",           price(150.00));
        add(list, Items.REDSTONE_LAMP,    "Redstone Lamp",     price(15.00));

        String[] woods = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo"};

        for (String wood : woods) {
            addByPath(list, wood + "_button",         price(0.50));
            addByPath(list, wood + "_pressure_plate", price(1.50));
            addByPath(list, wood + "_door",           price(2.00));
            addByPath(list, wood + "_trapdoor",       price(1.50));
        }

        add(list, Items.STONE_BUTTON,                          "Stone Button",                    price(0.50));
        add(list, Items.POLISHED_BLACKSTONE_BUTTON,            "Polished Blackstone Button",      price(0.75));
        add(list, Items.STONE_PRESSURE_PLATE,                  "Stone Pressure Plate",            price(1.50));
        add(list, Items.POLISHED_BLACKSTONE_PRESSURE_PLATE,    "Polished Blackstone Pressure Plate", price(2.00));
        add(list, Items.LIGHT_WEIGHTED_PRESSURE_PLATE,         "Light Weighted Pressure Plate",   price(25.00));
        add(list, Items.HEAVY_WEIGHTED_PRESSURE_PLATE,         "Heavy Weighted Pressure Plate",   price(25.00));

        add(list, Items.SLIME_BLOCK, "Slime Block", price(60.00));  // 9 slimeballs × $6 + markup
        add(list, Items.HONEY_BLOCK, "Honey Block", price(40.00));

        return list;
    }

    // ─────────────────────────────────────────────
    //  FUNCTIONAL
    // ─────────────────────────────────────────────

    private static List<ShopItem> functional() {
        List<ShopItem> list = new ArrayList<>();

        addById(list, "vinurl", "custom_record", price(15.00));

        add(list, Items.CRAFTING_TABLE,    "Crafting Table",    price(3.00));
        add(list, Items.FURNACE,           "Furnace",           price(5.00));
        add(list, Items.BLAST_FURNACE,     "Blast Furnace",     price(50.00));
        add(list, Items.SMOKER,            "Smoker",            price(25.00));
        add(list, Items.CARTOGRAPHY_TABLE, "Cartography Table", price(15.00));
        add(list, Items.FLETCHING_TABLE,   "Fletching Table",   price(15.00));
        add(list, Items.SMITHING_TABLE,    "Smithing Table",    price(35.00));
        add(list, Items.GRINDSTONE,        "Grindstone",        price(25.00));
        add(list, Items.STONECUTTER,       "Stonecutter",       price(20.00));
        add(list, Items.LOOM,              "Loom",              price(15.00));
        add(list, Items.BREWING_STAND,     "Brewing Stand",     price(80.00));
        add(list, Items.CAULDRON,          "Cauldron",          price(60.00));
        add(list, Items.ANVIL,             "Anvil",             price(400.00));  // 31 iron = ~$372 + markup

        add(list, Items.CHEST,       "Chest",       price(8.00));
        add(list, Items.ENDER_CHEST, "Ender Chest", price(300.00));
        add(list, Items.BARREL,      "Barrel",      price(8.00));
        add(list, Items.SHULKER_BOX, "Shulker Box", price(500.00));

        add(list, Items.HOPPER,    "Hopper",    price(80.00));
        add(list, Items.DROPPER,   "Dropper",   price(15.00));
        add(list, Items.DISPENSER, "Dispenser", price(25.00));

        add(list, Items.LADDER,      "Ladder",      price(0.75));
        add(list, Items.SCAFFOLDING, "Scaffolding", price(3.00));

        add(list, Items.TORCH,       "Torch",       price(0.25));
        add(list, Items.SOUL_TORCH,  "Soul Torch",  price(0.50));
        add(list, Items.LANTERN,     "Lantern",     price(10.00));
        add(list, Items.SOUL_LANTERN,"Soul Lantern",price(12.00));
        add(list, Items.CAMPFIRE,    "Campfire",    price(15.00));
        add(list, Items.SOUL_CAMPFIRE,"Soul Campfire",price(20.00));

        add(list, Items.BOOKSHELF,         "Bookshelf",          price(40.00));
        add(list, Items.CHISELED_BOOKSHELF,"Chiseled Bookshelf", price(50.00));
        add(list, Items.LECTERN,           "Lectern",            price(50.00));

        add(list, Items.BELL,          "Bell",          price(120.00));
        add(list, Items.COMPOSTER,     "Composter",     price(10.00));
        add(list, Items.BEE_NEST,      "Bee Nest",      price(120.00));
        add(list, Items.BEEHIVE,       "Beehive",       price(80.00));
        add(list, Items.LODESTONE,     "Lodestone",     price(500.00));
        add(list, Items.RESPAWN_ANCHOR,"Respawn Anchor",price(500.00));
        add(list, Items.DECORATED_POT, "Decorated Pot", price(30.00));
        add(list, Items.FLOWER_POT,    "Flower Pot",    price(4.00));

        add(list, Items.PAINTING,       "Painting",        price(8.00));
        add(list, Items.ITEM_FRAME,     "Item Frame",      price(10.00));
        add(list, Items.GLOW_ITEM_FRAME,"Glow Item Frame", price(35.00));
        add(list, Items.ARMOR_STAND,    "Armor Stand",     price(18.00));

        String[] colors = {
                "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        };

        for (String color : colors) {
            addByPath(list, color + "_bed",    price(30.00));
            addByPath(list, color + "_banner", price(15.00));
            addByPath(list, color + "_candle", price(4.00));
        }

        String[] woods = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo"};

        for (String wood : woods) {
            addByPath(list, wood + "_sign",         price(8.00));
            addByPath(list, wood + "_hanging_sign", price(15.00));
        }

        return list;
    }

    // ─────────────────────────────────────────────
    //  BLOCKS
    // ─────────────────────────────────────────────

    private static List<ShopItem> blocks() {
        List<ShopItem> list = new ArrayList<>();

        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);

            // Automatically add vanilla Minecraft blocks only.
            // Modded blocks will not appear unless manually added elsewhere.
            if (!id.getNamespace().equals("minecraft")) {
                continue;
            }

            if (!(item instanceof BlockItem)) {
                continue;
            }

            // Do not add an item to Blocks when it is already deliberately
            // priced in another category. This prevents a cheap fallback block
            // price from overriding things such as hoppers, anvils and chests.
            if (itemsListedOutsideBlocks().contains(item)) {
                continue;
            }

            String path = id.getPath();

            if (isBannedBlock(path)) {
                continue;
            }

            add(list, item, prettyName(item), blockPrice(path));
        }

        list.sort(Comparator.comparing(shopItem -> shopItem.name));
        return list;
    }

    private static Set<Item> outsideBlocksCache;

    private static synchronized Set<Item> itemsListedOutsideBlocks() {
        if (outsideBlocksCache != null) {
            return outsideBlocksCache;
        }

        Set<Item> items = new HashSet<>();

        for (ShopItem shopItem : ingredients()) items.add(shopItem.item);
        for (ShopItem shopItem : combat()) items.add(shopItem.item);
        for (ShopItem shopItem : food()) items.add(shopItem.item);
        for (ShopItem shopItem : redstone()) items.add(shopItem.item);
        for (ShopItem shopItem : functional()) items.add(shopItem.item);

        outsideBlocksCache = Set.copyOf(items);
        return outsideBlocksCache;
    }

    private static boolean isBannedBlock(String path) {
        if (path.equals("gold_block") ||
                path.equals("redstone_block") ||
                path.equals("emerald_block") ||
                path.equals("lapis_block") ||
                path.equals("diamond_block") ||
                path.equals("netherite_block") ||
                path.equals("reinforced_deepslate") ||
                path.equals("bedrock") ||
                path.equals("dragon_egg") ||
                path.equals("end_portal_frame") ||
                path.equals("turtle_egg") ||
                path.equals("sniffer_egg") ||
                path.equals("ancient_debris") ||
                path.equals("budding_amethyst") ||
                path.equals("amethyst_block") ||
                path.equals("amethyst_cluster") ||
                path.equals("nether_wart") ||
                path.equals("enchanting_table") ||
                path.equals("beacon") ||
                path.equals("conduit") ||
                path.equals("end_crystal")) {
            return true;
        }

        if (path.contains("_ore")) return true;
        if (path.startsWith("raw_") && path.endsWith("_block")) return true;
        if (path.contains("amethyst")) return true;
        if (path.contains("shulker_box")) return true;
        if (path.endsWith("_head") || path.endsWith("_skull")) return true;

        if (path.contains("command_block") ||
                path.equals("barrier") ||
                path.equals("structure_block") ||
                path.equals("structure_void") ||
                path.equals("jigsaw") ||
                path.equals("light") ||
                path.equals("spawner")) {
            return true;
        }

        return false;
    }

    // Block prices scaled relative to the iron foundation ($12/ingot).
    // Common dirt/stone = very cheap. Rare nether/end blocks = more.
    private static int blockPrice(String path) {
        if (path.equals("iron_block")) return price(108.00);  // 9 iron ingots × $12
        if (path.equals("coal_block")) return price(18.00);   // 9 coal × $2
        if (path.equals("dried_kelp_block")) return price(40.00);

        if (path.contains("obsidian"))  return price(15.00);

        if (path.contains("copper"))    return price(50.00);   // 9 copper ingots × $6 + markup
        if (path.contains("quartz"))    return price(12.00);
        if (path.contains("prismarine"))return price(5.00);
        if (path.contains("sea_lantern"))return price(12.00);
        if (path.contains("glowstone")) return price(5.00);

        if (path.contains("end_stone")) return price(3.00);
        if (path.contains("deepslate")) return price(0.50);
        if (path.contains("blackstone"))return price(0.75);
        if (path.contains("basalt"))    return price(0.50);
        if (path.contains("netherrack"))return price(0.20);
        if (path.contains("magma"))     return price(1.00);
        if (path.contains("soul_sand") || path.contains("soul_soil")) return price(1.50);

        if (path.contains("sand") || path.contains("gravel") || path.contains("dirt")) return price(0.25);
        if (path.contains("mud"))       return price(0.50);
        if (path.contains("clay"))      return price(1.00);

        if (path.contains("log") || path.contains("stem") || path.contains("wood") || path.contains("hyphae")) return price(2.00);
        if (path.contains("planks"))    return price(0.75);
        if (path.contains("stairs"))    return price(0.75);
        if (path.contains("slab"))      return price(0.40);
        if (path.contains("fence") || path.contains("wall")) return price(1.00);

        if (path.contains("glass"))     return price(1.00);
        if (path.contains("concrete"))  return price(1.50);
        if (path.contains("terracotta"))return price(1.50);
        if (path.contains("wool"))      return price(1.50);
        if (path.contains("carpet"))    return price(0.75);

        if (path.contains("ice"))       return price(2.50);
        if (path.contains("snow"))      return price(0.50);

        return price(0.75); // default fallback
    }

    // ─────────────────────────────────────────────
    //  SELL PRICES
    // ─────────────────────────────────────────────
    //
    // Only items that already exist somewhere in /shop are included.
    // Foundation prices are exact. Other items use conservative category rates
    // and are rounded DOWN so crafting/reselling is less exploitable:
    // ingredients 35%, blocks/food 30%, crafted gear/machines 25%.

    private static Map<Item, Integer> sellPriceCache;

    /**
     * Returns the sell price in cents, or null when the item is not sold in /shop.
     */
    public static Integer getSellPrice(Item item) {
        return sellPrices().get(item);
    }

    public static boolean canSell(Item item) {
        return getSellPrice(item) != null;
    }

    private static synchronized Map<Item, Integer> sellPrices() {
        if (sellPriceCache != null) {
            return sellPriceCache;
        }

        Map<Item, Integer> prices = new HashMap<>();

        for (Category category : Category.values()) {
            for (ShopItem shopItem : get(category)) {
                int calculated = automaticSellPrice(category, shopItem.price);

                // Some items appear in more than one category.
                // Keep the lower sell price if that ever happens.
                prices.merge(shopItem.item, calculated, Math::min);
            }
        }

        // Exact foundation prices requested by the server owner.
        prices.put(Items.COPPER_INGOT, price(2.50));
        prices.put(Items.IRON_INGOT,   price(5.00));
        prices.put(Items.GOLD_INGOT,   price(5.00));
        prices.put(Items.DIAMOND,      price(10.00));
        prices.put(Items.EMERALD,      price(5.00));

        sellPriceCache = Map.copyOf(prices);
        return sellPriceCache;
    }

    /**
     * Conservative automatic rates:
     * - Ingredients: 35% of buy price
     * - Blocks and food: 30%
     * - Combat, redstone and functional items: 25%
     *
     * The result is always rounded down:
     * - buy below $1.00: nearest $0.05
     * - buy below $10.00: nearest $0.10
     * - buy $10.00+: nearest $0.50
     */
    private static int automaticSellPrice(Category category, int buyPriceCents) {
        int percentage = switch (category) {
            case INGREDIENTS -> 35;
            case BLOCKS, FOOD -> 30;
            case COMBAT, REDSTONE, FUNCTIONAL -> 25;
        };

        int rawSellCents = Math.max(5, buyPriceCents * percentage / 100);

        int roundingStep;
        if (buyPriceCents < 100) {
            roundingStep = 5;
        } else if (buyPriceCents < 1000) {
            roundingStep = 10;
        } else {
            roundingStep = 50;
        }

        return Math.max(roundingStep, (rawSellCents / roundingStep) * roundingStep);
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private static void add(List<ShopItem> list, Item item, String name, int price) {
        if (item != Items.AIR) {
            list.add(new ShopItem(item, name, price));
        }
    }

    private static void addByPath(List<ShopItem> list, String path, int price) {
        Item item = Registries.ITEM.get(new Identifier("minecraft", path));

        if (item != Items.AIR) {
            add(list, item, prettyName(item), price);
        }
    }

    private static void addById(List<ShopItem> list, String namespace, String path, int price) {
        Item item = Registries.ITEM.get(new Identifier(namespace, path));

        if (item != Items.AIR) {
            add(list, item, prettyName(item), price);
        }
    }

    private static String prettyName(Item item) {
        String path = Registries.ITEM.getId(item).getPath();
        String[] parts = path.split("_");

        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;

            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
            builder.append(" ");
        }

        return builder.toString().trim();
    }
}
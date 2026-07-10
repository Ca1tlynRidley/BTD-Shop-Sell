# BTD Shop & Sell

BTD Shop & Sell is a Fabric mod for Minecraft Java Edition 1.20.1 that adds graphical `/shop` and `/sell` menus powered by the Impactor economy system.

## Features

- Double-chest `/shop` buying interface
- Double-chest `/sell` selling interface
- Ingredients, Blocks, Combat, Food, Redstone, and Functional categories
- Buy individual items or complete stacks
- Preconfigured buy and sell prices
- Unsupported items are safely returned
- Unsold items are returned when the sell menu is closed
- Player balance display using the player's head
- Automatic vanilla food and block registration
- Optional support for selected modded items

## Requirements

- Minecraft Java Edition 1.20.1
- Java 17
- Fabric Loader
- Fabric API
- Impactor

BTD Shop & Sell must be installed on both the server and every connecting player's client.

Impactor is required on the server for balances and economy transactions.

## Installation

### Server

1. Install Fabric Loader for Minecraft 1.20.1.
2. Install Fabric API.
3. Install Impactor.
4. Place the BTD Shop & Sell JAR inside the server's `mods` folder.
5. Fully restart the server.

### Client

1. Install Fabric Loader for Minecraft 1.20.1.
2. Install Fabric API.
3. Place the BTD Shop & Sell JAR inside the client's `.minecraft/mods` folder.
4. Fully restart Minecraft.

Do not use `/reload` after installing or replacing the mod.

## Commands

| Command | Description |
|---|---|
| `/shop` | Opens the buying menu |
| `/sell` | Opens the selling menu |

## How selling works

1. Run `/sell`.
2. Place supported items inside the selling area.
3. Click the lime Sell button.
4. Valid items are removed and their value is deposited into the player's Impactor balance.
5. Unsupported items are returned safely.
6. Closing the menu cancels the sale and returns all unsold items.

## Pricing

Version 1.0.0 uses preconfigured prices stored inside the source code.

There is currently no external configuration file for changing prices. Server owners who want custom prices must edit `ShopItems.java` and rebuild the mod.

Base sell prices include:

| Item | Sell price |
|---|---:|
| Copper Ingot | $2.50 |
| Iron Ingot | $5.00 |
| Gold Ingot | $5.00 |
| Diamond | $10.00 |
| Emerald | $5.00 |

Other sell prices are calculated from their configured buying prices.

## Mod compatibility

Automatically registered food and block items are restricted to the vanilla `minecraft:` namespace.

Modded items are not automatically added. A modded item must be explicitly supported in the source code before it can be bought or sold.

Optional compatibility is included for the VinURL Custom Record when VinURL is installed.

VinURL is not included or redistributed with this project.

## Building from source

Clone or download this repository.

### Windows

```powershell
.\gradlew.bat clean build

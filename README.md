# Minecraft Historical Arsenal Plugin

Age of Empires Arsenal is a Minecraft historical weapons and military equipment plugin for Paper. It adds a catalog of Greece, Rome, Viking, medieval, Japanese, and Mongolian themed weapons and armor, with custom-named items, crafting recipes, combat effects, and right-click abilities. Items use vanilla materials with custom model data; custom 3D models, textures, sounds, or animations are not included.

## Install

1. Use a Paper 1.21.11 server running Java 21.
2. Build the plugin with `mvn package`.
3. Copy `target/plugin.jar` into the server's `plugins` directory.
4. Restart the server.

## Use

- Run `/arsenal list` to view the equipment catalog.
- Run `/arsenal give <player> <item-key>` to give an item, for example `/arsenal give Steve katana`.
- Craft equipment using the recipes registered by the plugin. Each recipe uses the item’s listed material ingredient and sticks.
- Sneak and right-click with an arsenal item to use its ability. Abilities have an 8-second cooldown.
- Java and Bedrock players can use the plugin through Paper and Geyser/Floodgate. Custom visuals require a compatible resource pack, which this project does not provide.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/9d1091e7-512d-47e1-86fa-de1ef229842b?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=9d1091e7-512d-47e1-86fa-de1ef229842b
